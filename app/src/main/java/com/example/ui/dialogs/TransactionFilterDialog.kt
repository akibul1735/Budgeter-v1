package com.example.ui.dialogs

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

/**
 * Utility function to compute start and end epoch ms for LedgerDatePreset.
 */
fun computePresetDateBounds(preset: LedgerDatePreset, nowMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
    return when (preset) {
        LedgerDatePreset.LAST_12_MONTHS -> {
            cal.add(Calendar.MONTH, -12)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            Pair(cal.timeInMillis, DateUtils.getEndOfDay(nowMs) + 86400000L * 365L)
        }
        LedgerDatePreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        LedgerDatePreset.TODAY -> {
            val start = DateUtils.getStartOfDay(nowMs)
            Pair(start, DateUtils.getEndOfDay(nowMs))
        }
        LedgerDatePreset.YESTERDAY -> {
            val todayStart = DateUtils.getStartOfDay(nowMs)
            val yestStart = todayStart - 86400000L
            Pair(yestStart, todayStart - 1L)
        }
        LedgerDatePreset.THIS_WEEK -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val start = DateUtils.getStartOfDay(cal.timeInMillis)
            Pair(start, DateUtils.getEndOfDay(nowMs) + (7L * 86400000L))
        }
        LedgerDatePreset.LAST_WEEK -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val thisWeekStart = DateUtils.getStartOfDay(cal.timeInMillis)
            val lastWeekStart = thisWeekStart - (7L * 86400000L)
            Pair(lastWeekStart, thisWeekStart - 1L)
        }
        LedgerDatePreset.THIS_MONTH -> {
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
        }
        LedgerDatePreset.LAST_MONTH -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, -1)
            val prevYear = cal.get(Calendar.YEAR)
            val prevMonth = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(prevYear, prevMonth), DateUtils.getEndOfMonth(prevYear, prevMonth))
        }
        LedgerDatePreset.THIS_QUARTER -> {
            val year = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val quarterStartMonth = (currentMonth / 3) * 3 + 1
            val quarterEndMonth = quarterStartMonth + 2
            Pair(DateUtils.getStartOfMonth(year, quarterStartMonth), DateUtils.getEndOfMonth(year, quarterEndMonth))
        }
        LedgerDatePreset.THIS_YEAR -> {
            val year = cal.get(Calendar.YEAR)
            Pair(DateUtils.getStartOfMonth(year, 1), DateUtils.getEndOfMonth(year, 12))
        }
        LedgerDatePreset.LAST_YEAR -> {
            val curYear = cal.get(Calendar.YEAR)
            val lastYear = curYear - 1
            Pair(DateUtils.getStartOfMonth(lastYear, 1), DateUtils.getEndOfMonth(lastYear, 12))
        }
        LedgerDatePreset.SINCE_LAST_YEAR -> {
            val curYear = cal.get(Calendar.YEAR)
            val lastYear = curYear - 1
            Pair(DateUtils.getStartOfMonth(lastYear, 1), DateUtils.getEndOfMonth(curYear, 12))
        }
        LedgerDatePreset.CUSTOM -> Pair(0L, nowMs)
    }
}

/**
 * Display Settings for Ledger screen transactions.
 */
data class TransactionDisplaySettings(
    val showTotalAmount: Boolean = true,
    val showTransfersInTotal: Boolean = false,
    val showAllTransactionsForNewAccount: Boolean = true,
    val showAccountBalance: Boolean = true,
    val showOldestDateFirst: Boolean = false
)

/**
 * Complete State representation for Transaction Filter Dialog.
 */
data class TransactionFilterState(
    val searchQuery: String = "",
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val datePreset: LedgerDatePreset = LedgerDatePreset.LAST_12_MONTHS,
    val startDateMs: Long = 0L,
    val endDateMs: Long = System.currentTimeMillis(),
    val transactionType: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val label: String? = null,
    val status: TransactionStatus? = null,
    val rowStyle: LedgerRowStyle = LedgerRowStyle.STANDARD,
    val displaySettings: TransactionDisplaySettings = TransactionDisplaySettings()
) {
    fun activeFilterCount(): Int {
        var count = 0
        if (searchQuery.isNotBlank()) count++
        if ((minAmount != null && minAmount > 0) || (maxAmount != null && maxAmount > 0)) count++
        if (datePreset != LedgerDatePreset.LAST_12_MONTHS) count++
        if (transactionType != null) count++
        if (categoryId != null) count++
        if (accountId != null) count++
        if (!label.isNullOrBlank()) count++
        if (status != null) count++
        return count
    }
}

