package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.DatePickerModal
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

enum class LedgerRowStyle {
    STANDARD,
    COMPACT,
    DETAILED
}

enum class LedgerDatePreset(val displayName: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_QUARTER("This Quarter"),
    THIS_YEAR("This Year"),
    LAST_YEAR("Last Year"),
    SINCE_LAST_YEAR("Since Last Year"),
    CUSTOM("Custom")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    allCategories: List<Category> = emptyList(),
    allAccounts: List<Account> = emptyList(),
    accountsWithBalances: List<AccountWithBalance> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onUpdateTransactions: (List<Transaction>) -> Unit = {},
    onDeleteTransactions: (List<Transaction>) -> Unit = {},
    onDeleteTransaction: (Transaction) -> Unit = {}
) {
    // Search & Filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var showSearchField by remember { mutableStateOf(false) }
    var selectedDatePreset by remember { mutableStateOf(LedgerDatePreset.ALL_TIME) }
    var minAmountFilter by remember { mutableDoubleStateOf(0.0) }
    var maxAmountFilter by remember { mutableDoubleStateOf(Double.MAX_VALUE) }
    var customStartDateMs by remember { mutableLongStateOf(0L) }
    var customEndDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var selectedCategoryIdFilter by remember { mutableStateOf<Long?>(null) }
    var selectedAccountIdFilter by remember { mutableStateOf<Long?>(null) }
    var selectedLabelFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<TransactionStatus?>(null) }

    var rowStyle by remember { mutableStateOf(LedgerRowStyle.STANDARD) }

    // Multi-Selection State
    var selectedTransactionIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedTransactionIds.isNotEmpty()

    // Dialog Visibilities
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    // Batch Action Modals
    var showBatchMenu by remember { mutableStateOf(false) }
    var showBatchChangeNameDialog by remember { mutableStateOf(false) }
    var showBatchChangeDateModal by remember { mutableStateOf(false) }
    var showBatchChangeCategoryDialog by remember { mutableStateOf(false) }
    var showBatchChangeAccountDialog by remember { mutableStateOf(false) }
    var showBatchChangeAmountDialog by remember { mutableStateOf(false) }
    var showBatchAddLabelDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirmDialog by remember { mutableStateOf(false) }

    val accountBalanceMap: Map<Long, Double> = remember(accountsWithBalances) {
        val map = mutableMapOf<Long, Double>()
        fun addAcc(awb: AccountWithBalance) {
            map[awb.account.id] = awb.currentBalance
            awb.subAccounts.forEach { addAcc(it) }
        }
        accountsWithBalances.forEach { addAcc(it) }
        map
    }

    // Compute Date Bounds
    val (startEpochMs, endEpochMs) = remember(selectedDatePreset, customStartDateMs, customEndDateMs) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedDatePreset) {
            LedgerDatePreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            LedgerDatePreset.TODAY -> {
                val start = DateUtils.getStartOfDay(now)
                Pair(start, start + 86400000L - 1L)
            }
            LedgerDatePreset.YESTERDAY -> {
                val todayStart = DateUtils.getStartOfDay(now)
                val yestStart = todayStart - 86400000L
                Pair(yestStart, todayStart - 1L)
            }
            LedgerDatePreset.THIS_WEEK -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.LAST_WEEK -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val thisWeekStart = DateUtils.getStartOfDay(cal.timeInMillis)
                val lastWeekStart = thisWeekStart - (7L * 86400000L)
                Pair(lastWeekStart, thisWeekStart - 1L)
            }
            LedgerDatePreset.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.LAST_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val thisMonthStart = DateUtils.getStartOfDay(cal.timeInMillis)
                cal.add(Calendar.MONTH, -1)
                val lastMonthStart = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(lastMonthStart, thisMonthStart - 1L)
            }
            LedgerDatePreset.THIS_QUARTER -> {
                cal.timeInMillis = now
                val currentMonth = cal.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.THIS_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.LAST_YEAR -> {
                cal.timeInMillis = now
                val curYear = cal.get(Calendar.YEAR)
                cal.set(Calendar.YEAR, curYear - 1)
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                val end = DateUtils.getStartOfDay(cal.timeInMillis) + 86400000L - 1L
                Pair(start, end)
            }
            LedgerDatePreset.SINCE_LAST_YEAR -> {
                cal.timeInMillis = now
                val curYear = cal.get(Calendar.YEAR)
                cal.set(Calendar.YEAR, curYear - 1)
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.CUSTOM -> Pair(customStartDateMs, customEndDateMs)
        }
    }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedTypeFilter,
        startEpochMs,
        endEpochMs,
        minAmountFilter,
        maxAmountFilter,
        selectedCategoryIdFilter,
        selectedAccountIdFilter,
        selectedLabelFilter,
        selectedStatusFilter
    ) {
        transactions.filter { item ->
            val tx = item.transaction
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesAmount = tx.amount >= minAmountFilter && tx.amount <= maxAmountFilter

            val matchesCategory = if (selectedCategoryIdFilter == null) true else {
                tx.categoryId == selectedCategoryIdFilter || tx.subCategoryId == selectedCategoryIdFilter
            }

            val matchesAccount = if (selectedAccountIdFilter == null) true else {
                tx.debitAccountId == selectedAccountIdFilter || tx.creditAccountId == selectedAccountIdFilter
            }

            val matchesLabel = if (selectedLabelFilter == null) true else {
                tx.referenceNo.equals(selectedLabelFilter, ignoreCase = true) ||
                        tx.note.contains(selectedLabelFilter!!, ignoreCase = true)
            }

            val matchesStatus = if (selectedStatusFilter == null) true else {
                tx.status == selectedStatusFilter
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val query = searchQuery.trim().lowercase()
                tx.note.lowercase().contains(query) ||
                        tx.payeeOrPayer.lowercase().contains(query) ||
                        tx.referenceNo.lowercase().contains(query) ||
                        (item.category?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.category?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.debitAccount?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.creditAccount?.nameEn?.lowercase()?.contains(query) == true)
            }
            matchesType && matchesDate && matchesAmount && matchesCategory && matchesAccount && matchesLabel && matchesStatus && matchesSearch
        }
    }

    // Group transactions by calendar day (descending order)
    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateUtils.getStartOfDay(it.transaction.dateEpochMs) }
            .toList()
            .sortedByDescending { it.first }
    }

    // Calculate selected items metrics
    val selectedTransactions = remember(selectedTransactionIds, transactions) {
        transactions.filter { selectedTransactionIds.contains(it.transaction.id) }
    }

    val selectedNetTotal = remember(selectedTransactions) {
        val inc = selectedTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val exp = selectedTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
        inc - exp
    }

    val hasActiveFilters = selectedDatePreset != LedgerDatePreset.ALL_TIME ||
            selectedTypeFilter != null ||
            minAmountFilter > 0.0 ||
            maxAmountFilter < Double.MAX_VALUE ||
            selectedCategoryIdFilter != null ||
            selectedAccountIdFilter != null ||
            selectedLabelFilter != null ||
            selectedStatusFilter != null ||
            searchQuery.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection Mode Top Bar (Bluecoins style) OR Standard Header
        if (isSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { selectedTransactionIds = emptySet() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Selection",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            val sign = if (selectedNetTotal > 0) "+" else if (selectedNetTotal < 0) "-" else ""
                            val formattedAmount = LanguageHelper.formatCurrency(kotlin.math.abs(selectedNetTotal), languageMode)
                            Text(
                                text = "$sign$formattedAmount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (selectedNetTotal >= 0) SolidIncome else SolidExpense
                            )
                            Text(
                                text = "${selectedTransactionIds.size} selected",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle row style
                        IconButton(onClick = {
                            rowStyle = when (rowStyle) {
                                LedgerRowStyle.STANDARD -> LedgerRowStyle.COMPACT
                                LedgerRowStyle.COMPACT -> LedgerRowStyle.DETAILED
                                LedgerRowStyle.DETAILED -> LedgerRowStyle.STANDARD
                            }
                        }) {
                            Icon(
                                imageVector = when (rowStyle) {
                                    LedgerRowStyle.STANDARD -> Icons.Default.ViewAgenda
                                    LedgerRowStyle.COMPACT -> Icons.Default.TableRows
                                    LedgerRowStyle.DETAILED -> Icons.Default.GridView
                                },
                                contentDescription = "Row Style",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Select All / Deselect All
                        IconButton(onClick = {
                            val allVisibleIds = filteredTransactions.map { it.transaction.id }.toSet()
                            selectedTransactionIds = if (selectedTransactionIds.containsAll(allVisibleIds)) {
                                emptySet()
                            } else {
                                allVisibleIds
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select All",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // 3-Dots Overflow Menu for Batch Actions
                        Box {
                            IconButton(onClick = { showBatchMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Actions",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            DropdownMenu(
                                expanded = showBatchMenu,
                                onDismissRequest = { showBatchMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Change Name") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchChangeNameDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Date") },
                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchChangeDateModal = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Category") },
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchChangeCategoryDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Account") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchChangeAccountDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Amount") },
                                    leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchChangeAmountDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add Label") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchAddLabelDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reconcile") },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome) },
                                    onClick = {
                                        showBatchMenu = false
                                        val updated = selectedTransactions.map {
                                            it.transaction.copy(status = TransactionStatus.RECONCILED)
                                        }
                                        onUpdateTransactions(updated)
                                        selectedTransactionIds = emptySet()
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showBatchMenu = false
                                        showBatchDeleteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ledger_screen"),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search & Filter Header (when not in selection mode)
            if (!isSelectionMode) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageHelper.getString("transactions", languageMode),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (filteredTransactions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${filteredTransactions.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Multi-select mode trigger button
                                IconButton(
                                    onClick = {
                                        val firstId = filteredTransactions.firstOrNull()?.transaction?.id
                                        if (firstId != null) selectedTransactionIds = setOf(firstId)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckBox,
                                        contentDescription = "Select Mode",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { showSearchField = !showSearchField },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (showSearchField || searchQuery.isNotEmpty()) SolidPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { showFilterDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = if (hasActiveFilters) SolidPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        if (showSearchField || searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search for text in item name, payee or notes", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Type Filter Chips (All, Expense, Income, Transfer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedTypeFilter == null,
                                onClick = { selectedTypeFilter = null },
                                label = { Text(LanguageHelper.getString("all", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedTypeFilter == TransactionType.EXPENSE,
                                onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE },
                                label = { Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidExpense,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedTypeFilter == TransactionType.INCOME,
                                onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME },
                                label = { Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidIncome,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = selectedTypeFilter == TransactionType.TRANSFER,
                                onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER },
                                label = { Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidTransfer,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Active Filter Indicators bar
                        if (hasActiveFilters && !isSelectionMode) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            append("Filtered: ")
                                            if (selectedDatePreset != LedgerDatePreset.ALL_TIME) append(selectedDatePreset.displayName)
                                            if (selectedCategoryIdFilter != null) {
                                                val cat = allCategories.firstOrNull { it.id == selectedCategoryIdFilter }
                                                if (isNotEmpty()) append(", ")
                                                append(cat?.nameEn ?: "Category")
                                            }
                                            if (selectedAccountIdFilter != null) {
                                                val acc = allAccounts.firstOrNull { it.id == selectedAccountIdFilter }
                                                if (isNotEmpty()) append(", ")
                                                append(acc?.nameEn ?: "Account")
                                            }
                                            if (selectedLabelFilter != null) {
                                                if (isNotEmpty()) append(", ")
                                                append(selectedLabelFilter)
                                            }
                                            if (selectedStatusFilter != null) {
                                                if (isNotEmpty()) append(", ")
                                                append(selectedStatusFilter!!.name)
                                            }
                                        },
                                        fontSize = 11.sp,
                                        color = SolidPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        selectedDatePreset = LedgerDatePreset.ALL_TIME
                                        selectedTypeFilter = null
                                        minAmountFilter = 0.0
                                        maxAmountFilter = Double.MAX_VALUE
                                        selectedCategoryIdFilter = null
                                        selectedAccountIdFilter = null
                                        selectedLabelFilter = null
                                        selectedStatusFilter = null
                                        searchQuery = ""
                                    },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Reset", fontSize = 11.sp, color = SolidExpense)
                                }
                            }
                        }
                    }
                }
            }

            if (groupedByDay.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LanguageHelper.getString("no_transactions", languageMode),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                // Render Date Grouped Transactions
                groupedByDay.forEach { (dayEpochMs, dayTxList) ->
                    val dayIncome = dayTxList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
                    val dayExpense = dayTxList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
                    val dayNet = dayIncome - dayExpense

                    // Day Header
                    item(key = "day_header_$dayEpochMs") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.formatDayHeader(dayEpochMs, languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val netSign = if (dayNet > 0) "+" else if (dayNet < 0) "-" else ""
                                val netColor = if (dayNet > 0) SolidIncome else if (dayNet < 0) SolidExpense else MaterialTheme.colorScheme.outline
                                Text(
                                    text = "$netSign${LanguageHelper.formatCurrency(kotlin.math.abs(dayNet), languageMode)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = netColor
                                )
                            }
                        }
                    }

                    // Day's Transaction Items Container
                    item(key = "day_items_$dayEpochMs") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                dayTxList.forEachIndexed { index, item ->
                                    val tx = item.transaction
                                    val isSelected = selectedTransactionIds.contains(tx.id)

                                    if (tx.type == TransactionType.TRANSFER) {
                                        val showSourceLeg = selectedAccountIdFilter == null || tx.creditAccountId == selectedAccountIdFilter
                                        val showDestLeg = selectedAccountIdFilter == null || tx.debitAccountId == selectedAccountIdFilter
                                        val transferTitle = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else (item.debitAccount?.localizedName(languageMode) ?: LanguageHelper.getString("transfer", languageMode))

                                        if (showSourceLeg) {
                                            val srcAccountName = item.creditAccount?.localizedName(languageMode) ?: "Source"
                                            val srcBalance = tx.creditAccountId?.let { accountBalanceMap[it] }

                                            TransactionRowItem(
                                                item = item,
                                                languageMode = languageMode,
                                                rowStyle = rowStyle,
                                                isSelected = isSelected,
                                                isSelectionMode = isSelectionMode,
                                                accountName = srcAccountName,
                                                accountBalance = srcBalance,
                                                overrideTitle = transferTitle,
                                                overrideSubtitle = "(${LanguageHelper.getString("transfer", languageMode)})",
                                                overrideSign = "−",
                                                overrideAmtColor = SolidExpense,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedTransactionIds = if (isSelected) selectedTransactionIds - tx.id else selectedTransactionIds + tx.id
                                                    } else {
                                                        onTransactionClick(tx)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedTransactionIds = if (isSelected) selectedTransactionIds - tx.id else selectedTransactionIds + tx.id
                                                }
                                            )
                                        }

                                        if (showSourceLeg && showDestLeg) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                        }

                                        if (showDestLeg) {
                                            val destAccountName = item.debitAccount?.localizedName(languageMode) ?: "Dest"
                                            val destBalance = tx.debitAccountId?.let { accountBalanceMap[it] }

                                            TransactionRowItem(
                                                item = item,
                                                languageMode = languageMode,
                                                rowStyle = rowStyle,
                                                isSelected = isSelected,
                                                isSelectionMode = isSelectionMode,
                                                accountName = destAccountName,
                                                accountBalance = destBalance,
                                                overrideTitle = transferTitle,
                                                overrideSubtitle = "(${LanguageHelper.getString("transfer", languageMode)})",
                                                overrideSign = "+",
                                                overrideAmtColor = SolidIncome,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedTransactionIds = if (isSelected) selectedTransactionIds - tx.id else selectedTransactionIds + tx.id
                                                    } else {
                                                        onTransactionClick(tx)
                                                    }
                                                },
                                                onLongClick = {
                                                    selectedTransactionIds = if (isSelected) selectedTransactionIds - tx.id else selectedTransactionIds + tx.id
                                                }
                                            )
                                        }
                                    } else {
                                        val accName = when (tx.type) {
                                            TransactionType.EXPENSE -> item.creditAccount?.localizedName(languageMode) ?: ""
                                            TransactionType.INCOME -> item.debitAccount?.localizedName(languageMode) ?: ""
                                            else -> ""
                                        }
                                        val accBalance = when (tx.type) {
                                            TransactionType.EXPENSE -> tx.creditAccountId?.let { accountBalanceMap[it] }
                                            TransactionType.INCOME -> tx.debitAccountId?.let { accountBalanceMap[it] }
                                            else -> null
                                        }

                                        TransactionRowItem(
                                            item = item,
                                            languageMode = languageMode,
                                            rowStyle = rowStyle,
                                            isSelected = isSelected,
                                            isSelectionMode = isSelectionMode,
                                            accountName = accName,
                                            accountBalance = accBalance,
                                            overrideSign = if (tx.type == TransactionType.EXPENSE) "−" else "+",
                                            overrideAmtColor = if (tx.type == TransactionType.EXPENSE) SolidExpense else SolidIncome,
                                            onClick = {
                                                if (isSelectionMode) {
                                                    selectedTransactionIds = if (isSelected) {
                                                        selectedTransactionIds - tx.id
                                                    } else {
                                                        selectedTransactionIds + tx.id
                                                    }
                                                } else {
                                                    onTransactionClick(tx)
                                                }
                                            },
                                            onLongClick = {
                                                selectedTransactionIds = if (isSelected) {
                                                    selectedTransactionIds - tx.id
                                                } else {
                                                    selectedTransactionIds + tx.id
                                                }
                                            }
                                        )
                                    }

                                    if (index < dayTxList.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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

    // Advanced Filtering Dialog (Matching Screenshot 2 - Bluecoins style)
    if (showFilterDialog) {
        AdvancedTransactionsFilterDialog(
            currentPreset = selectedDatePreset,
            currentType = selectedTypeFilter,
            currentMinAmount = minAmountFilter,
            currentMaxAmount = if (maxAmountFilter == Double.MAX_VALUE) 0.0 else maxAmountFilter,
            currentCategoryId = selectedCategoryIdFilter,
            currentAccountId = selectedAccountIdFilter,
            currentLabel = selectedLabelFilter,
            currentStatus = selectedStatusFilter,
            currentRowStyle = rowStyle,
            allCategories = allCategories,
            allAccounts = allAccounts,
            allTransactions = transactions,
            languageMode = languageMode,
            onApply = { newPreset, newType, newMin, newMax, newCat, newAcc, newLbl, newStat, newStyle ->
                selectedDatePreset = newPreset
                selectedTypeFilter = newType
                minAmountFilter = newMin
                maxAmountFilter = if (newMax > 0) newMax else Double.MAX_VALUE
                selectedCategoryIdFilter = newCat
                selectedAccountIdFilter = newAcc
                selectedLabelFilter = newLbl
                selectedStatusFilter = newStat
                rowStyle = newStyle
                showFilterDialog = false
            },
            onSelectCustomDates = {
                showFilterDialog = false
                showCustomStartPicker = true
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Custom Date Range Pickers
    if (showCustomStartPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customStartDateMs)
        DatePickerDialog(
            onDismissRequest = { showCustomStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        customStartDateMs = it
                        showCustomStartPicker = false
                        showCustomEndPicker = true
                    }
                }) {
                    Text("Next: End Date")
                }
            },
            dismissButton = { TextButton(onClick = { showCustomStartPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dateState, title = { Text("Select Start Date", modifier = Modifier.padding(16.dp)) })
        }
    }

    if (showCustomEndPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customEndDateMs)
        DatePickerDialog(
            onDismissRequest = { showCustomEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        customEndDateMs = it + (24L * 60L * 60L * 1000L - 1L)
                        selectedDatePreset = LedgerDatePreset.CUSTOM
                        showCustomEndPicker = false
                    }
                }) {
                    Text("Apply Range")
                }
            },
            dismissButton = { TextButton(onClick = { showCustomEndPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dateState, title = { Text("Select End Date", modifier = Modifier.padding(16.dp)) })
        }
    }

    // Batch Action Dialogs
    if (showBatchChangeNameDialog) {
        val existingPayees = remember(transactions) {
            transactions.map { it.transaction.payeeOrPayer }
                .filter { it.isNotBlank() }
                .groupBy { it.trim() }
                .map { (name, list) -> Pair(name, list.size) }
                .sortedByDescending { it.second }
        }
        BatchChangeNameDialog(
            count = selectedTransactionIds.size,
            existingPayeesWithCount = existingPayees,
            languageMode = languageMode,
            onDismiss = { showBatchChangeNameDialog = false },
            onConfirm = { newName ->
                val updated = selectedTransactions.map { it.transaction.copy(payeeOrPayer = newName) }
                onUpdateTransactions(updated)
                showBatchChangeNameDialog = false
                selectedTransactionIds = emptySet()
            }
        )
    }

    if (showBatchChangeDateModal) {
        DatePickerModal(
            selectedDateEpochMs = System.currentTimeMillis(),
            languageMode = languageMode,
            onDateSelected = { newDateMs ->
                val updated = selectedTransactions.map { it.transaction.copy(dateEpochMs = newDateMs) }
                onUpdateTransactions(updated)
                showBatchChangeDateModal = false
                selectedTransactionIds = emptySet()
            },
            onDismiss = { showBatchChangeDateModal = false }
        )
    }

    if (showBatchChangeCategoryDialog) {
        BatchSelectCategoryDialog(
            categories = allCategories,
            languageMode = languageMode,
            onDismiss = { showBatchChangeCategoryDialog = false },
            onSelect = { cat, subCat ->
                val updated = selectedTransactions.map {
                    it.transaction.copy(categoryId = cat.id, subCategoryId = subCat?.id)
                }
                onUpdateTransactions(updated)
                showBatchChangeCategoryDialog = false
                selectedTransactionIds = emptySet()
            }
        )
    }

    if (showBatchChangeAccountDialog) {
        BatchSelectAccountDialog(
            accounts = allAccounts,
            languageMode = languageMode,
            onDismiss = { showBatchChangeAccountDialog = false },
            onSelect = { acc ->
                val updated = selectedTransactions.map { item ->
                    val tx = item.transaction
                    when (tx.type) {
                        TransactionType.EXPENSE -> tx.copy(creditAccountId = acc.id)
                        TransactionType.INCOME -> tx.copy(debitAccountId = acc.id)
                        TransactionType.TRANSFER -> tx.copy(debitAccountId = acc.id)
                    }
                }
                onUpdateTransactions(updated)
                showBatchChangeAccountDialog = false
                selectedTransactionIds = emptySet()
            }
        )
    }

    if (showBatchChangeAmountDialog) {
        BatchChangeAmountDialog(
            count = selectedTransactionIds.size,
            languageMode = languageMode,
            onDismiss = { showBatchChangeAmountDialog = false },
            onConfirm = { newAmt ->
                val updated = selectedTransactions.map { it.transaction.copy(amount = newAmt) }
                onUpdateTransactions(updated)
                showBatchChangeAmountDialog = false
                selectedTransactionIds = emptySet()
            }
        )
    }

    if (showBatchAddLabelDialog) {
        BatchAddLabelDialog(
            count = selectedTransactionIds.size,
            allTransactions = transactions,
            onDismiss = { showBatchAddLabelDialog = false },
            onConfirm = { newLabel ->
                val updated = selectedTransactions.map { it.transaction.copy(referenceNo = newLabel) }
                onUpdateTransactions(updated)
                showBatchAddLabelDialog = false
                selectedTransactionIds = emptySet()
            }
        )
    }

    if (showBatchDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${selectedTransactionIds.size} selected transaction(s)? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = selectedTransactions.map { it.transaction }
                        onDeleteTransactions(toDelete)
                        showBatchDeleteConfirmDialog = false
                        selectedTransactionIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageHelper.getString("delete", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirmDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }
}

/**
 * Advanced Filtering Dialog matching Bluecoins Screenshot 2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTransactionsFilterDialog(
    currentPreset: LedgerDatePreset,
    currentType: TransactionType?,
    currentMinAmount: Double,
    currentMaxAmount: Double,
    currentCategoryId: Long?,
    currentAccountId: Long?,
    currentLabel: String?,
    currentStatus: TransactionStatus?,
    currentRowStyle: LedgerRowStyle,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    allTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onApply: (LedgerDatePreset, TransactionType?, Double, Double, Long?, Long?, String?, TransactionStatus?, LedgerRowStyle) -> Unit,
    onSelectCustomDates: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempPreset by remember { mutableStateOf(currentPreset) }
    var tempType by remember { mutableStateOf(currentType) }
    var tempMinStr by remember { mutableStateOf(if (currentMinAmount > 0) currentMinAmount.toInt().toString() else "") }
    var tempMaxStr by remember { mutableStateOf(if (currentMaxAmount > 0) currentMaxAmount.toInt().toString() else "") }
    var tempCategoryId by remember { mutableStateOf(currentCategoryId) }
    var tempAccountId by remember { mutableStateOf(currentAccountId) }
    var tempLabel by remember { mutableStateOf(currentLabel) }
    var tempStatus by remember { mutableStateOf(currentStatus) }
    var tempRowStyle by remember { mutableStateOf(currentRowStyle) }

    var showFromCalc by remember { mutableStateOf(false) }
    var showToCalc by remember { mutableStateOf(false) }
    var showCatPicker by remember { mutableStateOf(false) }
    var showAccPicker by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header with Row style, Reset & Preset icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            tempRowStyle = when (tempRowStyle) {
                                LedgerRowStyle.STANDARD -> LedgerRowStyle.COMPACT
                                LedgerRowStyle.COMPACT -> LedgerRowStyle.DETAILED
                                LedgerRowStyle.DETAILED -> LedgerRowStyle.STANDARD
                            }
                        },
                        label = { Text("Row style: ${tempRowStyle.name.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                when (tempRowStyle) {
                                    LedgerRowStyle.STANDARD -> Icons.Default.ViewAgenda
                                    LedgerRowStyle.COMPACT -> Icons.Default.TableRows
                                    LedgerRowStyle.DETAILED -> Icons.Default.GridView
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                tempPreset = LedgerDatePreset.ALL_TIME
                                tempType = null
                                tempMinStr = ""
                                tempMaxStr = ""
                                tempCategoryId = null
                                tempAccountId = null
                                tempLabel = null
                                tempStatus = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Filters", tint = MaterialTheme.colorScheme.outline)
                        }

                        IconButton(
                            onClick = { /* Quick Favorite / Preset */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "Presets", tint = SolidPrimary)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Amount Range (Amount from / Amount to with Calculator)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempMinStr,
                        onValueChange = { tempMinStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount from", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { showFromCalc = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = tempMaxStr,
                        onValueChange = { tempMaxStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount to", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { showToCalc = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calc", modifier = Modifier.size(16.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Date Range Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Date Range", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.ALL_TIME,
                            onClick = { tempPreset = LedgerDatePreset.ALL_TIME },
                            label = { Text("All Time", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.TODAY,
                            onClick = { tempPreset = LedgerDatePreset.TODAY },
                            label = { Text("Today", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.YESTERDAY,
                            onClick = { tempPreset = LedgerDatePreset.YESTERDAY },
                            label = { Text("Yesterday", fontSize = 10.sp) }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.THIS_WEEK,
                            onClick = { tempPreset = LedgerDatePreset.THIS_WEEK },
                            label = { Text("This Week", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.LAST_WEEK,
                            onClick = { tempPreset = LedgerDatePreset.LAST_WEEK },
                            label = { Text("Last Week", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.THIS_MONTH,
                            onClick = { tempPreset = LedgerDatePreset.THIS_MONTH },
                            label = { Text("This Month", fontSize = 10.sp) }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.LAST_MONTH,
                            onClick = { tempPreset = LedgerDatePreset.LAST_MONTH },
                            label = { Text("Last Month", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.SINCE_LAST_YEAR,
                            onClick = { tempPreset = LedgerDatePreset.SINCE_LAST_YEAR },
                            label = { Text("Since Last Year", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempPreset == LedgerDatePreset.CUSTOM,
                            onClick = {
                                tempPreset = LedgerDatePreset.CUSTOM
                                onSelectCustomDates()
                            },
                            label = { Text("Custom 📅", fontSize = 10.sp) }
                        )
                    }
                }

                // Transaction Type
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Transaction Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = tempType == null,
                            onClick = { tempType = null },
                            label = { Text("All", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempType == TransactionType.EXPENSE,
                            onClick = { tempType = TransactionType.EXPENSE },
                            label = { Text("Expense", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempType == TransactionType.INCOME,
                            onClick = { tempType = TransactionType.INCOME },
                            label = { Text("Income", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempType == TransactionType.TRANSFER,
                            onClick = { tempType = TransactionType.TRANSFER },
                            label = { Text("Transfer", fontSize = 10.sp) }
                        )
                    }
                }

                // Category Selector Row with Side Filter Icon
                FilterSelectorRow(
                    label = "Category",
                    selectedValue = if (tempCategoryId == null) "(All Categories)" else allCategories.firstOrNull { it.id == tempCategoryId }?.nameEn ?: "Category",
                    isFiltered = tempCategoryId != null,
                    onOpenPicker = { showCatPicker = true },
                    onClear = { tempCategoryId = null }
                )

                // Account Selector Row with Side Filter Icon
                FilterSelectorRow(
                    label = "Account",
                    selectedValue = if (tempAccountId == null) "(All Accounts)" else allAccounts.firstOrNull { it.id == tempAccountId }?.nameEn ?: "Account",
                    isFiltered = tempAccountId != null,
                    onOpenPicker = { showAccPicker = true },
                    onClear = { tempAccountId = null }
                )

                // Labels Selector Row with Side Filter Icon
                FilterSelectorRow(
                    label = "Labels",
                    selectedValue = tempLabel ?: "(No Filter)",
                    isFiltered = tempLabel != null,
                    onOpenPicker = { showLabelPicker = true },
                    onClear = { tempLabel = null }
                )

                // Status Selector Row with Side Filter Icon
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = tempStatus == null,
                            onClick = { tempStatus = null },
                            label = { Text("All", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempStatus == TransactionStatus.RECONCILED,
                            onClick = { tempStatus = TransactionStatus.RECONCILED },
                            label = { Text("Reconciled", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempStatus == TransactionStatus.CLEARED,
                            onClick = { tempStatus = TransactionStatus.CLEARED },
                            label = { Text("Cleared", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = tempStatus == TransactionStatus.VOID,
                            onClick = { tempStatus = TransactionStatus.VOID },
                            label = { Text("Void", fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Actions (Cancel / OK)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val minAmt = tempMinStr.toDoubleOrNull() ?: 0.0
                            val maxAmt = tempMaxStr.toDoubleOrNull() ?: 0.0
                            onApply(tempPreset, tempType, minAmt, maxAmt, tempCategoryId, tempAccountId, tempLabel, tempStatus, tempRowStyle)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    // Calculators
    if (showFromCalc) {
        PopupCalculatorDialog(
            initialValue = tempMinStr.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showFromCalc = false },
            onValueConfirmed = { value ->
                tempMinStr = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                showFromCalc = false
            }
        )
    }

    if (showToCalc) {
        PopupCalculatorDialog(
            initialValue = tempMaxStr.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showToCalc = false },
            onValueConfirmed = { value ->
                tempMaxStr = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                showToCalc = false
            }
        )
    }

    // Category Picker for Filter
    if (showCatPicker) {
        BatchSelectCategoryDialog(
            categories = allCategories,
            languageMode = languageMode,
            onDismiss = { showCatPicker = false },
            onSelect = { cat, subCat ->
                tempCategoryId = subCat?.id ?: cat.id
                showCatPicker = false
            }
        )
    }

    // Account Picker for Filter
    if (showAccPicker) {
        BatchSelectAccountDialog(
            accounts = allAccounts,
            languageMode = languageMode,
            onDismiss = { showAccPicker = false },
            onSelect = { acc ->
                tempAccountId = acc.id
                showAccPicker = false
            }
        )
    }

    // Label Picker for Filter
    if (showLabelPicker) {
        val uniqueLabels = remember(allTransactions) {
            allTransactions.mapNotNull { it.transaction.referenceNo.takeIf { s -> s.isNotBlank() } }.distinct()
        }
        AlertDialog(
            onDismissRequest = { showLabelPicker = false },
            title = { Text("Select Label", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (uniqueLabels.isEmpty()) {
                        Text("No labels found in records.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    } else {
                        uniqueLabels.forEach { lbl ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tempLabel == lbl) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempLabel = lbl
                                        showLabelPicker = false
                                    }
                            ) {
                                Text(
                                    text = "#$lbl",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLabelPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FilterSelectorRow(
    label: String,
    selectedValue: String,
    isFiltered: Boolean,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    fontSize = 13.sp,
                    fontWeight = if (isFiltered) FontWeight.Bold else FontWeight.Normal,
                    color = if (isFiltered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenPicker)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFiltered) {
                        IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onOpenPicker, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = "Pick", tint = SolidPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRowItem(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    rowStyle: LedgerRowStyle = LedgerRowStyle.STANDARD,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    accountName: String? = null,
    accountBalance: Double? = null,
    overrideTitle: String? = null,
    overrideSubtitle: String? = null,
    overrideSign: String? = null,
    overrideAmtColor: Color? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val tx = item.transaction

    val iconColor = when (tx.type) {
        TransactionType.EXPENSE -> SolidExpense
        TransactionType.INCOME -> SolidIncome
        TransactionType.TRANSFER -> SolidTransfer
    }
    val iconBg = when (tx.type) {
        TransactionType.EXPENSE -> SolidExpenseContainer
        TransactionType.INCOME -> SolidIncomeContainer
        TransactionType.TRANSFER -> SolidPrimaryContainer
    }

    val primaryTitle = overrideTitle ?: if (tx.payeeOrPayer.isNotBlank()) {
        tx.payeeOrPayer
    } else {
        when (tx.type) {
            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("expense", languageMode)
            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("income", languageMode)
            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
        }
    }

    val subTitle = overrideSubtitle ?: when (tx.type) {
        TransactionType.EXPENSE -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != cat) "$cat > $sub" else cat
        }
        TransactionType.INCOME -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != cat) "$cat > $sub" else cat
        }
        TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
    }

    val accountDisplay = accountName ?: when (tx.type) {
        TransactionType.EXPENSE -> item.creditAccount?.localizedName(languageMode) ?: ""
        TransactionType.INCOME -> item.debitAccount?.localizedName(languageMode) ?: ""
        TransactionType.TRANSFER -> {
            val from = item.creditAccount?.localizedName(languageMode) ?: "Source"
            val to = item.debitAccount?.localizedName(languageMode) ?: "Dest"
            "$from ➔ $to"
        }
    }

    val isPositiveEffect = when (tx.type) {
        TransactionType.EXPENSE -> tx.amount < 0
        TransactionType.INCOME -> tx.amount >= 0
        TransactionType.TRANSFER -> false
    }
    val isNegativeEffect = when (tx.type) {
        TransactionType.EXPENSE -> tx.amount >= 0
        TransactionType.INCOME -> tx.amount < 0
        TransactionType.TRANSFER -> false
    }
    val sign = overrideSign ?: when {
        tx.type == TransactionType.TRANSFER -> ""
        isPositiveEffect -> "+"
        isNegativeEffect -> "−"
        else -> ""
    }
    val amtColor = overrideAmtColor ?: when {
        tx.type == TransactionType.TRANSFER -> SolidTransfer
        isPositiveEffect -> SolidIncome
        else -> SolidExpense
    }

    val rowBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                horizontal = 12.dp,
                vertical = when (rowStyle) {
                    LedgerRowStyle.COMPACT -> 6.dp
                    LedgerRowStyle.STANDARD -> 10.dp
                    LedgerRowStyle.DETAILED -> 12.dp
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Avatar Icon OR Selection Check Circle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SolidPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (tx.type) {
                        TransactionType.EXPENSE -> IconHelper.getIconByName(item.category?.iconName ?: "Category")
                        TransactionType.INCOME -> IconHelper.getIconByName(item.category?.iconName ?: "Payments")
                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center: Name (left top), Category/Group (left below), Labels & Notes (below)
            Column(modifier = Modifier.weight(1f)) {
                // Left Top: Name / Payee
                Text(
                    text = primaryTitle,
                    fontSize = if (rowStyle == LedgerRowStyle.COMPACT) 12.5.sp else 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Left Below: Category ("Group > category")
                if (subTitle.isNotBlank()) {
                    Text(
                        text = subTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Below that: Labels on left (round shape) and just right notes if any
                val hasLabels = tx.referenceNo.isNotBlank()
                val hasNote = tx.note.isNotBlank()
                val hasAttachment = tx.attachmentUri.isNotBlank()

                if (hasLabels || hasNote || hasAttachment) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Round Shape Label(s) on Left
                        if (hasLabels) {
                            val labelList = tx.referenceNo.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            labelList.take(2).forEach { lbl ->
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "#$lbl",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (labelList.size > 2) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "+${labelList.size - 2}",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Just right: Notes if any
                        if (hasNote) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = tx.note,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (hasAttachment) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attached",
                                tint = SolidPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Amount on top (with color & sign), Account name & balance below
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$sign${LanguageHelper.formatCurrency(Math.abs(tx.amount), languageMode)}",
                fontSize = if (rowStyle == LedgerRowStyle.COMPACT) 12.5.sp else 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = amtColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            val accountLine = if (accountDisplay.isNotBlank()) {
                if (accountBalance != null) {
                    val balStr = if (accountBalance < 0) {
                        "-${LanguageHelper.formatCurrency(kotlin.math.abs(accountBalance), languageMode)}"
                    } else {
                        LanguageHelper.formatCurrency(accountBalance, languageMode)
                    }
                    "$accountDisplay  $balStr"
                } else {
                    accountDisplay
                }
            } else ""

            if (accountLine.isNotBlank()) {
                Text(
                    text = accountLine,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (tx.status != TransactionStatus.NONE) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = (if (tx.status == TransactionStatus.RECONCILED) SolidIncome else MaterialTheme.colorScheme.outline).copy(alpha = 0.12f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = tx.status.titleEn,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.status == TransactionStatus.RECONCILED) SolidIncome else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// Batch Action Dialogs
@Composable
private fun BatchChangeNameDialog(
    count: Int,
    existingPayeesWithCount: List<Pair<String, Int>>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf("") }

    val filteredSuggestions = remember(searchQuery, existingPayeesWithCount) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            existingPayeesWithCount
        } else {
            existingPayeesWithCount.filter { it.first.lowercase().contains(q) }
        }
    }

    val trimmedSearch = searchQuery.trim()
    val isExactMatch = existingPayeesWithCount.any { it.first.equals(trimmedSearch, ignoreCase = true) }
    val showCreateNewOption = trimmedSearch.isNotEmpty() && !isExactMatch

    val activeName = if (selectedName.isNotBlank()) selectedName else trimmedSearch

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নাম / গ্রহীতা পরিবর্তন করুন" else "Change Name / Payee",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "$count টি লেনদেন নির্বাচিত" else "$count items selected",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Search & input field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        selectedName = it
                    },
                    label = {
                        Text(if (languageMode == LanguageMode.BANGLA) "নাম খুঁজুন বা নতুন লিখুন" else "Search or enter new name")
                    },
                    placeholder = {
                        Text(if (languageMode == LanguageMode.BANGLA) "উদাঃ স্বপ্ন, বাজার, বেতন..." else "e.g. Walmart, Salary, Rent...")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                selectedName = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_change_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী এন্ট্রি সমূহ" else "Previous Entries"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "পরামর্শ ও ফলাফল" else "Suggestions & Results"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (existingPayeesWithCount.isNotEmpty()) {
                        Text(
                            text = "${existingPayeesWithCount.size} total",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Suggestions List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option 1: "Use as New Entry" if search text is not in existing items
                    if (showCreateNewOption) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedName = trimmedSearch
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "New",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "নতুন এন্ট্রি হিসেবে যোগ করুন:" else "Create / Use as new entry:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "\"$trimmedSearch\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    if (selectedName == trimmedSearch) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Existing entries
                    if (filteredSuggestions.isEmpty() && !showCreateNewOption) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোন পূর্ববর্তী এন্ট্রি পাওয়া যায়নি" else "No previous entries found",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        items(filteredSuggestions) { (payeeName, countOccurrences) ->
                            val isSelected = selectedName.equals(payeeName, ignoreCase = true) ||
                                (selectedName.isBlank() && searchQuery.equals(payeeName, ignoreCase = true))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                shadowElevation = if (isSelected) 1.dp else 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = payeeName
                                        selectedName = payeeName
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = payeeName,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (countOccurrences > 0) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "$countOccurrences বার ব্যবহৃত" else "Used in $countOccurrences transactions",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
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
            Button(
                onClick = {
                    if (activeName.isNotBlank()) {
                        onConfirm(activeName)
                    }
                },
                enabled = activeName.isNotBlank()
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
private fun BatchChangeAmountDialog(
    count: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var showCalc by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Amount ($count items)", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("New Amount") },
                trailingIcon = {
                    IconButton(onClick = { showCalc = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Calc")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull() ?: 0.0
                    if (parsed > 0) onConfirm(parsed)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showCalc) {
        PopupCalculatorDialog(
            initialValue = amountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showCalc = false },
            onValueConfirmed = { value ->
                amountText = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
                showCalc = false
            }
        )
    }
}

@Composable
private fun BatchAddLabelDialog(
    count: Int,
    allTransactions: List<TransactionWithDetails>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var labelText by remember { mutableStateOf("") }
    val existingLabels = remember(allTransactions) {
        allTransactions.mapNotNull { it.transaction.referenceNo.takeIf { s -> s.isNotBlank() } }.distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add / Change Label ($count items)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Label / Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (existingLabels.isNotEmpty()) {
                    Text("Existing Labels:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingLabels.take(4).forEach { ex ->
                            AssistChip(
                                onClick = { labelText = ex },
                                label = { Text("#$ex", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(labelText.trim()) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BatchSelectCategoryDialog(
    categories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelect: (Category, Category?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentCategories = remember(categories) { categories.filter { it.parentId == null } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Category...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    parentCategories.forEach { parent ->
                        val subCats = categories.filter { it.parentId == parent.id }
                        val matchesSearch = searchQuery.isBlank() ||
                                parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                                subCats.any { it.nameEn.contains(searchQuery, ignoreCase = true) }

                        if (matchesSearch) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(parent, null) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(parent.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            subCats.forEach { sub ->
                                if (searchQuery.isBlank() || sub.nameEn.contains(searchQuery, ignoreCase = true)) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelect(parent, sub) }
                                                .padding(start = 20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(sub.iconName),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(sub.nameEn, fontSize = 12.sp)
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BatchSelectAccountDialog(
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelect: (Account) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(accounts, searchQuery) {
        if (searchQuery.isBlank()) accounts else accounts.filter { it.nameEn.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Account...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { acc ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(acc) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(acc.iconName),
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(acc.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(acc.type.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
