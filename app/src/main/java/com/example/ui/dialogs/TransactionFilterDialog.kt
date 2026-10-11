package com.example.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.DropdownItem
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterGroup
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterItem
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedGroupedMultiSelectDropdown
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.components.UnifiedSingleSelectDropdown
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
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
 * Complete State representation for Transaction Filter Dialog matching BudgetFilterDialog structure.
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
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),
    val rowStyle: LedgerRowStyle = LedgerRowStyle.STANDARD,
    val displaySettings: TransactionDisplaySettings = TransactionDisplaySettings()
) {
    fun activeFilterCount(): Int {
        var count = 0
        if (searchQuery.isNotBlank()) count++
        if ((minAmount != null && minAmount > 0) || (maxAmount != null && maxAmount > 0)) count++
        if (datePreset != LedgerDatePreset.LAST_12_MONTHS) count++
        if (transactionType != null) count++
        if (categoryId != null || selectedCategoryIds.isNotEmpty()) count++
        if (accountId != null || selectedAccountIds.isNotEmpty()) count++
        if (!label.isNullOrBlank() || selectedLabels.isNotEmpty()) count++
        if (status != null || selectedStatusSet.isNotEmpty()) count++
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
    private const val PREFS_KEY = "saved_transaction_filter_presets_v2"

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

                    val catArray = JSONArray()
                    p.state.selectedCategoryIds.forEach { catArray.put(it) }
                    put("selectedCategoryIds", catArray)

                    val accArray = JSONArray()
                    p.state.selectedAccountIds.forEach { accArray.put(it) }
                    put("selectedAccountIds", accArray)

                    val lblArray = JSONArray()
                    p.state.selectedLabels.forEach { lblArray.put(it) }
                    put("selectedLabels", lblArray)

                    val stArray = JSONArray()
                    p.state.selectedStatusSet.forEach { stArray.put(it.name) }
                    put("selectedStatusSet", stArray)

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

                val selCats = mutableSetOf<Long>()
                val catJson = obj.optJSONArray("selectedCategoryIds")
                if (catJson != null) {
                    for (j in 0 until catJson.length()) selCats.add(catJson.getLong(j))
                }
                if (catId != null) selCats.add(catId)

                val selAccs = mutableSetOf<Long>()
                val accJson = obj.optJSONArray("selectedAccountIds")
                if (accJson != null) {
                    for (j in 0 until accJson.length()) selAccs.add(accJson.getLong(j))
                }
                if (accId != null) selAccs.add(accId)

                val selLbls = mutableSetOf<String>()
                val lblJson = obj.optJSONArray("selectedLabels")
                if (lblJson != null) {
                    for (j in 0 until lblJson.length()) selLbls.add(lblJson.getString(j))
                }
                if (lbl != null) selLbls.add(lbl)

                val selSts = mutableSetOf<TransactionStatus>()
                val stJson = obj.optJSONArray("selectedStatusSet")
                if (stJson != null) {
                    for (j in 0 until stJson.length()) {
                        try { selSts.add(TransactionStatus.valueOf(stJson.getString(j))) } catch (_: Exception) {}
                    }
                }
                if (status != null) selSts.add(status)

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
                    selectedCategoryIds = selCats,
                    selectedAccountIds = selAccs,
                    selectedLabels = selLbls,
                    selectedStatusSet = selSts,
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
 * Compact toggle row for Display Settings matching BudgetFilterDialog styling.
 */
@Composable
private fun BudgetCompactToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.70f)
        )
    }
}