/**
 * Saved filter preset model for persistence.
 */
data class SavedTransactionFilterPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val state: TransactionFilterState,
    val createdAtMs: Long = System.currentTimeMillis()
)

/**
 * SharedPreferences storage for transaction filter presets.
 */
object TransactionFilterPresetsStorage {
    private const val PREFS_NAME = "transaction_filter_presets_storage"
    private const val PREFS_KEY = "saved_transaction_filter_presets_v1"

    fun savePresets(context: Context, presets: List<SavedTransactionFilterPreset>) {
        try {
            val jsonArray = JSONArray()
            for (p in presets) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("createdAtMs", p.createdAtMs)
                    put("searchQuery", p.state.searchQuery)
                    put("minAmount", p.state.minAmount ?: -1.0)
                    put("maxAmount", p.state.maxAmount ?: -1.0)
                    put("datePreset", p.state.datePreset.name)
                    put("startDateMs", p.state.startDateMs)
                    put("endDateMs", p.state.endDateMs)
                    put("transactionType", p.state.transactionType?.name ?: "")
                    put("categoryId", p.state.categoryId ?: -1L)
                    put("accountId", p.state.accountId ?: -1L)
                    put("label", p.state.label ?: "")
                    put("status", p.state.status?.name ?: "")
                    put("rowStyle", p.state.rowStyle.name)
                    put("showTotalAmount", p.state.displaySettings.showTotalAmount)
                    put("showTransfersInTotal", p.state.displaySettings.showTransfersInTotal)
                    put("showAllTransactionsForNewAccount", p.state.displaySettings.showAllTransactionsForNewAccount)
                    put("showAccountBalance", p.state.displaySettings.showAccountBalance)
                    put("showOldestDateFirst", p.state.displaySettings.showOldestDateFirst)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFS_KEY, jsonArray.toString())
                .apply()
        } catch (_: Exception) {}
    }

    fun loadPresets(context: Context): List<SavedTransactionFilterPreset> {
        return try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PREFS_KEY, null) ?: return emptyList()
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<SavedTransactionFilterPreset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Preset")
                val createdAt = obj.optLong("createdAtMs", System.currentTimeMillis())

                val datePreset = try {
                    LedgerDatePreset.valueOf(obj.optString("datePreset", LedgerDatePreset.LAST_12_MONTHS.name))
                } catch (_: Exception) {
                    LedgerDatePreset.LAST_12_MONTHS
                }

                val txType = obj.optString("transactionType").takeIf { it.isNotBlank() }?.let {
                    try { TransactionType.valueOf(it) } catch (_: Exception) { null }
                }

                val status = obj.optString("status").takeIf { it.isNotBlank() }?.let {
                    try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
                }

                val rowStyle = try {
                    LedgerRowStyle.valueOf(obj.optString("rowStyle", LedgerRowStyle.STANDARD.name))
                } catch (_: Exception) {
                    LedgerRowStyle.STANDARD
                }

                val minAmt = obj.optDouble("minAmount", -1.0).takeIf { it >= 0.0 }
                val maxAmt = obj.optDouble("maxAmount", -1.0).takeIf { it >= 0.0 }
                val catId = obj.optLong("categoryId", -1L).takeIf { it != -1L }
                val accId = obj.optLong("accountId", -1L).takeIf { it != -1L }
                val lbl = obj.optString("label").takeIf { it.isNotBlank() }

                val displaySettings = TransactionDisplaySettings(
                    showTotalAmount = obj.optBoolean("showTotalAmount", true),
                    showTransfersInTotal = obj.optBoolean("showTransfersInTotal", false),
                    showAllTransactionsForNewAccount = obj.optBoolean("showAllTransactionsForNewAccount", true),
                    showAccountBalance = obj.optBoolean("showAccountBalance", true),
                    showOldestDateFirst = obj.optBoolean("showOldestDateFirst", false)
                )

                val state = TransactionFilterState(
                    searchQuery = obj.optString("searchQuery", ""),
                    minAmount = minAmt,
                    maxAmount = maxAmt,
                    datePreset = datePreset,
                    startDateMs = obj.optLong("startDateMs", 0L),
                    endDateMs = obj.optLong("endDateMs", System.currentTimeMillis()),
                    transactionType = txType,
                    categoryId = catId,
                    accountId = accId,
                    label = lbl,
                    status = status,
                    rowStyle = rowStyle,
                    displaySettings = displaySettings
                )

                list.add(SavedTransactionFilterPreset(id, name, state, createdAt))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}

