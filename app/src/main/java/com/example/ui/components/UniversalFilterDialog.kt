package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FilterDateRange(val labelEn: String, val labelBn: String) {
    ALL_TIME("All Time", "সব সময়"),
    TODAY("Today", "আজকে"),
    YESTERDAY("Yesterday", "গতকাল"),
    THIS_WEEK("This Week", "এই সপ্তাহ"),
    THIS_MONTH("This Month", "এই মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    LAST_30_DAYS("Last 30 Days", "গত ৩০ দিন"),
    LAST_90_DAYS("Last 90 Days", "গত ৯০ দিন"),
    THIS_YEAR("This Year", "এই বছর"),
    CUSTOM("Custom Range", "নির্দিষ্ট সময়সীমা")
}

data class UniversalFilterState(
    val selectedTypes: Set<TransactionType> = emptySet(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedAccountTypes: Set<AccountType> = emptySet(),
    val selectedStatuses: Set<TransactionStatus> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val dateRangeType: FilterDateRange = FilterDateRange.ALL_TIME,
    val customStartDateEpochMs: Long? = null,
    val customEndDateEpochMs: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val onlyOverBudget: Boolean = false,
    val onlyWithBudget: Boolean = false
) {
    val isActive: Boolean
        get() = selectedTypes.isNotEmpty() ||
                selectedCategoryIds.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedAccountTypes.isNotEmpty() ||
                selectedStatuses.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                dateRangeType != FilterDateRange.ALL_TIME ||
                customStartDateEpochMs != null ||
                customEndDateEpochMs != null ||
                minAmount != null ||
                maxAmount != null ||
                onlyOverBudget ||
                onlyWithBudget

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedTypes.isNotEmpty()) count++
            if (selectedCategoryIds.isNotEmpty()) count++
            if (selectedAccountIds.isNotEmpty()) count++
            if (selectedAccountTypes.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            if (selectedLabels.isNotEmpty()) count++
            if (dateRangeType != FilterDateRange.ALL_TIME) count++
            if (minAmount != null || maxAmount != null) count++
            if (onlyOverBudget || onlyWithBudget) count++
            return count
        }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UniversalFilterDialog(
    initialState: UniversalFilterState = UniversalFilterState(),
    allCategories: List<Category> = emptyList(),
    allAccounts: List<Account> = emptyList(),
    allLabels: List<String> = emptyList(),
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    supportedTabs: List<String> = listOf("TYPES", "DATE", "CATEGORIES", "ACCOUNTS", "AMOUNT", "MORE"),
    onApply: (UniversalFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var state by remember { mutableStateOf(initialState) }
    var selectedTabIdx by remember { mutableIntStateOf(0) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val tabTitles = remember(supportedTabs, languageMode) {
        supportedTabs.map { tab ->
            when (tab) {
                "TYPES" -> if (languageMode == LanguageMode.BANGLA) "ধরন" else "Types"
                "DATE" -> if (languageMode == LanguageMode.BANGLA) "তারিখ" else "Date"
                "CATEGORIES" -> if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories"
                "ACCOUNTS" -> if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Accounts"
                "AMOUNT" -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ" else "Amount"
                "MORE" -> if (languageMode == LanguageMode.BANGLA) "আরও" else "More"
                else -> tab
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("universal_filter_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার নির্বাচন করুন" else "Filter Selection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.activeFilterCount > 0) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "${state.activeFilterCount}টি ফিল্টার সক্রিয়"
                                    else
                                        "${state.activeFilterCount} active filter(s)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isActive) {
                            TextButton(
                                onClick = { state = UniversalFilterState() },
                                modifier = Modifier.testTag("filter_reset_btn")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset", fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                HorizontalDivider()

                // Filter Category Tabs
                if (supportedTabs.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIdx.coerceIn(0, supportedTabs.size - 1),
                        edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTabIdx in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        supportedTabs.forEachIndexed { index, tabKey ->
                            val isSelected = selectedTabIdx == index
                            val hasBadge = when (tabKey) {
                                "TYPES" -> state.selectedTypes.isNotEmpty()
                                "DATE" -> state.dateRangeType != FilterDateRange.ALL_TIME
                                "CATEGORIES" -> state.selectedCategoryIds.isNotEmpty()
                                "ACCOUNTS" -> state.selectedAccountIds.isNotEmpty() || state.selectedAccountTypes.isNotEmpty()
                                "AMOUNT" -> state.minAmount != null || state.maxAmount != null
                                "MORE" -> state.selectedStatuses.isNotEmpty() || state.selectedLabels.isNotEmpty() || state.onlyOverBudget || state.onlyWithBudget
                                else -> false
                            }

                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIdx = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tabTitles.getOrElse(index) { tabKey },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                        if (hasBadge) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // Filter Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val currentTabKey = supportedTabs.getOrElse(selectedTabIdx) { "TYPES" }

                    when (currentTabKey) {
                        "TYPES" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "লেনদেনের ধরন (মাল্টিপল নির্বাচন করুন)" else "Transaction Types (Multi-select)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                val types = listOf(
                                    Triple(TransactionType.EXPENSE, if (languageMode == LanguageMode.BANGLA) "খরচ (Expense)" else "Expense", SolidExpense),
                                    Triple(TransactionType.INCOME, if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income", SolidIncome),
                                    Triple(TransactionType.TRANSFER, if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার (Transfer)" else "Transfer", SolidTransfer)
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    types.forEach { (type, label, color) ->
                                        val isSelected = state.selectedTypes.contains(type)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                val next = state.selectedTypes.toMutableSet()
                                                if (isSelected) next.remove(type) else next.add(type)
                                                state = state.copy(selectedTypes = next)
                                            },
                                            label = {
                                                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            },
                                            leadingIcon = {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                    )
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = color.copy(alpha = 0.18f),
                                                selectedLabelColor = color
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { state = state.copy(selectedTypes = TransactionType.entries.toSet()) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "সব ধরন নির্বাচন" else "Select All Types", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { state = state.copy(selectedTypes = emptySet()) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear Types", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        "DATE" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সময়সীমা নির্বাচন করুন" else "Select Date Range",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterDateRange.entries.forEach { range ->
                                        val isSelected = state.dateRangeType == range
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { state = state.copy(dateRangeType = range) },
                                            label = {
                                                Text(
                                                    if (languageMode == LanguageMode.BANGLA) range.labelBn else range.labelEn,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                    }
                                }

                                if (state.dateRangeType == FilterDateRange.CUSTOM) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "কাস্টম সময়কাল" else "Custom Date Period",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { showStartDatePicker = true },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = state.customStartDateEpochMs?.let { dateFormat.format(Date(it)) } ?: "Start Date",
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = { showEndDatePicker = true },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = state.customEndDateEpochMs?.let { dateFormat.format(Date(it)) } ?: "End Date",
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "CATEGORIES" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি (মাল্টি-সিলেক্ট)" else "Categories (Multi-select)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        TextButton(onClick = {
                                            state = state.copy(selectedCategoryIds = allCategories.map { it.id }.toSet())
                                        }) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "সব" else "All", fontSize = 11.sp)
                                        }
                                        TextButton(onClick = {
                                            state = state.copy(selectedCategoryIds = emptySet())
                                        }) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "মুছুন" else "Clear", fontSize = 11.sp)
                                        }
                                    }
                                }

                                val parentCats = allCategories.filter { it.parentId == null }
                                parentCats.forEach { parent ->
                                    val children = allCategories.filter { it.parentId == parent.id }
                                    val isParentSelected = state.selectedCategoryIds.contains(parent.id)

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val next = state.selectedCategoryIds.toMutableSet()
                                                        if (isParentSelected) {
                                                            next.remove(parent.id)
                                                            children.forEach { next.remove(it.id) }
                                                        } else {
                                                            next.add(parent.id)
                                                            children.forEach { next.add(it.id) }
                                                        }
                                                        state = state.copy(selectedCategoryIds = next)
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) parent.nameBn else parent.nameEn,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (parent.type == CategoryType.EXPENSE) SolidExpense else SolidIncome
                                                )
                                                if (isParentSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            if (children.isNotEmpty()) {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    children.forEach { child ->
                                                        val isChildSelected = state.selectedCategoryIds.contains(child.id)
                                                        FilterChip(
                                                            selected = isChildSelected,
                                                            onClick = {
                                                                val next = state.selectedCategoryIds.toMutableSet()
                                                                if (isChildSelected) next.remove(child.id) else next.add(child.id)
                                                                state = state.copy(selectedCategoryIds = next)
                                                            },
                                                            label = {
                                                                Text(
                                                                    if (languageMode == LanguageMode.BANGLA) child.nameBn else child.nameEn,
                                                                    fontSize = 11.sp
                                                                )
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

                        "ACCOUNTS" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ও শ্রেণি" else "Accounts & Classes",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        TextButton(onClick = {
                                            state = state.copy(selectedAccountIds = allAccounts.map { it.id }.toSet())
                                        }) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "সব" else "All", fontSize = 11.sp)
                                        }
                                        TextButton(onClick = {
                                            state = state.copy(selectedAccountIds = emptySet(), selectedAccountTypes = emptySet())
                                        }) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "মুছুন" else "Clear", fontSize = 11.sp)
                                        }
                                    }
                                }

                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট শ্রেণি" else "Account Class",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    val isAssetSelected = state.selectedAccountTypes.contains(AccountType.ASSET)
                                    val isLiabSelected = state.selectedAccountTypes.contains(AccountType.LIABILITY)

                                    FilterChip(
                                        selected = isAssetSelected,
                                        onClick = {
                                            val next = state.selectedAccountTypes.toMutableSet()
                                            if (isAssetSelected) next.remove(AccountType.ASSET) else next.add(AccountType.ASSET)
                                            state = state.copy(selectedAccountTypes = next)
                                        },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সম্পদ (Asset)" else "Asset") }
                                    )
                                    FilterChip(
                                        selected = isLiabSelected,
                                        onClick = {
                                            val next = state.selectedAccountTypes.toMutableSet()
                                            if (isLiabSelected) next.remove(AccountType.LIABILITY) else next.add(AccountType.LIABILITY)
                                            state = state.copy(selectedAccountTypes = next)
                                        },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "দায় (Liability)" else "Liability") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val parentAccounts = allAccounts.filter { it.parentId == null }
                                parentAccounts.forEach { parent ->
                                    val children = allAccounts.filter { it.parentId == parent.id }
                                    val isParentSelected = state.selectedAccountIds.contains(parent.id)

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val next = state.selectedAccountIds.toMutableSet()
                                                        if (isParentSelected) {
                                                            next.remove(parent.id)
                                                            children.forEach { next.remove(it.id) }
                                                        } else {
                                                            next.add(parent.id)
                                                            children.forEach { next.add(it.id) }
                                                        }
                                                        state = state.copy(selectedAccountIds = next)
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) parent.nameBn else parent.nameEn,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                if (isParentSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            if (children.isNotEmpty()) {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    children.forEach { child ->
                                                        val isChildSelected = state.selectedAccountIds.contains(child.id)
                                                        FilterChip(
                                                            selected = isChildSelected,
                                                            onClick = {
                                                                val next = state.selectedAccountIds.toMutableSet()
                                                                if (isChildSelected) next.remove(child.id) else next.add(child.id)
                                                                state = state.copy(selectedAccountIds = next)
                                                            },
                                                            label = {
                                                                Text(
                                                                    if (languageMode == LanguageMode.BANGLA) child.nameBn else child.nameEn,
                                                                    fontSize = 11.sp
                                                                )
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

                        "AMOUNT" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "পরিমাণের সীমা (৳)" else "Amount Range",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                var minStr by remember { mutableStateOf(state.minAmount?.toString() ?: "") }
                                var maxStr by remember { mutableStateOf(state.maxAmount?.toString() ?: "") }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = minStr,
                                        onValueChange = {
                                            minStr = it
                                            state = state.copy(minAmount = it.toDoubleOrNull())
                                        },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বনিম্ন (Min)" else "Min Amount") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = maxStr,
                                        onValueChange = {
                                            maxStr = it
                                            state = state.copy(maxAmount = it.toDoubleOrNull())
                                        },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ (Max)" else "Max Amount") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val quickRanges = listOf(
                                        Pair(0.0, 500.0) to "< 500",
                                        Pair(500.0, 2000.0) to "500 - 2k",
                                        Pair(2000.0, 10000.0) to "2k - 10k",
                                        Pair(10000.0, null) to "> 10k"
                                    )
                                    quickRanges.forEach { (range, label) ->
                                        FilterChip(
                                            selected = state.minAmount == range.first && state.maxAmount == range.second,
                                            onClick = {
                                                state = state.copy(minAmount = range.first, maxAmount = range.second)
                                                minStr = range.first?.toString() ?: ""
                                                maxStr = range.second?.toString() ?: ""
                                            },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        "MORE" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (allLabels.isNotEmpty()) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ" else "Labels / Tags",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        allLabels.forEach { label ->
                                            val isSelected = state.selectedLabels.contains(label)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    val next = state.selectedLabels.toMutableSet()
                                                    if (isSelected) next.remove(label) else next.add(label)
                                                    state = state.copy(selectedLabels = next)
                                                },
                                                label = { Text(label, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TransactionStatus.entries.forEach { status ->
                                        val isSelected = state.selectedStatuses.contains(status)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                val next = state.selectedStatuses.toMutableSet()
                                                if (isSelected) next.remove(status) else next.add(status)
                                                state = state.copy(selectedStatuses = next)
                                            },
                                            label = { Text(status.name, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বাজেট শর্ত" else "Budget Conditions",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = state.onlyWithBudget,
                                        onClick = { state = state.copy(onlyWithBudget = !state.onlyWithBudget) },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "বাজেট নির্ধারিত" else "Has Budget", fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = state.onlyOverBudget,
                                        onClick = { state = state.copy(onlyOverBudget = !state.onlyOverBudget) },
                                        label = { Text(if (languageMode == LanguageMode.BANGLA) "বাজেট অতিক্রান্ত" else "Over Budget", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("filter_cancel_btn")
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            onApply(state)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("filter_apply_btn")
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (languageMode == LanguageMode.BANGLA)
                                "প্রয়োগ করুন (${state.activeFilterCount})"
                            else
                                "Apply Filter (${state.activeFilterCount})"
                        )
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customStartDateEpochMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        state = state.copy(customStartDateEpochMs = it, dateRangeType = FilterDateRange.CUSTOM)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.customEndDateEpochMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        state = state.copy(customEndDateEpochMs = it, dateRangeType = FilterDateRange.CUSTOM)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
