package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.DropdownItem
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.components.UnifiedSingleSelectDropdown
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.util.Calendar

enum class AggregatedDatePreset(val labelEn: String, val labelBn: String) {
    THIS_MONTH("This Month", "চলতি মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    THIS_WEEK("This Week", "এই সপ্তাহ"),
    TODAY("Today", "আজকে"),
    YESTERDAY("Yesterday", "গতকাল"),
    LAST_30_DAYS("Last 30 Days", "গত ৩০ দিন"),
    LAST_90_DAYS("Last 90 Days", "গত ৯০ দিন"),
    THIS_YEAR("This Year", "এই বছর"),
    LAST_YEAR("Last Year", "গত বছর"),
    ALL_TIME("All Time", "সব সময়"),
    CUSTOM("Custom Range", "নির্দিষ্ট সময়সীমা")
}

enum class AggregatedSortOrder(val titleEn: String, val titleBn: String) {
    AMOUNT_DESC("Amount: High → Low", "পরিমাণ: বেশি → কম"),
    AMOUNT_ASC("Amount: Low → High", "পরিমাণ: কম → বেশি"),
    COUNT_DESC("Count: Most Frequent", "লেনদেন: বেশি → কম"),
    COUNT_ASC("Count: Least Frequent", "লেনদেন: কম → বেশি"),
    AVG_DESC("Average: High → Low", "গড়: বেশি → কম"),
    AVG_ASC("Average: Low → High", "গড়: কম → বেশি"),
    NAME_ASC("Name: A → Z", "নাম: A → Z"),
    NAME_DESC("Name: Z → A", "নাম: Z → A"),
    RECENT_DATE("Recent Activity", "সাম্প্রতিক লেনদেন")
}

data class AggregatedFilterState(
    val datePreset: AggregatedDatePreset = AggregatedDatePreset.THIS_MONTH,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,
    val transactionType: TransactionType? = null,
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedStatuses: Set<TransactionStatus> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val sortOrder: AggregatedSortOrder = AggregatedSortOrder.AMOUNT_DESC,
    val excludeZeroAmounts: Boolean = true
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (datePreset != AggregatedDatePreset.THIS_MONTH) count++
            if (transactionType != null) count++
            if (selectedAccountIds.isNotEmpty()) count++
            if (selectedCategoryIds.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            if (minAmount != null && minAmount > 0.0) count++
            if (maxAmount != null && maxAmount > 0.0) count++
            if (sortOrder != AggregatedSortOrder.AMOUNT_DESC) count++
            if (!excludeZeroAmounts) count++
            return count
        }

    val isFilterActive: Boolean
        get() = activeFilterCount > 0

    fun calculateDateRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        return when (datePreset) {
            AggregatedDatePreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            AggregatedDatePreset.TODAY -> {
                val start = DateUtils.getStartOfDay(now)
                val end = DateUtils.getEndOfDay(now)
                Pair(start, end)
            }
            AggregatedDatePreset.YESTERDAY -> {
                val yCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val start = DateUtils.getStartOfDay(yCal.timeInMillis)
                val end = DateUtils.getEndOfDay(yCal.timeInMillis)
                Pair(start, end)
            }
            AggregatedDatePreset.THIS_WEEK -> {
                val wCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    firstDayOfWeek = DateUtils.activeFirstDayOfWeek
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                val start = DateUtils.getStartOfDay(wCal.timeInMillis)
                val end = start + (7L * 24L * 60L * 60L * 1000L) - 1L
                Pair(start, end)
            }
            AggregatedDatePreset.THIS_MONTH -> {
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
            }
            AggregatedDatePreset.LAST_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, -1)
                val prevYear = cal.get(Calendar.YEAR)
                val prevMonth = cal.get(Calendar.MONTH) + 1
                Pair(DateUtils.getStartOfMonth(prevYear, prevMonth), DateUtils.getEndOfMonth(prevYear, prevMonth))
            }
            AggregatedDatePreset.LAST_30_DAYS -> {
                val endOfToday = DateUtils.getStartOfDay(now) + 86400000L - 1L
                Pair(now - (30L * 24L * 60L * 60L * 1000L), endOfToday)
            }
            AggregatedDatePreset.LAST_90_DAYS -> {
                val endOfToday = DateUtils.getStartOfDay(now) + 86400000L - 1L
                Pair(now - (90L * 24L * 60L * 60L * 1000L), endOfToday)
            }
            AggregatedDatePreset.THIS_YEAR -> {
                val year = cal.get(Calendar.YEAR)
                Pair(DateUtils.getStartOfMonth(year, 1), DateUtils.getEndOfMonth(year, 12))
            }
            AggregatedDatePreset.LAST_YEAR -> {
                val year = cal.get(Calendar.YEAR) - 1
                Pair(DateUtils.getStartOfMonth(year, 1), DateUtils.getEndOfMonth(year, 12))
            }
            AggregatedDatePreset.CUSTOM -> {
                val start = customStartDateMs ?: 0L
                val end = customEndDateMs ?: Long.MAX_VALUE
                Pair(start, end)
            }
        }
    }

    fun buildFilterSummary(languageMode: LanguageMode): String {
        val parts = mutableListOf<String>()
        when (datePreset) {
            AggregatedDatePreset.ALL_TIME -> {}
            AggregatedDatePreset.TODAY -> parts.add(if (languageMode == LanguageMode.BANGLA) "আজকে" else "Today")
            AggregatedDatePreset.YESTERDAY -> parts.add(if (languageMode == LanguageMode.BANGLA) "গতকাল" else "Yesterday")
            AggregatedDatePreset.THIS_WEEK -> parts.add(if (languageMode == LanguageMode.BANGLA) "এই সপ্তাহ" else "This Week")
            AggregatedDatePreset.THIS_MONTH -> parts.add(if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month")
            AggregatedDatePreset.LAST_MONTH -> parts.add(if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month")
            AggregatedDatePreset.LAST_30_DAYS -> parts.add(if (languageMode == LanguageMode.BANGLA) "বিগত ৩০ দিন" else "Last 30 Days")
            AggregatedDatePreset.LAST_90_DAYS -> parts.add(if (languageMode == LanguageMode.BANGLA) "বিগত ৯০ দিন" else "Last 90 Days")
            AggregatedDatePreset.THIS_YEAR -> parts.add(if (languageMode == LanguageMode.BANGLA) "চলতি বছর" else "This Year")
            AggregatedDatePreset.LAST_YEAR -> parts.add(if (languageMode == LanguageMode.BANGLA) "গত বছর" else "Last Year")
            AggregatedDatePreset.CUSTOM -> {
                if (customStartDateMs != null && customEndDateMs != null) {
                    val s = DateUtils.formatDate(customStartDateMs, languageMode)
                    val e = DateUtils.formatDate(customEndDateMs, languageMode)
                    parts.add("$s - $e")
                }
            }
        }
        if (transactionType != null) {
            val typeStr = when (transactionType) {
                TransactionType.EXPENSE -> if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense"
                TransactionType.INCOME -> if (languageMode == LanguageMode.BANGLA) "আয়" else "Income"
                TransactionType.TRANSFER -> if (languageMode == LanguageMode.BANGLA) "স্থানান্তর" else "Transfer"
            }
            parts.add(typeStr)
        }
        if (selectedAccountIds.isNotEmpty()) {
            parts.add(if (languageMode == LanguageMode.BANGLA) "${selectedAccountIds.size}টি একাউন্ট" else "${selectedAccountIds.size} Accounts")
        }
        if (selectedCategoryIds.isNotEmpty()) {
            parts.add(if (languageMode == LanguageMode.BANGLA) "${selectedCategoryIds.size}টি ক্যাটাগরি" else "${selectedCategoryIds.size} Categories")
        }
        if (selectedStatuses.isNotEmpty()) {
            parts.add(selectedStatuses.joinToString(",") { if (languageMode == LanguageMode.BANGLA) it.titleBn else it.titleEn })
        }
        if (minAmount != null && minAmount > 0) {
            parts.add("≥ ${LanguageHelper.formatCurrency(minAmount, languageMode)}")
        }
        if (maxAmount != null && maxAmount > 0) {
            parts.add("≤ ${LanguageHelper.formatCurrency(maxAmount, languageMode)}")
        }
        return parts.joinToString(" • ")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AggregatedFilterDialog(
    title: String,
    currentState: AggregatedFilterState,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (AggregatedFilterState) -> Unit
) {
    var tempState by remember { mutableStateOf(currentState) }
    var minAmountText by remember { mutableStateOf(tempState.minAmount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var maxAmountText by remember { mutableStateOf(tempState.maxAmount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }

    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

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

    val datePresetDropdownItems = remember(languageMode) {
        AggregatedDatePreset.entries.map { preset ->
            DropdownItem(
                id = preset,
                title = if (languageMode == LanguageMode.BANGLA) preset.labelBn else preset.labelEn
            )
        }
    }

    val statusDropdownItems = remember(languageMode) {
        TransactionStatus.entries.map { st ->
            DropdownItem(
                id = st,
                title = if (languageMode == LanguageMode.BANGLA) st.titleBn else st.titleEn
            )
        }
    }

    val sortOrderDropdownItems = remember(languageMode) {
        AggregatedSortOrder.entries.map { order ->
            DropdownItem(
                id = order,
                title = if (languageMode == LanguageMode.BANGLA) order.titleBn else order.titleEn
            )
        }
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "aggregated_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Dialog Header
            UnifiedFilterHeader(
                title = title,
                activeCount = tempState.activeFilterCount,
                languageMode = languageMode,
                onReset = {
                    tempState = AggregatedFilterState()
                    minAmountText = ""
                    maxAmountText = ""
                },
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
                // SECTION 1: Date Range Presets
                UnifiedFilterSection(
                    icon = Icons.Default.DateRange,
                    title = if (languageMode == LanguageMode.BANGLA) "সময়কাল" else "Date Period"
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "সময়কাল নির্বাচন করুন" else "Select Date Period",
                        items = datePresetDropdownItems,
                        selectedId = tempState.datePreset,
                        onSelectionChanged = { tempState = tempState.copy(datePreset = it) },
                        leadingIcon = Icons.Default.DateRange,
                        languageMode = languageMode
                    )

                    // Custom Date Range Pickers if CUSTOM is active
                    if (tempState.datePreset == AggregatedDatePreset.CUSTOM) {
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
                                        onClick = { showCustomStartDatePicker = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempState.customStartDateMs?.let { DateUtils.formatDate(it, languageMode) } ?: "Start Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showCustomEndDatePicker = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempState.customEndDateMs?.let { DateUtils.formatDate(it, languageMode) } ?: "End Date",
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

                // SECTION 2: Transaction Flow / Type
                UnifiedFilterSection(
                    icon = Icons.Default.SwapHoriz,
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেনের ধরন" else "Transaction Flow"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isAllSelected = tempState.transactionType == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { tempState = tempState.copy(transactionType = null) },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "সকল" else "All", fontSize = 11.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        val isExpenseSelected = tempState.transactionType == TransactionType.EXPENSE
                        FilterChip(
                            selected = isExpenseSelected,
                            onClick = { tempState = tempState.copy(transactionType = TransactionType.EXPENSE) },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidExpense.copy(alpha = 0.16f),
                                selectedLabelColor = SolidExpense
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        val isIncomeSelected = tempState.transactionType == TransactionType.INCOME
                        FilterChip(
                            selected = isIncomeSelected,
                            onClick = { tempState = tempState.copy(transactionType = TransactionType.INCOME) },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "আয়" else "Income", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidIncome.copy(alpha = 0.16f),
                                selectedLabelColor = SolidIncome
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // SECTION 3: Accounts Multi-Select Dropdown
                if (accounts.isNotEmpty()) {
                    UnifiedFilterSection(
                        icon = Icons.Default.AccountBalance,
                        title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Accounts"
                    ) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Accounts",
                            items = accountDropdownItems,
                            selectedIds = tempState.selectedAccountIds,
                            onSelectionChanged = { tempState = tempState.copy(selectedAccountIds = it) },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল অ্যাকাউন্ট)" else "(All Accounts)",
                            leadingIcon = Icons.Default.AccountBalance,
                            languageMode = languageMode
                        )
                    }
                }

                // SECTION 4: Categories Multi-Select Dropdown
                if (categories.isNotEmpty()) {
                    UnifiedFilterSection(
                        icon = Icons.Default.Category,
                        title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories"
                    ) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Categories",
                            items = categoryDropdownItems,
                            selectedIds = tempState.selectedCategoryIds,
                            onSelectionChanged = { tempState = tempState.copy(selectedCategoryIds = it) },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল ক্যাটাগরি)" else "(All Categories)",
                            leadingIcon = Icons.Default.Category,
                            languageMode = languageMode
                        )
                    }
                }

                // SECTION 5: Transaction Status Dropdown
                UnifiedFilterSection(
                    icon = Icons.Default.CheckCircle,
                    title = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Transaction Status"
                ) {
                    UnifiedMultiSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস নির্বাচন করুন" else "Select Statuses",
                        items = statusDropdownItems,
                        selectedIds = tempState.selectedStatuses,
                        onSelectionChanged = { tempState = tempState.copy(selectedStatuses = it) },
                        placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল স্ট্যাটাস)" else "(All Statuses)",
                        leadingIcon = Icons.Default.CheckCircle,
                        languageMode = languageMode
                    )
                }

                // SECTION 6: Amount Range (Min & Max)
                UnifiedFilterSection(
                    icon = Icons.Default.Payments,
                    title = if (languageMode == LanguageMode.BANGLA) "পরিমাণের সীমা" else "Amount Range (৳)"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmountText,
                            onValueChange = { input ->
                                minAmountText = input.filter { it.isDigit() || it == '.' }
                                tempState = tempState.copy(minAmount = minAmountText.toDoubleOrNull())
                            },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বনিম্ন (৳)" else "Min ৳", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxAmountText,
                            onValueChange = { input ->
                                maxAmountText = input.filter { it.isDigit() || it == '.' }
                                tempState = tempState.copy(maxAmount = maxAmountText.toDoubleOrNull())
                            },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ (৳)" else "Max ৳", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // SECTION 7: Sort Order Dropdown
                UnifiedFilterSection(
                    icon = Icons.Default.Sort,
                    title = if (languageMode == LanguageMode.BANGLA) "সাজানোর ক্রম" else "Sort Order"
                ) {
                    UnifiedSingleSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "সাজানোর ক্রম নির্বাচন করুন" else "Select Sort Order",
                        items = sortOrderDropdownItems,
                        selectedId = tempState.sortOrder,
                        onSelectionChanged = { tempState = tempState.copy(sortOrder = it) },
                        leadingIcon = Icons.Default.Sort,
                        languageMode = languageMode
                    )
                }

                // SECTION 8: Display Options
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য অপশন" else "Display Options"
                ) {
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude Zero Amounts",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোন লেনদেন না থাকা আইটেম লুকান" else "Hide items with 0 total in this period",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = tempState.excludeZeroAmounts,
                                onCheckedChange = { tempState = tempState.copy(excludeZeroAmounts = it) }
                            )
                        }
                    }
                }
            }

            // Unified Sticky Footer
            UnifiedFilterFooter(
                activeCount = tempState.activeFilterCount,
                languageMode = languageMode,
                onReset = {
                    tempState = AggregatedFilterState()
                    minAmountText = ""
                    maxAmountText = ""
                },
                onDismiss = onDismiss,
                onApply = {
                    val minVal = minAmountText.toDoubleOrNull()
                    val maxVal = maxAmountText.toDoubleOrNull()
                    val finalState = tempState.copy(minAmount = minVal, maxAmount = maxVal)
                    onApply(finalState)
                }
            )
        }
    }

    // Custom Date Pickers
    if (showCustomStartDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = tempState.customStartDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        tempState = tempState.copy(customStartDateMs = it)
                    }
                    showCustomStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = dateState,
                title = { Text(if (languageMode == LanguageMode.BANGLA) "শুরুর তারিখ নির্বাচন" else "Select Start Date", modifier = Modifier.padding(16.dp)) }
            )
        }
    }

    if (showCustomEndDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = tempState.customEndDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val endOfDay = DateUtils.getEndOfDay(it)
                        tempState = tempState.copy(customEndDateMs = endOfDay)
                    }
                    showCustomEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = dateState,
                title = { Text(if (languageMode == LanguageMode.BANGLA) "শেষ তারিখ নির্বাচন" else "Select End Date", modifier = Modifier.padding(16.dp)) }
            )
        }
    }
}

@Composable
private fun FilterSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