/**
 * Transaction Filter Popup matching the exact BudgetFilterDialog layout and aesthetics.
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
    var tempFilter by remember { mutableStateOf(initialState) }

    // Text inputs for amounts
    var minAmountText by remember {
        mutableStateOf(if (initialState.minAmount != null && initialState.minAmount > 0) initialState.minAmount.toInt().toString() else "")
    }
    var maxAmountText by remember {
        mutableStateOf(if (initialState.maxAmount != null && initialState.maxAmount > 0) initialState.maxAmount.toInt().toString() else "")
    }

    // Sub-dialog states
    var showFromCalc by remember { mutableStateOf(false) }
    var showToCalc by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showSavedPresetsDialog by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedTransactionFilterPreset>().apply {
            addAll(TransactionFilterPresetsStorage.loadPresets(context))
        }
    }

    // Date Presets Dropdown Items
    val datePresetDropdownItems = remember(languageMode) {
        listOf(
            DropdownItem(
                id = LedgerDatePreset.ALL_TIME,
                title = if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
            ),
            DropdownItem(
                id = LedgerDatePreset.THIS_MONTH,
                title = if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month"
            ),
            DropdownItem(
                id = LedgerDatePreset.LAST_MONTH,
                title = if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month"
            ),
            DropdownItem(
                id = LedgerDatePreset.THIS_YEAR,
                title = if (languageMode == LanguageMode.BANGLA) "চলতি বছর" else "This Year"
            ),
            DropdownItem(
                id = LedgerDatePreset.LAST_12_MONTHS,
                title = if (languageMode == LanguageMode.BANGLA) "বিগত ১২ মাস" else "Last 12 Months"
            ),
            DropdownItem(
                id = LedgerDatePreset.TODAY,
                title = if (languageMode == LanguageMode.BANGLA) "আজ" else "Today"
            ),
            DropdownItem(
                id = LedgerDatePreset.YESTERDAY,
                title = if (languageMode == LanguageMode.BANGLA) "গতকাল" else "Yesterday"
            ),
            DropdownItem(
                id = LedgerDatePreset.THIS_WEEK,
                title = if (languageMode == LanguageMode.BANGLA) "চলতি সপ্তাহ" else "This Week"
            ),
            DropdownItem(
                id = LedgerDatePreset.LAST_WEEK,
                title = if (languageMode == LanguageMode.BANGLA) "গত সপ্তাহ" else "Last Week"
            ),
            DropdownItem(
                id = LedgerDatePreset.THIS_QUARTER,
                title = if (languageMode == LanguageMode.BANGLA) "চলতি কোয়ার্টার" else "This Quarter"
            ),
            DropdownItem(
                id = LedgerDatePreset.LAST_YEAR,
                title = if (languageMode == LanguageMode.BANGLA) "গত বছর" else "Last Year"
            ),
            DropdownItem(
                id = LedgerDatePreset.SINCE_LAST_YEAR,
                title = if (languageMode == LanguageMode.BANGLA) "গত বছর থেকে" else "Since Last Year"
            ),
            DropdownItem(
                id = LedgerDatePreset.CUSTOM,
                title = if (languageMode == LanguageMode.BANGLA) "কাস্টম সময়কাল" else "Custom Date Range"
            )
        )
    }

    // Category Filter Groups (Identical to BudgetFilterDialog)
    val categoryFilterGroups = remember(allCategories, languageMode) {
        val activeCats = allCategories.filter { it.isActive }
        val parentCats = activeCats.filter { it.parentId == null }
        val childCats = activeCats.filter { it.parentId != null }
        val childrenByParent = childCats.groupBy { it.parentId }

        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        parentCats.forEach { parent ->
            val children = childrenByParent[parent.id] ?: emptyList()
            val parentName = if (languageMode == LanguageMode.BANGLA) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
            val parentColor = try {
                if (parent.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(parent.colorHex)) else null
            } catch (_: Exception) { null }

            if (children.isNotEmpty()) {
                val items = children.map { child ->
                    val childName = if (languageMode == LanguageMode.BANGLA) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                    val childColor = try {
                        if (child.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(child.colorHex)) else parentColor
                    } catch (_: Exception) { parentColor }

                    UnifiedFilterItem(
                        id = child.id,
                        title = childName,
                        subtitle = if (child.type == CategoryType.EXPENSE) (if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense") else (if (languageMode == LanguageMode.BANGLA) "আয়" else "Income"),
                        color = childColor
                    )
                }
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "cat_parent_${parent.id}",
                        groupTitle = parentName,
                        groupSubtitle = if (parent.type == CategoryType.EXPENSE) (if (languageMode == LanguageMode.BANGLA) "ব্যয় গ্রুপ" else "Expense Group") else (if (languageMode == LanguageMode.BANGLA) "আয় গ্রুপ" else "Income Group"),
                        color = parentColor,
                        items = items
                    )
                )
            } else {
                val singleItem = UnifiedFilterItem(
                    id = parent.id,
                    title = parentName,
                    subtitle = if (parent.type == CategoryType.EXPENSE) (if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense") else (if (languageMode == LanguageMode.BANGLA) "আয়" else "Income"),
                    color = parentColor
                )
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "cat_parent_${parent.id}",
                        groupTitle = parentName,
                        color = parentColor,
                        items = listOf(singleItem)
                    )
                )
            }
        }

        val orphaned = childCats.filter { child -> parentCats.none { it.id == child.parentId } }
        if (orphaned.isNotEmpty()) {
            val items = orphaned.map { child ->
                val childName = if (languageMode == LanguageMode.BANGLA) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                UnifiedFilterItem(
                    id = child.id,
                    title = childName,
                    subtitle = if (child.type == CategoryType.EXPENSE) "Expense" else "Income"
                )
            }
            groupList.add(
                UnifiedFilterGroup(
                    groupKey = "cat_orphaned",
                    groupTitle = if (languageMode == LanguageMode.BANGLA) "অন্যান্য ক্যাটাগরি" else "Other Categories",
                    items = items
                )
            )
        }

        groupList
    }

    // Account Filter Groups (Identical to BudgetFilterDialog)
    val accountFilterGroups = remember(allAccounts, languageMode) {
        val activeAccs = allAccounts.filter { it.isActive }
        val parentAccs = activeAccs.filter { it.parentId == null }
        val childAccs = activeAccs.filter { it.parentId != null }
        val childrenByParent = childAccs.groupBy { it.parentId }

        val groupList = mutableListOf<UnifiedFilterGroup<Long>>()

        if (childAccs.isNotEmpty()) {
            parentAccs.forEach { parent ->
                val children = childrenByParent[parent.id] ?: emptyList()
                val parentName = if (languageMode == LanguageMode.BANGLA) parent.nameBn.ifBlank { parent.nameEn } else parent.nameEn
                val parentColor = try {
                    if (parent.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(parent.colorHex)) else null
                } catch (_: Exception) { null }

                if (children.isNotEmpty()) {
                    val items = children.map { child ->
                        val childName = if (languageMode == LanguageMode.BANGLA) child.nameBn.ifBlank { child.nameEn } else child.nameEn
                        UnifiedFilterItem(
                            id = child.id,
                            title = childName,
                            subtitle = child.accountNumber.ifBlank { child.type.name },
                            color = try {
                                if (child.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(child.colorHex)) else parentColor
                            } catch (_: Exception) { parentColor }
                        )
                    }
                    groupList.add(
                        UnifiedFilterGroup(
                            groupKey = "acc_parent_${parent.id}",
                            groupTitle = parentName,
                            groupSubtitle = parent.type.name,
                            color = parentColor,
                            items = items
                        )
                    )
                } else {
                    val singleItem = UnifiedFilterItem(
                        id = parent.id,
                        title = parentName,
                        subtitle = parent.accountNumber.ifBlank { parent.type.name },
                        color = parentColor
                    )
                    groupList.add(
                        UnifiedFilterGroup(
                            groupKey = "acc_parent_${parent.id}",
                            groupTitle = parentName,
                            color = parentColor,
                            items = listOf(singleItem)
                        )
                    )
                }
            }
        } else {
            val groupedByType = activeAccs.groupBy { it.type }
            groupedByType.forEach { (type, accList) ->
                val groupTitle = when (type) {
                    com.example.data.model.AccountType.ASSET -> if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets"
                    com.example.data.model.AccountType.LIABILITY -> if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities"
                    com.example.data.model.AccountType.EQUITY -> if (languageMode == LanguageMode.BANGLA) "মালিকানাস্বত্ব (Equity)" else "Equity"
                    com.example.data.model.AccountType.INCOME -> if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income"
                    com.example.data.model.AccountType.EXPENSE -> if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expense)" else "Expense"
                }
                val groupColor = when (type) {
                    com.example.data.model.AccountType.ASSET -> Color(0xFF10B981)
                    com.example.data.model.AccountType.LIABILITY -> Color(0xFFEF4444)
                    com.example.data.model.AccountType.EQUITY -> Color(0xFF6366F1)
                    com.example.data.model.AccountType.INCOME -> Color(0xFF10B981)
                    com.example.data.model.AccountType.EXPENSE -> Color(0xFFEF4444)
                }
                val items = accList.map { acc ->
                    val accName = if (languageMode == LanguageMode.BANGLA) acc.nameBn.ifBlank { acc.nameEn } else acc.nameEn
                    UnifiedFilterItem(
                        id = acc.id,
                        title = accName,
                        subtitle = acc.accountNumber.ifBlank { acc.type.name },
                        color = try {
                            if (acc.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(acc.colorHex)) else groupColor
                        } catch (_: Exception) { groupColor }
                    )
                }
                groupList.add(
                    UnifiedFilterGroup(
                        groupKey = "acc_type_${type.name}",
                        groupTitle = groupTitle,
                        color = groupColor,
                        items = items
                    )
                )
            }
        }
        groupList
    }

    // All distinct labels from transactions
    val allLabels = remember(allTransactions) {
        allTransactions.flatMap { tw ->
            val list = mutableListOf<String>()
            if (tw.transaction.referenceNo.isNotBlank()) list.add(tw.transaction.referenceNo.trim())
            tw.transaction.note.split(" ", "\n", ",").filter { it.startsWith("#") && it.length > 1 }.forEach { list.add(it.trim()) }
            list
        }.distinct().sorted()
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

    val activeCount = remember(tempFilter, minAmountText, maxAmountText) {
        val parsedMin = minAmountText.toDoubleOrNull()
        val parsedMax = maxAmountText.toDoubleOrNull()
        tempFilter.copy(minAmount = parsedMin, maxAmount = parsedMax).activeFilterCount()
    }

    // Match count computation for result badge in footer
    val resultMatchCount = remember(tempFilter, minAmountText, maxAmountText, allTransactions) {
        val parsedMin = minAmountText.toDoubleOrNull() ?: 0.0
        val parsedMax = maxAmountText.toDoubleOrNull() ?: Double.MAX_VALUE
        val bounds = computePresetDateBounds(tempFilter.datePreset)
        val startMs = if (tempFilter.datePreset == LedgerDatePreset.CUSTOM) tempFilter.startDateMs else bounds.first
        val endMs = if (tempFilter.datePreset == LedgerDatePreset.CUSTOM) DateUtils.getEndOfDay(tempFilter.endDateMs) else bounds.second
        val q = tempFilter.searchQuery.trim().lowercase()

        allTransactions.count { tw ->
            val tx = tw.transaction
            val passType = tempFilter.transactionType == null || tx.type == tempFilter.transactionType
            val passDate = tx.dateEpochMs in startMs..endMs
            val amt = kotlin.math.abs(tx.amount)
            val passAmt = amt >= parsedMin && amt <= parsedMax
            val passAcc = tempFilter.selectedAccountIds.isEmpty() ||
                (tx.debitAccountId != null && tempFilter.selectedAccountIds.contains(tx.debitAccountId)) ||
                (tx.creditAccountId != null && tempFilter.selectedAccountIds.contains(tx.creditAccountId)) ||
                (tempFilter.accountId != null && (tx.debitAccountId == tempFilter.accountId || tx.creditAccountId == tempFilter.accountId))
            val passCat = tempFilter.selectedCategoryIds.isEmpty() ||
                (tx.categoryId != null && tempFilter.selectedCategoryIds.contains(tx.categoryId)) ||
                (tx.subCategoryId != null && tempFilter.selectedCategoryIds.contains(tx.subCategoryId)) ||
                (tempFilter.categoryId != null && (tx.categoryId == tempFilter.categoryId || tx.subCategoryId == tempFilter.categoryId))
            val passLbl = (tempFilter.selectedLabels.isEmpty() && tempFilter.label == null) ||
                tempFilter.selectedLabels.any { tx.referenceNo.equals(it, ignoreCase = true) || tx.note.contains(it, ignoreCase = true) } ||
                (tempFilter.label != null && (tx.referenceNo.equals(tempFilter.label, ignoreCase = true) || tx.note.contains(tempFilter.label!!, ignoreCase = true)))
            val passSt = (tempFilter.selectedStatusSet.isEmpty() && tempFilter.status == null) ||
                tempFilter.selectedStatusSet.contains(tx.status) ||
                (tempFilter.status != null && tx.status == tempFilter.status)
            val passSearch = q.isEmpty() ||
                tx.note.lowercase().contains(q) ||
                tx.payeeOrPayer.lowercase().contains(q) ||
                tx.referenceNo.lowercase().contains(q)

            passType && passDate && passAmt && passAcc && passCat && passLbl && passSt && passSearch
        }
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "transaction_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Header
            UnifiedFilterHeader(
                title = if (languageMode == LanguageMode.BANGLA) "লেনদেন ফিল্টার" else "Transaction Filter",
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = TransactionFilterState()
                    minAmountText = ""
                    maxAmountText = ""
                    Toast.makeText(
                        context,
                        if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSavePreset = { showSavePresetDialog = true },
                onOpenPreset = { showSavedPresetsDialog = true },
                onDismiss = onDismiss
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Date Range Dropdown
                UnifiedFilterSection(
                    icon = Icons.Default.DateRange,
                    title = if (languageMode == LanguageMode.BANGLA) "সময়কাল" else "Date Range"
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "সময়কাল নির্বাচন করুন" else "Select Date Range",
                        items = datePresetDropdownItems,
                        selectedId = tempFilter.datePreset,
                        onSelectionChanged = { tempFilter = tempFilter.copy(datePreset = it) },
                        leadingIcon = Icons.Default.DateRange,
                        languageMode = languageMode
                    )

                    // Custom Date Range
                    if (tempFilter.datePreset == LedgerDatePreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(6.dp))
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
                                        onClick = { showStartDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (tempFilter.startDateMs > 0) DateUtils.formatDate(tempFilter.startDateMs, languageMode) else "Start Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showEndDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = DateUtils.formatDate(tempFilter.endDateMs, languageMode),
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

                // 2. Transaction Type Section
                UnifiedFilterSection(
                    icon = Icons.Default.FilterList,
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেনের ধরন" else "Transaction Type"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val typeOptions = listOf(
                            null to (if (languageMode == LanguageMode.BANGLA) "সকল" else "All"),
                            TransactionType.EXPENSE to (if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense"),
                            TransactionType.INCOME to (if (languageMode == LanguageMode.BANGLA) "আয়" else "Income"),
                            TransactionType.TRANSFER to (if (languageMode == LanguageMode.BANGLA) "স্থানান্তর" else "Transfer")
                        )
                        typeOptions.forEach { (type, title) ->
                            val isSelected = tempFilter.transactionType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempFilter = tempFilter.copy(transactionType = type) },
                                label = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) {
                                            when (type) {
                                                TransactionType.EXPENSE -> SolidExpense
                                                TransactionType.INCOME -> SolidIncome
                                                TransactionType.TRANSFER -> SolidTransfer
                                                null -> MaterialTheme.colorScheme.primary
                                            }
                                        } else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Categories & Accounts side-by-side in compact 2-column row (Identical to BudgetFilterDialog)
                UnifiedFilterSection(
                    icon = Icons.Default.Category,
                    title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ও অ্যাকাউন্ট" else "Categories & Accounts"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedGroupedMultiSelectDropdown(
                                label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories",
                                groups = categoryFilterGroups,
                                selectedIds = tempFilter.selectedCategoryIds,
                                onSelectionChanged = { tempFilter = tempFilter.copy(selectedCategoryIds = it) },
                                placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.Category,
                                languageMode = languageMode
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedGroupedMultiSelectDropdown(
                                label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Accounts",
                                groups = accountFilterGroups,
                                selectedIds = tempFilter.selectedAccountIds,
                                onSelectionChanged = { tempFilter = tempFilter.copy(selectedAccountIds = it) },
                                placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.AccountBalance,
                                languageMode = languageMode
                            )
                        }
                    }
                }

                // 4. Status & Labels side-by-side in compact 2-column row (Identical to BudgetFilterDialog)
                UnifiedFilterSection(
                    icon = Icons.Default.Check,
                    title = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস ও লেবেল" else "Status & Labels"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            UnifiedMultiSelectDropdown(
                                label = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status",
                                items = statusDropdownItems,
                                selectedIds = selectedStatusIds,
                                onSelectionChanged = { newIds ->
                                    val selectedStatuses = TransactionStatus.entries.filter { newIds.contains(it.ordinal.toLong()) }.toSet()
                                    tempFilter = tempFilter.copy(selectedStatusSet = selectedStatuses)
                                },
                                placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                                leadingIcon = Icons.Default.Check,
                                languageMode = languageMode
                            )
                        }

                        if (allLabels.isNotEmpty()) {
                            Box(modifier = Modifier.weight(1f)) {
                                UnifiedMultiSelectDropdown(
                                    label = if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels",
                                    items = labelDropdownItems,
                                    selectedIds = selectedLabelIds,
                                    onSelectionChanged = { newIds ->
                                        val selectedStrings = allLabels.filter { newIds.contains(it.hashCode().toLong()) }.toSet()
                                        tempFilter = tempFilter.copy(selectedLabels = selectedStrings)
                                    },
                                    placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল)" else "(All)",
                                    leadingIcon = Icons.AutoMirrored.Filled.Label,
                                    languageMode = languageMode
                                )
                            }
                        }
                    }
                }

                // 5. Amount Range
                UnifiedFilterSection(
                    icon = Icons.Default.Calculate,
                    title = if (languageMode == LanguageMode.BANGLA) "পরিমাণের সীমা" else "Amount Range"
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
                            modifier = Modifier.weight(1f)
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
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickAmounts = listOf(1000.0, 5000.0, 10000.0)
                        quickAmounts.forEach { amt ->
                            val isCurrent = minAmountText == amt.toInt().toString()
                            FilterChip(
                                selected = isCurrent,
                                onClick = {
                                    minAmountText = if (isCurrent) "" else amt.toInt().toString()
                                },
                                label = { Text("≥ ৳${amt.toInt()}", fontSize = 10.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 6. Search
                UnifiedFilterSection(
                    icon = Icons.Default.Search,
                    title = if (languageMode == LanguageMode.BANGLA) "অনুসন্ধান" else "Search"
                ) {
                    OutlinedTextField(
                        value = tempFilter.searchQuery,
                        onValueChange = { tempFilter = tempFilter.copy(searchQuery = it) },
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
                            if (tempFilter.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { tempFilter = tempFilter.copy(searchQuery = "") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 7. Display & Options
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন ও বিকল্প" else "Display & Options"
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
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            BudgetCompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "মোট পরিমাণ প্রদর্শন" else "Show Total Amount",
                                checked = tempFilter.displaySettings.showTotalAmount,
                                onCheckedChange = {
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showTotalAmount = it)
                                    )
                                }
                            )
                            BudgetCompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "মোটে স্থানান্তর অন্তর্ভুক্তি" else "Show Transfers in Total",
                                checked = tempFilter.displaySettings.showTransfersInTotal,
                                onCheckedChange = {
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showTransfersInTotal = it)
                                    )
                                }
                            )
                            BudgetCompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "চলতি অ্যাকাউন্ট ব্যালেন্স প্রদর্শন" else "Show Account Balance",
                                checked = tempFilter.displaySettings.showAccountBalance,
                                onCheckedChange = {
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showAccountBalance = it)
                                    )
                                }
                            )
                            BudgetCompactToggleRow(
                                title = if (languageMode == LanguageMode.BANGLA) "পুরোনো তারিখ প্রথমে প্রদর্শন" else "Show Oldest Date First",
                                checked = tempFilter.displaySettings.showOldestDateFirst,
                                onCheckedChange = {
                                    tempFilter = tempFilter.copy(
                                        displaySettings = tempFilter.displaySettings.copy(showOldestDateFirst = it)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ভিউ স্টাইল" else "View Style",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    LedgerRowStyle.STANDARD to "Standard",
                                    LedgerRowStyle.COMPACT to "Compact",
                                    LedgerRowStyle.DETAILED to "Detailed"
                                ).forEach { (style, name) ->
                                    val isSelected = tempFilter.rowStyle == style
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { tempFilter = tempFilter.copy(rowStyle = style) },
                                        label = { Text(name, fontSize = 11.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                        } else null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Unified Footer
            UnifiedFilterFooter(
                activeCount = activeCount,
                matchCount = resultMatchCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = TransactionFilterState()
                    minAmountText = ""
                    maxAmountText = ""
                },
                onDismiss = onDismiss,
                onApply = {
                    val parsedMin = minAmountText.toDoubleOrNull()
                    val parsedMax = maxAmountText.toDoubleOrNull()
                    onApply(tempFilter.copy(minAmount = parsedMin, maxAmount = parsedMax))
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Sub-dialogs: Calculators, DatePickers, and Presets
    // -------------------------------------------------------------------------

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

    // 2. Custom Start & End Date Pickers
    if (showStartDatePicker) {
        val initialStartMs = if (tempFilter.startDateMs > 0) tempFilter.startDateMs else System.currentTimeMillis()
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialStartMs)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            tempFilter = tempFilter.copy(
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
        val dateState = rememberDatePickerState(initialSelectedDateMillis = tempFilter.endDateMs)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            tempFilter = tempFilter.copy(
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

    // 3. Save Preset Dialog
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
                            val parsedMin = minAmountText.toDoubleOrNull()
                            val parsedMax = maxAmountText.toDoubleOrNull()
                            val stateToSave = tempFilter.copy(minAmount = parsedMin, maxAmount = parsedMax)
                            val newPreset = SavedTransactionFilterPreset(
                                name = presetNameInput.trim(),
                                state = stateToSave
                            )
                            savedPresets.add(newPreset)
                            TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                            showSavePresetDialog = false
                            Toast.makeText(
                                context,
                                if (languageMode == LanguageMode.BANGLA) "প্রিসেট সংরক্ষিত হয়েছে" else "Preset '${presetNameInput.trim()}' saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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

    // 4. Open Saved Presets Dialog
    if (showSavedPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedPresetsDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত ফিল্টারসমূহ" else "Saved Filter Presets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট প্রিসেটসমূহ" else "Built-in Presets",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempFilter = TransactionFilterState(datePreset = LedgerDatePreset.THIS_MONTH)
                                minAmountText = ""
                                maxAmountText = ""
                                showSavedPresetsDialog = false
                                Toast.makeText(context, "Current Month preset loaded", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                            Text(if (languageMode == LanguageMode.BANGLA) "চলতি মাসের লেনদেন" else "This Month's Transactions", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (languageMode == LanguageMode.BANGLA) "চলতি মাস • সকল ক্যাটাগরি ও অ্যাকাউন্ট" else "This Month • All Categories & Accounts", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempFilter = TransactionFilterState(
                                    datePreset = LedgerDatePreset.ALL_TIME,
                                    transactionType = TransactionType.EXPENSE,
                                    minAmount = 1000.0
                                )
                                minAmountText = "1000"
                                maxAmountText = ""
                                showSavedPresetsDialog = false
                                Toast.makeText(context, "High Expense preset loaded", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বেশি ব্যয়ের লেনদেন" else "High Expense (> ৳1,000)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (languageMode == LanguageMode.BANGLA) "শুধু ব্যয় • ১,০০০ টাকার বেশি" else "Expense Only • Amount >= 1,000", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (savedPresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "আপনার সংরক্ষিত প্রিসেটসমূহ" else "Your Saved Presets",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        savedPresets.forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempFilter = p.state
                                        minAmountText = if (p.state.minAmount != null && p.state.minAmount > 0) p.state.minAmount.toInt().toString() else ""
                                        maxAmountText = if (p.state.maxAmount != null && p.state.maxAmount > 0) p.state.maxAmount.toInt().toString() else ""
                                        showSavedPresetsDialog = false
                                        Toast.makeText(context, "Preset '${p.name}' loaded", Toast.LENGTH_SHORT).show()
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
                                        Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${p.state.datePreset.displayName} • Active: ${p.state.activeFilterCount()}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            savedPresets.remove(p)
                                            TransactionFilterPresetsStorage.savePresets(context, savedPresets)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showSavedPresetsDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}