/**
 * Transaction Filter Popup component adhering to specification:
 * Header (Drag handle, Row style, Reset, Save, Open Folder), Search, Amount Range (From/To),
 * Date Filter (Preset dropdown, Start Date, End Date), Type dropdown, Category dropdown + options,
 * Account dropdown + options, Labels dropdown + options, Status dropdown + options,
 * Display Settings (Toggle switches), Fixed Footer (Cancel, OK).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterDialog(
    initialState: TransactionFilterState,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    allTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onApply: (TransactionFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var filterState by remember { mutableStateOf(initialState) }

    // Text inputs for amounts
    var minAmountText by remember {
        mutableStateOf(if (initialState.minAmount != null && initialState.minAmount > 0) initialState.minAmount.toInt().toString() else "")
    }
    var maxAmountText by remember {
        mutableStateOf(if (initialState.maxAmount != null && initialState.maxAmount > 0) initialState.maxAmount.toInt().toString() else "")
    }

    // Sub-dialogs
    var showFromCalc by remember { mutableStateOf(false) }
    var showToCalc by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showCategoryPickerModal by remember { mutableStateOf(false) }
    var showAccountPickerModal by remember { mutableStateOf(false) }
    var showLabelPickerModal by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showSavedPresetsDialog by remember { mutableStateOf(false) }
    var showRowStyleMenu by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedTransactionFilterPreset>().apply {
            addAll(TransactionFilterPresetsStorage.loadPresets(context))
        }
    }

    val activeCount = remember(filterState, minAmountText, maxAmountText) {
        val parsedMin = minAmountText.toDoubleOrNull()
        val parsedMax = maxAmountText.toDoubleOrNull()
        filterState.copy(minAmount = parsedMin, maxAmount = parsedMax).activeFilterCount()
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "transaction_filter_dialog"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("transaction_filter_popup")
        ) {
            // ─── 1. HEADER ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Title + Active Filter Count Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার লেনদেন" else "Filter Transactions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (activeCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$activeCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Header Action Buttons: Row Style, Reset, Save, Open Folder
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Row Style button
                        Box {
                            IconButton(
                                onClick = { showRowStyleMenu = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("filter_row_style_btn")
                            ) {
                                Icon(
                                    imageVector = when (filterState.rowStyle) {
                                        LedgerRowStyle.STANDARD -> Icons.Default.ViewAgenda
                                        LedgerRowStyle.COMPACT -> Icons.Default.TableRows
                                        LedgerRowStyle.DETAILED -> Icons.Default.GridView
                                    },
                                    contentDescription = if (languageMode == LanguageMode.BANGLA) "সারি স্টাইল" else "Row Style",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showRowStyleMenu,
                                onDismissRequest = { showRowStyleMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Standard View") },
                                    leadingIcon = { Icon(Icons.Default.ViewAgenda, contentDescription = null) },
                                    trailingIcon = { if (filterState.rowStyle == LedgerRowStyle.STANDARD) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                                    onClick = {
                                        filterState = filterState.copy(rowStyle = LedgerRowStyle.STANDARD)
                                        showRowStyleMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Compact View") },
                                    leadingIcon = { Icon(Icons.Default.TableRows, contentDescription = null) },
                                    trailingIcon = { if (filterState.rowStyle == LedgerRowStyle.COMPACT) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                                    onClick = {
                                        filterState = filterState.copy(rowStyle = LedgerRowStyle.COMPACT)
                                        showRowStyleMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Detailed View") },
                                    leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null) },
                                    trailingIcon = { if (filterState.rowStyle == LedgerRowStyle.DETAILED) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                                    onClick = {
                                        filterState = filterState.copy(rowStyle = LedgerRowStyle.DETAILED)
                                        showRowStyleMenu = false
                                    }
                                )
                            }
                        }

                        // 2. Reset button
                        IconButton(
                            onClick = {
                                filterState = TransactionFilterState(
                                    rowStyle = filterState.rowStyle,
                                    displaySettings = filterState.displaySettings
                                )
                                minAmountText = ""
                                maxAmountText = ""
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("filter_reset_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 3. Save button
                        IconButton(
                            onClick = { showSavePresetDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("filter_save_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = if (languageMode == LanguageMode.BANGLA) "প্রিসেট সংরক্ষণ" else "Save Preset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 4. Open Folder button
                        IconButton(
                            onClick = { showSavedPresetsDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("filter_open_folder_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত প্রিসেট" else "Open Presets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ─── 2. SCROLLABLE CONTENT ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── 2.1 Search by item name, payee, or notes ──
                FilterSectionWrapper(
                    title = if (languageMode == LanguageMode.BANGLA) "অনুসন্ধান" else "Search",
                    icon = Icons.Default.Search
                ) {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { filterState = filterState.copy(searchQuery = it) },
                        placeholder = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আইটেম, প্রাপক বা নোট দিয়ে খুঁজুন..." else "Search by item name, payee, or notes",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { filterState = filterState.copy(searchQuery = "") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filter_search_input")
                    )
                }

                // ── 2.2 Amount Range (Amount From / Amount To) ──
                FilterSectionWrapper(
                    title = if (languageMode == LanguageMode.BANGLA) "পরিমাণের সীমা" else "Amount Range",
                    icon = Icons.Default.Calculate
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmountText,
                            onValueChange = { minAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বনিম্ন (৳)" else "Amount From", fontSize = 11.sp) },
                            placeholder = { Text("0.00", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                IconButton(onClick = { showFromCalc = true }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_amount_from_input")
                        )

                        OutlinedTextField(
                            value = maxAmountText,
                            onValueChange = { maxAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ (৳)" else "Amount To", fontSize = 11.sp) },
                            placeholder = { Text("∞", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                IconButton(onClick = { showToCalc = true }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_amount_to_input")
                        )
                    }
                }

                // ── 2.3 Date Filter (Date Range Dropdown + Start Date + End Date) ──
                FilterSectionWrapper(
                    title = if (languageMode == LanguageMode.BANGLA) "সময়কাল ফিল্টার" else "Date Filter",
                    icon = Icons.Default.CalendarToday
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Date Range dropdown selector
                        var showDatePresetMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDatePresetMenu = true }
                                    .testTag("filter_date_range_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = filterState.datePreset.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showDatePresetMenu,
                                onDismissRequest = { showDatePresetMenu = false }
                            ) {
                                LedgerDatePreset.values().forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset.displayName, fontSize = 12.sp) },
                                        trailingIcon = {
                                            if (filterState.datePreset == preset) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary)
                                            }
                                        },
                                        onClick = {
                                            if (preset == LedgerDatePreset.CUSTOM) {
                                                filterState = filterState.copy(datePreset = preset)
                                            } else {
                                                val bounds = computePresetDateBounds(preset)
                                                filterState = filterState.copy(
                                                    datePreset = preset,
                                                    startDateMs = bounds.first,
                                                    endDateMs = if (bounds.second == Long.MAX_VALUE) System.currentTimeMillis() else bounds.second
                                                )
                                            }
                                            showDatePresetMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Start Date & End Date fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start Date
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showStartDatePicker = true }
                                    .testTag("filter_start_date_picker_btn")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "শুরুর তারিখ" else "Start Date",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (filterState.startDateMs > 0) DateUtils.formatDate(filterState.startDateMs, languageMode) else "Beginning",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // End Date
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showEndDatePicker = true }
                                    .testTag("filter_end_date_picker_btn")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "শেষ তারিখ" else "End Date",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DateUtils.formatDate(filterState.endDateMs, languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 2.4 Transaction Type Dropdown ──
                var showTypeDropdown by remember { mutableStateOf(false) }
                FilterDropdownSelectorRow(
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেনের ধরন" else "Transaction Type",
                    selectedValue = when (filterState.transactionType) {
                        null -> if (languageMode == LanguageMode.BANGLA) "সকল প্রকার (All)" else "All Types"
                        TransactionType.EXPENSE -> if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expense)" else "Expense"
                        TransactionType.INCOME -> if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income"
                        TransactionType.TRANSFER -> if (languageMode == LanguageMode.BANGLA) "স্থানান্তর (Transfer)" else "Transfer"
                    },
                    icon = Icons.Default.FilterList,
                    iconTint = when (filterState.transactionType) {
                        TransactionType.EXPENSE -> SolidExpense
                        TransactionType.INCOME -> SolidIncome
                        TransactionType.TRANSFER -> SolidTransfer
                        null -> SolidPrimary
                    },
                    isFiltered = filterState.transactionType != null,
                    onClear = { filterState = filterState.copy(transactionType = null) },
                    onClick = { showTypeDropdown = true },
                    testTag = "filter_type_dropdown"
                ) {
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Types") },
                            trailingIcon = { if (filterState.transactionType == null) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                            onClick = {
                                filterState = filterState.copy(transactionType = null)
                                showTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Expense", color = SolidExpense) },
                            trailingIcon = { if (filterState.transactionType == TransactionType.EXPENSE) Icon(Icons.Default.Check, contentDescription = null, tint = SolidExpense) },
                            onClick = {
                                filterState = filterState.copy(transactionType = TransactionType.EXPENSE)
                                showTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Income", color = SolidIncome) },
                            trailingIcon = { if (filterState.transactionType == TransactionType.INCOME) Icon(Icons.Default.Check, contentDescription = null, tint = SolidIncome) },
                            onClick = {
                                filterState = filterState.copy(transactionType = TransactionType.INCOME)
                                showTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Transfer", color = SolidTransfer) },
                            trailingIcon = { if (filterState.transactionType == TransactionType.TRANSFER) Icon(Icons.Default.Check, contentDescription = null, tint = SolidTransfer) },
                            onClick = {
                                filterState = filterState.copy(transactionType = TransactionType.TRANSFER)
                                showTypeDropdown = false
                            }
                        )
                    }
                }

                // ── 2.5 Category Dropdown + Filter Options ──
                val selectedCategory = remember(filterState.categoryId, allCategories) {
                    allCategories.firstOrNull { it.id == filterState.categoryId }
                }
                FilterDropdownSelectorRow(
                    title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                    selectedValue = selectedCategory?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি" else "All Categories"),
                    icon = Icons.Default.Category,
                    iconTint = SolidPrimary,
                    isFiltered = filterState.categoryId != null,
                    showFilterOptionButton = true,
                    onFilterOptionClick = { showCategoryPickerModal = true },
                    onClear = { filterState = filterState.copy(categoryId = null) },
                    onClick = { showCategoryPickerModal = true },
                    testTag = "filter_category_dropdown"
                )

                // ── 2.6 Account Dropdown + Filter Options ──
                val selectedAccount = remember(filterState.accountId, allAccounts) {
                    allAccounts.firstOrNull { it.id == filterState.accountId }
                }
                FilterDropdownSelectorRow(
                    title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account",
                    selectedValue = selectedAccount?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট" else "All Accounts"),
                    icon = Icons.Default.AccountBalance,
                    iconTint = SolidPrimary,
                    isFiltered = filterState.accountId != null,
                    showFilterOptionButton = true,
                    onFilterOptionClick = { showAccountPickerModal = true },
                    onClear = { filterState = filterState.copy(accountId = null) },
                    onClick = { showAccountPickerModal = true },
                    testTag = "filter_account_dropdown"
                )

                // ── 2.7 Labels Dropdown + Filter Options ──
                FilterDropdownSelectorRow(
                    title = if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ" else "Labels",
                    selectedValue = if (!filterState.label.isNullOrBlank()) "#${filterState.label}" else (if (languageMode == LanguageMode.BANGLA) "কোনো ফিল্টার নেই" else "No Filter"),
                    icon = Icons.AutoMirrored.Filled.Label,
                    iconTint = SolidPrimary,
                    isFiltered = !filterState.label.isNullOrBlank(),
                    showFilterOptionButton = true,
                    onFilterOptionClick = { showLabelPickerModal = true },
                    onClear = { filterState = filterState.copy(label = null) },
                    onClick = { showLabelPickerModal = true },
                    testTag = "filter_labels_dropdown"
                )

                // ── 2.8 Status Dropdown + Filter Options ──
                var showStatusDropdown by remember { mutableStateOf(false) }
                FilterDropdownSelectorRow(
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেন স্ট্যাটাস" else "Status",
                    selectedValue = when (filterState.status) {
                        null -> if (languageMode == LanguageMode.BANGLA) "কোনো ফিল্টার নেই" else "No Filter"
                        TransactionStatus.CLEARED -> "Cleared"
                        TransactionStatus.RECONCILED -> "Reconciled"
                        TransactionStatus.VOID -> "Void / Voided"
                        TransactionStatus.NONE -> "None"
                    },
                    icon = Icons.Default.Tune,
                    iconTint = SolidPrimary,
                    isFiltered = filterState.status != null,
                    onClear = { filterState = filterState.copy(status = null) },
                    onClick = { showStatusDropdown = true },
                    testTag = "filter_status_dropdown"
                ) {
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Filter (All)") },
                            trailingIcon = { if (filterState.status == null) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                            onClick = {
                                filterState = filterState.copy(status = null)
                                showStatusDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cleared") },
                            trailingIcon = { if (filterState.status == TransactionStatus.CLEARED) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                            onClick = {
                                filterState = filterState.copy(status = TransactionStatus.CLEARED)
                                showStatusDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reconciled") },
                            trailingIcon = { if (filterState.status == TransactionStatus.RECONCILED) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                            onClick = {
                                filterState = filterState.copy(status = TransactionStatus.RECONCILED)
                                showStatusDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Void") },
                            trailingIcon = { if (filterState.status == TransactionStatus.VOID) Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary) },
                            onClick = {
                                filterState = filterState.copy(status = TransactionStatus.VOID)
                                showStatusDropdown = false
                            }
                        )
                    }
                }

                // ── 2.9 Display Settings (Toggle Switches) ──
                FilterSectionWrapper(
                    title = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন সেটিংস" else "Display Settings",
                    icon = Icons.Default.Tune
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 1. Show Total Amount
                            DisplaySettingToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "মোট পরিমাণ প্রদর্শন" else "Show Total Amount",
                                checked = filterState.displaySettings.showTotalAmount,
                                onCheckedChange = {
                                    filterState = filterState.copy(
                                        displaySettings = filterState.displaySettings.copy(showTotalAmount = it)
                                    )
                                },
                                testTag = "toggle_show_total_amount"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 2. Show Transfers in Total
                            DisplaySettingToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "মোটে স্থানান্তর অন্তর্ভুক্তি" else "Show Transfers in Total",
                                checked = filterState.displaySettings.showTransfersInTotal,
                                onCheckedChange = {
                                    filterState = filterState.copy(
                                        displaySettings = filterState.displaySettings.copy(showTransfersInTotal = it)
                                    )
                                },
                                testTag = "toggle_show_transfers_in_total"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 3. Show All Transactions for New Account
                            DisplaySettingToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "নতুন অ্যাকাউন্টের সকল লেনদেন" else "Show All Transactions for New Account",
                                checked = filterState.displaySettings.showAllTransactionsForNewAccount,
                                onCheckedChange = {
                                    filterState = filterState.copy(
                                        displaySettings = filterState.displaySettings.copy(showAllTransactionsForNewAccount = it)
                                    )
                                },
                                testTag = "toggle_show_all_new_account"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 4. Show Account Balance
                            DisplaySettingToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "চলতি অ্যাকাউন্ট ব্যালেন্স প্রদর্শন" else "Show Account Balance",
                                checked = filterState.displaySettings.showAccountBalance,
                                onCheckedChange = {
                                    filterState = filterState.copy(
                                        displaySettings = filterState.displaySettings.copy(showAccountBalance = it)
                                    )
                                },
                                testTag = "toggle_show_account_balance"
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // 5. Show Oldest Date First
                            DisplaySettingToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "পুরোনো তারিখ প্রথমে প্রদর্শন" else "Show Oldest Date First",
                                checked = filterState.displaySettings.showOldestDateFirst,
                                onCheckedChange = {
                                    filterState = filterState.copy(
                                        displaySettings = filterState.displaySettings.copy(showOldestDateFirst = it)
                                    )
                                },
                                testTag = "toggle_show_oldest_first"
                            )
                        }
                    }
                }
            }

            // ─── 3. FIXED FOOTER (Cancel & OK) ──────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("filter_cancel_button")
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val parsedMin = minAmountText.toDoubleOrNull()
                        val parsedMax = maxAmountText.toDoubleOrNull()
                        val finalState = filterState.copy(
                            minAmount = parsedMin,
                            maxAmount = parsedMax
                        )
                        onApply(finalState)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("filter_apply_button")
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন (OK)" else "OK",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    // ─── POPUP DIALOGS ─────────────────────────────────────────────────────────────

    // 1. Popup Calculators
    if (showFromCalc) {
        PopupCalculatorDialog(
            initialValue = minAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showFromCalc = false },
            onValueConfirmed = { value ->
                minAmountText = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                showFromCalc = false
            }
        )
    }

    if (showToCalc) {
        PopupCalculatorDialog(
            initialValue = maxAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showToCalc = false },
            onValueConfirmed = { value ->
                maxAmountText = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                showToCalc = false
            }
        )
    }

    // 2. Start & End Date Pickers
    if (showStartDatePicker) {
        val initialStartMs = if (filterState.startDateMs > 0) filterState.startDateMs else System.currentTimeMillis()
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialStartMs)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            filterState = filterState.copy(
                                startDateMs = it,
                                datePreset = LedgerDatePreset.CUSTOM
                            )
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = filterState.endDateMs)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            filterState = filterState.copy(
                                endDateMs = it,
                                datePreset = LedgerDatePreset.CUSTOM
                            )
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // 3. Category Picker Modal
    if (showCategoryPickerModal) {
        FilterCategorySelectModal(
            categories = allCategories,
            selectedCategoryId = filterState.categoryId,
            languageMode = languageMode,
            onDismiss = { showCategoryPickerModal = false },
            onSelect = { selectedId ->
                filterState = filterState.copy(categoryId = selectedId)
                showCategoryPickerModal = false
            }
        )
    }

    // 4. Account Picker Modal
    if (showAccountPickerModal) {
        FilterAccountSelectModal(
            accounts = allAccounts,
            selectedAccountId = filterState.accountId,
            languageMode = languageMode,
            onDismiss = { showAccountPickerModal = false },
            onSelect = { selectedId ->
                filterState = filterState.copy(accountId = selectedId)
                showAccountPickerModal = false
            }
        )
    }

    // 5. Label Picker Modal
    if (showLabelPickerModal) {
        val uniqueLabels = remember(allTransactions) {
            allTransactions.mapNotNull { it.transaction.referenceNo.takeIf { s -> s.isNotBlank() } }.distinct()
        }
        AlertDialog(
            onDismissRequest = { showLabelPickerModal = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "লেবেল নির্বাচন করুন" else "Select Label / Tag",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (uniqueLabels.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো লেবেল পাওয়া যায়নি" else "No labels found in records.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        uniqueLabels.forEach { lbl ->
                            val isSelected = filterState.label == lbl
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filterState = filterState.copy(label = lbl)
                                        showLabelPickerModal = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#$lbl",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLabelPickerModal = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // 6. Save Preset Dialog
    if (showSavePresetDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রিসেট সংরক্ষণ" else "Save Filter Preset",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "এই ফিল্টার কনফিগারেশনের একটি নাম দিন:" else "Enter a name for this filter configuration:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        placeholder = { Text("e.g. Monthly Expenses, Food & Fuel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            val parsedMin = minAmountText.toDoubleOrNull()
                            val parsedMax = maxAmountText.toDoubleOrNull()
                            val stateToSave = filterState.copy(minAmount = parsedMin, maxAmount = parsedMax)
                            val newPreset = SavedTransactionFilterPreset(
                                name = presetName.trim(),
                                state = stateToSave
                            )
                            savedPresets.add(newPreset)
                            TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // 7. Saved Presets Manager Dialog (Open Folder)
    if (showSavedPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedPresetsDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত ফিল্টার প্রিসেট" else "Saved Filter Presets",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    if (savedPresets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো সংরক্ষিত প্রিসেট পাওয়া যায়নি" else "No saved presets found. Save your current filter setup with the Bookmark button.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(savedPresets, key = { it.id }) { preset ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    filterState = preset.state
                                                    minAmountText = if (preset.state.minAmount != null && preset.state.minAmount > 0) preset.state.minAmount.toInt().toString() else ""
                                                    maxAmountText = if (preset.state.maxAmount != null && preset.state.maxAmount > 0) preset.state.maxAmount.toInt().toString() else ""
                                                    showSavedPresetsDialog = false
                                                }
                                        ) {
                                            Text(
                                                text = preset.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${preset.state.datePreset.displayName} • ${preset.state.rowStyle.name}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    savedPresets.remove(preset)
                                                    TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSavedPresetsDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}

/**
 * Clean UI Section wrapper with icon and title.
 */
@Composable
private fun FilterSectionWrapper(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

/**
 * Dropdown selector row with label, value, clear button, and filter option action.
 */
@Composable
private fun FilterDropdownSelectorRow(
    title: String,
    selectedValue: String,
    icon: ImageVector,
    iconTint: Color = SolidPrimary,
    isFiltered: Boolean = false,
    showFilterOptionButton: Boolean = false,
    onFilterOptionClick: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    onClick: () -> Unit,
    testTag: String = "",
    menuContent: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showFilterOptionButton && onFilterOptionClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onFilterOptionClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Options",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (isFiltered) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.outlineVariant
                ),
                color = if (isFiltered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClick() }
                    .testTag(testTag)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedValue,
                        fontSize = 13.sp,
                        fontWeight = if (isFiltered) FontWeight.Bold else FontWeight.Normal,
                        color = if (isFiltered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFiltered && onClear != null) {
                            IconButton(
                                onClick = onClear,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            menuContent?.invoke()
        }
    }
}

/**
 * Toggle Switch Row for Display Settings.
 */
@Composable
private fun DisplaySettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(width = 44.dp, height = 24.dp)
                .testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Category Selection modal for filter.
 */
@Composable
private fun FilterCategorySelectModal(
    categories: List<Category>,
    selectedCategoryId: Long?,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentCategories = remember(categories) { categories.filter { it.parentId == null } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ফিল্টার" else "Filter by Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি খুঁজুন..." else "Search Category...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategoryId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(null) }
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি (All)" else "All Categories (No Filter)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    parentCategories.forEach { parent ->
                        val subCats = categories.filter { it.parentId == parent.id }
                        val matchesSearch = searchQuery.isBlank() ||
                                parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                                parent.nameBn.contains(searchQuery, ignoreCase = true) ||
                                subCats.any { it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery, ignoreCase = true) }

                        if (matchesSearch) {
                            item {
                                val isSelected = selectedCategoryId == parent.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(parent.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconHelper.AppIcon(
                                                iconName = parent.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(parent.localizedName(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            subCats.forEach { sub ->
                                if (searchQuery.isBlank() || sub.nameEn.contains(searchQuery, ignoreCase = true) || sub.nameBn.contains(searchQuery, ignoreCase = true)) {
                                    item {
                                        val isSubSelected = selectedCategoryId == sub.id
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelect(sub.id) }
                                                .padding(start = 18.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconHelper.AppIcon(
                                                        iconName = sub.iconName,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(sub.localizedName(languageMode), fontSize = 12.sp)
                                                }
                                                if (isSubSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(14.dp))
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel") }
        }
    )
}

/**
 * Account Selection modal for filter.
 */
@Composable
private fun FilterAccountSelectModal(
    accounts: List<Account>,
    selectedAccountId: Long?,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentAccounts = remember(accounts) { accounts.filter { it.parentId == null } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ফিল্টার" else "Filter by Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search Account...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedAccountId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(null) }
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট (All)" else "All Accounts (No Filter)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    parentAccounts.forEach { parent ->
                        val subAccs = accounts.filter { it.parentId == parent.id }
                        val matchesSearch = searchQuery.isBlank() ||
                                parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                                parent.nameBn.contains(searchQuery, ignoreCase = true) ||
                                subAccs.any { it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery, ignoreCase = true) }

                        if (matchesSearch) {
                            item {
                                val isSelected = selectedAccountId == parent.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(parent.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconHelper.AppIcon(
                                                iconName = parent.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(parent.localizedName(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            subAccs.forEach { sub ->
                                if (searchQuery.isBlank() || sub.nameEn.contains(searchQuery, ignoreCase = true) || sub.nameBn.contains(searchQuery, ignoreCase = true)) {
                                    item {
                                        val isSubSelected = selectedAccountId == sub.id
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelect(sub.id) }
                                                .padding(start = 18.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconHelper.AppIcon(
                                                        iconName = sub.iconName,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(sub.localizedName(languageMode), fontSize = 12.sp)
                                                }
                                                if (isSubSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(14.dp))
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel") }
        }
    )
}
