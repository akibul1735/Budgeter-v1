package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AttachFile
import com.example.ui.components.ExportMenuButton
import com.example.util.DateUtils
import com.example.util.ExportFormat
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.PdfPrintHelper
import com.example.util.TabExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

enum class AccountDetailTab {
    TABLE,
    CHART
}

enum class ChartDateRange(val labelEn: String, val labelBn: String) {
    LAST_7_DAYS("Last 7 Days", "বিগত ৭ দিন"),
    LAST_30_DAYS("Last 30 Days", "বিগত ৩০ দিন"),
    THIS_MONTH("This Month", "চলতি মাস"),
    LAST_90_DAYS("Last 90 Days", "বিগত ৯০ দিন"),
    THIS_YEAR("This Year", "চলতি বছর"),
    ALL_TIME("All Time", "সব সময়")
}

enum class ChartFrequency(val labelEn: String, val labelBn: String) {
    DAILY("Daily", "দৈনিক"),
    WEEKLY("Weekly", "সাপ্তাহিক"),
    MONTHLY("Monthly", "মাসিক")
}

enum class ChartVisualType {
    BAR,
    LINE
}

data class AccountDetailFilterState(
    val nameQuery: String = "",
    val amountFrom: Double? = null,
    val amountTo: Double? = null,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedCategoryGroupIds: Set<Long> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val selectedStatuses: Set<TransactionStatus> = emptySet(),
    val selectedTypes: Set<TransactionType> = emptySet(),
    val dateRange: ChartDateRange = ChartDateRange.ALL_TIME
) {
    val isActive: Boolean
        get() = nameQuery.isNotBlank() ||
                amountFrom != null ||
                amountTo != null ||
                selectedCategoryIds.isNotEmpty() ||
                selectedCategoryGroupIds.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                selectedStatuses.isNotEmpty() ||
                selectedTypes.isNotEmpty() ||
                dateRange != ChartDateRange.ALL_TIME

    val activeCount: Int
        get() {
            var count = 0
            if (nameQuery.isNotBlank()) count++
            if (amountFrom != null || amountTo != null) count++
            if (selectedCategoryIds.isNotEmpty() || selectedCategoryGroupIds.isNotEmpty()) count++
            if (selectedLabels.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            if (selectedTypes.isNotEmpty()) count++
            if (dateRange != ChartDateRange.ALL_TIME) count++
            return count
        }
}

private data class BalancePoint(
    val epochMs: Long,
    val dateLabel: String,
    val balance: Double,
    val isProjected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    account: Account,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    allTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionForAccount: (Account) -> Unit,
    onEditAccount: (Account) -> Unit,
    onSaveTransaction: (Transaction) -> Unit,
    onUpdateTransactions: (List<Transaction>) -> Unit,
    onDeleteTransactions: (List<Transaction>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AccountDetailTab.TABLE) }
    var filterState by remember { mutableStateOf(AccountDetailFilterState()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showReconcileSheet by remember { mutableStateOf(false) }

    // Multi-selection states (Image 1)
    var selectedTxIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchNameDialog by remember { mutableStateOf(false) }
    var showBatchDateDialog by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }
    var showBatchAccountDialog by remember { mutableStateOf(false) }
    var showBatchAmountDialog by remember { mutableStateOf(false) }
    var showBatchLabelDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirmDialog by remember { mutableStateOf(false) }

    // State for single-transaction quick reconcile long-press
    var selectedTxForQuickAction by remember { mutableStateOf<TransactionWithDetails?>(null) }

    // Sub-account IDs if account is a parent group
    val subAccountIds = remember(account, allAccounts) {
        allAccounts.filter { it.parentId == account.id }.map { it.id }.toSet()
    }
    val allRelevantAccountIds = remember(account, subAccountIds) {
        subAccountIds + account.id
    }

    // Filter transactions relevant to this account (debit or credit)
    val accountTransactions = remember(account, allRelevantAccountIds, allTransactions) {
        allTransactions.filter { item ->
            val tx = item.transaction
            tx.status != TransactionStatus.VOID &&
                    (allRelevantAccountIds.contains(tx.debitAccountId) ||
                            allRelevantAccountIds.contains(tx.creditAccountId))
        }.sortedWith(
            compareByDescending<TransactionWithDetails> { it.transaction.dateEpochMs }
                .thenByDescending { it.transaction.id }
        )
    }

    // Chronological Running Balance map
    val (runningBalanceMap, currentEndingBalance) = remember(accountTransactions, account, allRelevantAccountIds, allAccounts) {
        val subAccountInitial = if (account.parentId == null) {
            allAccounts.filter { it.parentId == account.id }.sumOf { it.initialBalance }
        } else 0.0
        val baseInitial = if (account.type == AccountType.LIABILITY) -(account.initialBalance + subAccountInitial) else (account.initialBalance + subAccountInitial)

        val chronological = accountTransactions.sortedWith(
            compareBy<TransactionWithDetails> { it.transaction.dateEpochMs }
                .thenBy { it.transaction.id }
        )

        var currentRunning = baseInitial
        val map = mutableMapOf<Long, Double>()

        chronological.forEach { item ->
            val tx = item.transaction
            val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
            val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)

            if (isDebit && !isCredit) {
                currentRunning += tx.amount
            } else if (isCredit && !isDebit) {
                currentRunning -= tx.amount
            }
            map[tx.id] = currentRunning
        }
        Pair(map, currentRunning)
    }

    // Extract all unique labels/tags from transaction notes
    val availableLabels = remember(accountTransactions) {
        val set = mutableSetOf<String>()
        val hashtagRegex = Regex("""#(\w+)""")
        accountTransactions.forEach { item ->
            val note = item.transaction.note
            val matches = hashtagRegex.findAll(note)
            matches.forEach { set.add(it.groupValues[1]) }
            if (set.isEmpty() && note.isNotBlank() && note.length <= 20 && !note.contains(" ")) {
                set.add(note.trim())
            }
        }
        set.toList().sorted()
    }

    // Apply FilterState
    val displayedTransactions = remember(accountTransactions, filterState, allCategories) {
        val categoryMap = allCategories.associateBy { it.id }
        accountTransactions.filter { item ->
            val tx = item.transaction

            // Name / Note query
            if (filterState.nameQuery.isNotBlank()) {
                val q = filterState.nameQuery.trim().lowercase()
                val matchPayee = tx.payeeOrPayer.lowercase().contains(q)
                val matchNote = tx.note.lowercase().contains(q)
                val matchRef = tx.referenceNo.lowercase().contains(q)
                val matchCat = item.category?.nameEn?.lowercase()?.contains(q) == true ||
                        item.category?.nameBn?.lowercase()?.contains(q) == true
                if (!matchPayee && !matchNote && !matchRef && !matchCat) return@filter false
            }

            // Amount Range
            val absAmt = abs(tx.amount)
            val minAmt = filterState.amountFrom
            val maxAmt = filterState.amountTo
            if (minAmt != null && absAmt < minAmt) return@filter false
            if (maxAmt != null && absAmt > maxAmt) return@filter false

            // Category & Category Groups
            if (filterState.selectedCategoryIds.isNotEmpty()) {
                val catId = tx.categoryId
                val subCatId = tx.subCategoryId
                if (catId == null || (!filterState.selectedCategoryIds.contains(catId) &&
                            (subCatId == null || !filterState.selectedCategoryIds.contains(subCatId)))) {
                    return@filter false
                }
            }
            if (filterState.selectedCategoryGroupIds.isNotEmpty()) {
                val cat = item.category
                val parentId = cat?.parentId ?: cat?.id
                if (parentId == null || !filterState.selectedCategoryGroupIds.contains(parentId)) {
                    return@filter false
                }
            }

            // Labels
            if (filterState.selectedLabels.isNotEmpty()) {
                val hasMatch = filterState.selectedLabels.any { tag ->
                    tx.note.contains("#$tag", ignoreCase = true) || tx.note.contains(tag, ignoreCase = true)
                }
                if (!hasMatch) return@filter false
            }

            // Statuses
            if (filterState.selectedStatuses.isNotEmpty() && !filterState.selectedStatuses.contains(tx.status)) {
                return@filter false
            }

            // Types
            if (filterState.selectedTypes.isNotEmpty() && !filterState.selectedTypes.contains(tx.type)) {
                return@filter false
            }

            // Date Range
            val now = System.currentTimeMillis()
            val txTime = tx.dateEpochMs
            val cal = Calendar.getInstance()
            when (filterState.dateRange) {
                ChartDateRange.LAST_7_DAYS -> if (txTime < now - (7L * 86400000L)) return@filter false
                ChartDateRange.LAST_30_DAYS -> if (txTime < now - (30L * 86400000L)) return@filter false
                ChartDateRange.THIS_MONTH -> {
                    val startOfMonth = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    if (txTime < startOfMonth) return@filter false
                }
                ChartDateRange.LAST_90_DAYS -> if (txTime < now - (90L * 86400000L)) return@filter false
                ChartDateRange.THIS_YEAR -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    if (txTime < cal.timeInMillis) return@filter false
                }
                ChartDateRange.ALL_TIME -> {}
            }

            true
        }
    }

    val accountName = remember(account, languageMode) {
        if (languageMode == LanguageMode.BANGLA && account.nameBn.isNotBlank()) account.nameBn else account.nameEn
    }

    // Calculate sum of selected transactions for top bar display (Image 1: e.g. -BDT 6,000.00)
    val selectedAmountText = remember(selectedTxIds, displayedTransactions, account, allRelevantAccountIds, languageMode) {
        if (selectedTxIds.isEmpty()) ""
        else {
            val selectedList = displayedTransactions.filter { selectedTxIds.contains(it.transaction.id) }
            val totalNet = selectedList.sumOf { item ->
                val tx = item.transaction
                val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
                val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)
                val sign = if (account.type == AccountType.ASSET) {
                    if (isDebit && !isCredit) 1.0 else if (isCredit && !isDebit) -1.0 else 0.0
                } else {
                    if (isCredit && !isDebit) 1.0 else if (isDebit && !isCredit) -1.0 else 0.0
                }
                tx.amount * sign
            }
            val formatted = LanguageHelper.formatCurrency(abs(totalNet), languageMode)
            when {
                totalNet < 0 -> "-$formatted"
                totalNet > 0 -> "+$formatted"
                else -> formatted
            }
        }
    }

    // Back handler exits selection mode
    BackHandler(enabled = selectedTxIds.isNotEmpty()) {
        selectedTxIds = emptySet()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_detail_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selectedTxIds.isNotEmpty()) {
                AccountDetailSelectionTopAppBar(
                    selectedAmountText = selectedAmountText,
                    languageMode = languageMode,
                    onBack = { selectedTxIds = emptySet() },
                    onSelectAll = {
                        val displayedIds = displayedTransactions.map { it.transaction.id }.toSet()
                        if (selectedTxIds.containsAll(displayedIds)) {
                            selectedTxIds = emptySet()
                        } else {
                            selectedTxIds = displayedIds
                        }
                    },
                    onDuplicate = {
                        val toDuplicate = displayedTransactions.filter { selectedTxIds.contains(it.transaction.id) }
                        toDuplicate.forEach { item ->
                            val orig = item.transaction
                            val copyTx = orig.copy(
                                id = 0L,
                                note = if (orig.note.isNotBlank()) "${orig.note} (Copy)" else "Copy",
                                createdAt = System.currentTimeMillis()
                            )
                            onSaveTransaction(copyTx)
                        }
                        Toast.makeText(context, "Duplicated ${toDuplicate.size} transactions", Toast.LENGTH_SHORT).show()
                        selectedTxIds = emptySet()
                    },
                    onExport = { format ->
                        val selectedList = displayedTransactions.filter { selectedTxIds.contains(it.transaction.id) }
                        when (format) {
                            ExportFormat.PDF -> {
                                val html = buildAccountStatementHtml(
                                    account = account,
                                    accountName = accountName,
                                    transactions = selectedList,
                                    runningBalanceMap = runningBalanceMap,
                                    currentBalance = currentEndingBalance,
                                    filterState = filterState,
                                    languageMode = languageMode
                                )
                                PdfPrintHelper.printHtml(
                                    context = context,
                                    jobName = "Statement_${account.nameEn.replace(" ", "_")}_Selected",
                                    htmlContent = html,
                                    isLandscape = false
                                )
                            }
                            ExportFormat.CSV, ExportFormat.HTML, ExportFormat.JSON -> {
                                val summary = "${accountName} (${selectedList.size} ${if (languageMode == LanguageMode.BANGLA) "টি নির্বাচিত লেনদেন" else "Selected Transactions"})"
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = selectedList,
                                    filterSummary = summary,
                                    languageMode = languageMode
                                )
                            }
                        }
                    },
                    onChangeName = { showBatchNameDialog = true },
                    onChangeDate = { showBatchDateDialog = true },
                    onChangeCategory = { showBatchCategoryDialog = true },
                    onChangeAccount = { showBatchAccountDialog = true },
                    onChangeAmount = { showBatchAmountDialog = true },
                    onAddLabel = { showBatchLabelDialog = true },
                    onReconcile = {
                        val toReconcile = displayedTransactions
                            .filter { selectedTxIds.contains(it.transaction.id) }
                            .map { it.transaction.copy(status = TransactionStatus.RECONCILED) }
                        if (toReconcile.isNotEmpty()) {
                            onUpdateTransactions(toReconcile)
                            Toast.makeText(context, "Marked ${toReconcile.size} transactions as Reconciled", Toast.LENGTH_SHORT).show()
                        }
                        selectedTxIds = emptySet()
                    },
                    onDelete = { showBatchDeleteConfirmDialog = true }
                )
            } else {
                AccountDetailTopAppBar(
                    accountName = accountName,
                    isFilterActive = filterState.isActive,
                    activeFilterCount = filterState.activeCount,
                    languageMode = languageMode,
                    onBack = onDismiss,
                    onFilterClick = { showFilterDialog = true },
                    onExport = { format ->
                        when (format) {
                            ExportFormat.PDF -> {
                                val html = buildAccountStatementHtml(
                                    account = account,
                                    accountName = accountName,
                                    transactions = displayedTransactions,
                                    runningBalanceMap = runningBalanceMap,
                                    currentBalance = currentEndingBalance,
                                    filterState = filterState,
                                    languageMode = languageMode
                                )
                                PdfPrintHelper.printHtml(
                                    context = context,
                                    jobName = "Statement_${account.nameEn.replace(" ", "_")}",
                                    htmlContent = html,
                                    isLandscape = false
                                )
                            }
                            ExportFormat.CSV, ExportFormat.HTML, ExportFormat.JSON -> {
                                val filterSummary = if (filterState.isActive) {
                                    "${accountName} (Filtered: ${displayedTransactions.size} transactions)"
                                } else {
                                    "${accountName} (All: ${displayedTransactions.size} transactions)"
                                }
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = displayedTransactions,
                                    filterSummary = filterSummary,
                                    languageMode = languageMode
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            AccountDetailBottomNavBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        },
        floatingActionButton = {
            if (selectedTxIds.isEmpty()) {
                FloatingActionButton(
                    onClick = { onAddTransactionForAccount(account) },
                    containerColor = SolidIncome,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(56.dp)
                        .testTag("account_detail_fab_add")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Action Subheader: Latest Balance & "Edit" & "Reconcile" (Screenshot 1 top right)
            AccountDetailSubheaderRow(
                account = account,
                latestBalance = currentEndingBalance,
                languageMode = languageMode,
                onEditAccount = { onEditAccount(account) },
                onReconcile = { showReconcileSheet = true }
            )

            // Active Filter Chips Bar (if any filter is applied)
            if (filterState.isActive) {
                AccountActiveFilterChips(
                    filterState = filterState,
                    onClearFilters = { filterState = AccountDetailFilterState() },
                    onOpenFilterDialog = { showFilterDialog = true }
                )
            }

            // Main Content: Table or Chart
            when (selectedTab) {
                AccountDetailTab.TABLE -> {
                    AccountDetailTableTab(
                        account = account,
                        transactions = displayedTransactions,
                        runningBalanceMap = runningBalanceMap,
                        languageMode = languageMode,
                        selectedTxIds = selectedTxIds,
                        onEditTransaction = onEditTransaction,
                        onToggleSelectTransaction = { txId ->
                            selectedTxIds = if (selectedTxIds.contains(txId)) {
                                selectedTxIds - txId
                            } else {
                                selectedTxIds + txId
                            }
                        }
                    )
                }
                AccountDetailTab.CHART -> {
                    AccountDetailChartTab(
                        account = account,
                        accountTransactions = accountTransactions,
                        allAccounts = allAccounts,
                        allRelevantAccountIds = allRelevantAccountIds,
                        languageMode = languageMode
                    )
                }
            }
        }
    }

    // Reconcile Bottom Sheet (Screenshot 3 / Image 2)
    if (showReconcileSheet) {
        AccountReconcileBottomSheet(
            account = account,
            latestBalance = currentEndingBalance,
            languageMode = languageMode,
            onDismiss = { showReconcileSheet = false },
            onConfirm = { targetBalance ->
                val diff = targetBalance - currentEndingBalance
                if (abs(diff) >= 0.01) {
                    val isAsset = account.type == AccountType.ASSET
                    val isIncome = if (isAsset) diff > 0 else diff < 0
                    val amount = abs(diff)

                    val adjustmentTx = if (isIncome) {
                        Transaction(
                            type = TransactionType.INCOME,
                            amount = amount,
                            debitAccountId = account.id,
                            creditAccountId = null,
                            note = "Balance Adjustment (Reconciliation)",
                            payeeOrPayer = "Adjustment",
                            status = TransactionStatus.RECONCILED,
                            dateEpochMs = System.currentTimeMillis()
                        )
                    } else {
                        Transaction(
                            type = TransactionType.EXPENSE,
                            amount = amount,
                            debitAccountId = null,
                            creditAccountId = account.id,
                            note = "Balance Adjustment (Reconciliation)",
                            payeeOrPayer = "Adjustment",
                            status = TransactionStatus.RECONCILED,
                            dateEpochMs = System.currentTimeMillis()
                        )
                    }
                    onSaveTransaction(adjustmentTx)
                }

                Toast.makeText(
                    context,
                    if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স সমন্বয় সম্পন্ন হয়েছে" else "Account balance reconciled successfully",
                    Toast.LENGTH_SHORT
                ).show()
                showReconcileSheet = false
            }
        )
    }

    // Date Picker for batch date change
    if (showBatchDateDialog) {
        val cal = Calendar.getInstance()
        val firstSelected = displayedTransactions.firstOrNull { selectedTxIds.contains(it.transaction.id) }
        if (firstSelected != null) {
            cal.timeInMillis = firstSelected.transaction.dateEpochMs
        }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance()
                chosenCal.set(year, month, dayOfMonth)
                val newEpochMs = chosenCal.timeInMillis
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { it.transaction.copy(dateEpochMs = newEpochMs) }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Updated date for ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchDateDialog = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showBatchDateDialog = false }
            show()
        }
    }

    // Batch Change Name Dialog
    if (showBatchNameDialog) {
        val firstItemName = displayedTransactions.firstOrNull { selectedTxIds.contains(it.transaction.id) }?.transaction?.payeeOrPayer ?: ""
        BatchChangeNameDialog(
            initialName = firstItemName,
            onDismiss = { showBatchNameDialog = false },
            onConfirm = { newName ->
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { it.transaction.copy(payeeOrPayer = newName) }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Updated name for ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchNameDialog = false
            }
        )
    }

    // Batch Change Category Dialog
    if (showBatchCategoryDialog) {
        BatchChangeCategoryDialog(
            allCategories = allCategories,
            languageMode = languageMode,
            onDismiss = { showBatchCategoryDialog = false },
            onSelectCategory = { cat ->
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { it.transaction.copy(categoryId = cat.id, subCategoryId = null) }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Updated category for ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchCategoryDialog = false
            }
        )
    }

    // Batch Change Account Dialog
    if (showBatchAccountDialog) {
        BatchChangeAccountDialog(
            allAccounts = allAccounts,
            languageMode = languageMode,
            onDismiss = { showBatchAccountDialog = false },
            onSelectAccount = { acc ->
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { item ->
                        val tx = item.transaction
                        if (allRelevantAccountIds.contains(tx.debitAccountId)) {
                            tx.copy(debitAccountId = acc.id)
                        } else if (allRelevantAccountIds.contains(tx.creditAccountId)) {
                            tx.copy(creditAccountId = acc.id)
                        } else tx
                    }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Updated account for ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchAccountDialog = false
            }
        )
    }

    // Batch Change Amount Dialog
    if (showBatchAmountDialog) {
        BatchChangeAmountDialog(
            onDismiss = { showBatchAmountDialog = false },
            onConfirm = { newAmount ->
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { it.transaction.copy(amount = newAmount) }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Updated amount for ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchAmountDialog = false
            }
        )
    }

    // Batch Add Label Dialog
    if (showBatchLabelDialog) {
        BatchAddLabelDialog(
            onDismiss = { showBatchLabelDialog = false },
            onConfirm = { label ->
                val cleanTag = "#" + label.trim().removePrefix("#")
                val toUpdate = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { item ->
                        val tx = item.transaction
                        val updatedNote = if (tx.note.contains(cleanTag)) tx.note
                        else if (tx.note.isBlank()) cleanTag
                        else "${tx.note} $cleanTag"
                        tx.copy(note = updatedNote)
                    }
                if (toUpdate.isNotEmpty()) {
                    onUpdateTransactions(toUpdate)
                    Toast.makeText(context, "Added label to ${toUpdate.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchLabelDialog = false
            }
        )
    }

    // Batch Delete Confirm Dialog
    if (showBatchDeleteConfirmDialog) {
        BatchDeleteConfirmDialog(
            count = selectedTxIds.size,
            onDismiss = { showBatchDeleteConfirmDialog = false },
            onConfirm = {
                val toDelete = displayedTransactions
                    .filter { selectedTxIds.contains(it.transaction.id) }
                    .map { it.transaction }
                if (toDelete.isNotEmpty()) {
                    onDeleteTransactions(toDelete)
                    Toast.makeText(context, "Deleted ${toDelete.size} transactions", Toast.LENGTH_SHORT).show()
                }
                selectedTxIds = emptySet()
                showBatchDeleteConfirmDialog = false
            }
        )
    }

    // Filter Dialog ("name, amount from and to, category, categories group, labels, status")
    if (showFilterDialog) {
        AccountDetailFilterDialog(
            currentState = filterState,
            allCategories = allCategories,
            availableLabels = availableLabels,
            languageMode = languageMode,
            onApply = { newState ->
                filterState = newState
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Single Transaction Quick Action / Reconcile Dialog (on long click)
    selectedTxForQuickAction?.let { item ->
        val tx = item.transaction
        val isReconciled = tx.status == TransactionStatus.RECONCILED

        Dialog(onDismissRequest = { selectedTxForQuickAction = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else (item.category?.nameEn ?: "Transaction"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date(tx.dateEpochMs))} • ${LanguageHelper.formatCurrency(abs(tx.amount), languageMode)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Reconciled button
                    Button(
                        onClick = {
                            val newStatus = if (isReconciled) TransactionStatus.CLEARED else TransactionStatus.RECONCILED
                            onSaveTransaction(tx.copy(status = newStatus))
                            selectedTxForQuickAction = null
                            val msg = if (newStatus == TransactionStatus.RECONCILED) {
                                "Marked as Reconciled"
                            } else "Reconciled status removed"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReconciled) MaterialTheme.colorScheme.surfaceVariant else SolidIncome
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (isReconciled) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isReconciled) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isReconciled) "Remove Reconciled Status" else "Mark as Reconciled",
                            color = if (isReconciled) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val targetTx = tx
                            selectedTxForQuickAction = null
                            onEditTransaction(targetTx)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Transaction")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { selectedTxForQuickAction = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOP APP BAR
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountDetailTopAppBar(
    accountName: String,
    isFilterActive: Boolean,
    activeFilterCount: Int,
    languageMode: LanguageMode,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("account_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = accountName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Filter Icon with active badge
            Box {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.testTag("account_detail_filter_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = if (isFilterActive) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(SolidIncome),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$activeFilterCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Export Menu Button (PDF, CSV, HTML, JSON)
            ExportMenuButton(
                onExport = onExport,
                languageMode = languageMode,
                testTag = "account_detail_export_btn"
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SUBHEADER: Latest Balance & "Edit" and "Reconcile"
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountDetailSubheaderRow(
    account: Account,
    latestBalance: Double,
    languageMode: LanguageMode,
    onEditAccount: () -> Unit,
    onReconcile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স:" else "Balance:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = LanguageHelper.formatCurrency(latestBalance, languageMode),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (latestBalance >= 0) SolidIncome else SolidExpense
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onEditAccount,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("account_edit_btn")
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সম্পাদনা" else "Edit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SolidIncome
                )
            }

            TextButton(
                onClick = onReconcile,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("account_reconcile_btn")
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "রিকনসাইল" else "Reconcile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SolidIncome
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ACTIVE FILTER CHIPS BAR
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountActiveFilterChips(
    filterState: AccountDetailFilterState,
    onClearFilters: () -> Unit,
    onOpenFilterDialog: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SolidIncome.copy(alpha = 0.15f),
                modifier = Modifier.clickable { onClearFilters() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear all (${filterState.activeCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SolidIncome)
                }
            }
        }

        if (filterState.nameQuery.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Query: \"${filterState.nameQuery}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (filterState.amountFrom != null || filterState.amountTo != null) {
            val from = filterState.amountFrom?.let { "$it" } ?: "0"
            val to = filterState.amountTo?.let { "$it" } ?: "∞"
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Amount: $from - $to",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (filterState.selectedStatuses.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Status: ${filterState.selectedStatuses.joinToString { it.name }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (filterState.dateRange != ChartDateRange.ALL_TIME) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = filterState.dateRange.labelEn,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TABLE TAB (Matching LedgerScreen Grouped by Date Layout)
// -------------------------------------------------------------------------------------------------
private data class AccountDayGroupedTransactions(
    val dayEpochMs: Long,
    val transactions: List<TransactionWithDetails>,
    val dayNet: Double
)

@Composable
private fun AccountDetailTableTab(
    account: Account,
    transactions: List<TransactionWithDetails>,
    runningBalanceMap: Map<Long, Double>,
    languageMode: LanguageMode,
    selectedTxIds: Set<Long>,
    onEditTransaction: (Transaction) -> Unit,
    onToggleSelectTransaction: (Long) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No transactions found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Try adjusting your filters or add a new transaction",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        val isSelectionMode = selectedTxIds.isNotEmpty()

        val groupedByDay = remember(transactions, account) {
            transactions.groupBy { DateUtils.getStartOfDay(it.transaction.dateEpochMs) }
                .map { (dayEpochMs, dayTxList) ->
                    var dayInflow = 0.0
                    var dayOutflow = 0.0
                    for (item in dayTxList) {
                        val tx = item.transaction
                        val isDebit = tx.debitAccountId == account.id
                        val isCredit = tx.creditAccountId == account.id
                        val isIncrease = if (tx.type == TransactionType.TRANSFER) {
                            val isSource = tx.creditAccountId == account.id
                            if (isSource) tx.amount < 0 else tx.amount >= 0
                        } else {
                            if (isDebit && !isCredit) tx.amount >= 0 else if (isCredit && !isDebit) tx.amount < 0 else false
                        }
                        if (isIncrease) dayInflow += abs(tx.amount) else dayOutflow += abs(tx.amount)
                    }
                    AccountDayGroupedTransactions(
                        dayEpochMs = dayEpochMs,
                        transactions = dayTxList,
                        dayNet = dayInflow - dayOutflow
                    )
                }
                .sortedByDescending { it.dayEpochMs }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groupedByDay, key = { "day_${it.dayEpochMs}" }) { dayGroup ->
                // Day Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 2.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDayHeader(dayGroup.dayEpochMs, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val netSign = if (dayGroup.dayNet > 0) "+" else if (dayGroup.dayNet < 0) "−" else ""
                        val netColor = if (dayGroup.dayNet > 0) SolidIncome else if (dayGroup.dayNet < 0) SolidExpense else MaterialTheme.colorScheme.outline
                        Text(
                            text = "$netSign${LanguageHelper.formatCurrency(abs(dayGroup.dayNet), languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                    }
                }

                // Day Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        dayGroup.transactions.forEachIndexed { index, item ->
                            val tx = item.transaction
                            val runningBal = runningBalanceMap[tx.id]
                            val isSelected = selectedTxIds.contains(tx.id)

                            if (tx.type == TransactionType.TRANSFER) {
                                val isSource = tx.creditAccountId == account.id
                                val transferTitle = if (tx.payeeOrPayer.isNotBlank()) {
                                    tx.payeeOrPayer
                                } else {
                                    val otherAcc = if (isSource) item.debitAccount else item.creditAccount
                                    otherAcc?.localizedName(languageMode) ?: LanguageHelper.getString("transfer", languageMode)
                                }

                                val isIncrease = if (isSource) tx.amount < 0 else tx.amount >= 0
                                val overrideSign = if (isIncrease) "+" else "−"
                                val overrideAmtColor = if (isIncrease) SolidIncome else SolidExpense

                                TransactionRowItem(
                                    item = item,
                                    languageMode = languageMode,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    accountName = account.localizedName(languageMode),
                                    accountBalance = runningBal,
                                    overrideTitle = transferTitle,
                                    overrideSubtitle = "(${LanguageHelper.getString("transfer", languageMode)})",
                                    overrideSign = overrideSign,
                                    overrideAmtColor = overrideAmtColor,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelectTransaction(tx.id)
                                        } else {
                                            onEditTransaction(tx)
                                        }
                                    },
                                    onLongClick = {
                                        onToggleSelectTransaction(tx.id)
                                    }
                                )
                            } else {
                                val isDebit = tx.debitAccountId == account.id
                                val isCredit = tx.creditAccountId == account.id
                                val isIncrease = if (isDebit && !isCredit) tx.amount >= 0 else if (isCredit && !isDebit) tx.amount < 0 else false

                                val overrideSign = if (isIncrease) "+" else "−"
                                val overrideAmtColor = if (isIncrease) SolidIncome else SolidExpense

                                TransactionRowItem(
                                    item = item,
                                    languageMode = languageMode,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    accountName = account.localizedName(languageMode),
                                    accountBalance = runningBal,
                                    overrideSign = overrideSign,
                                    overrideAmtColor = overrideAmtColor,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelectTransaction(tx.id)
                                        } else {
                                            onEditTransaction(tx)
                                        }
                                    },
                                    onLongClick = {
                                        onToggleSelectTransaction(tx.id)
                                    }
                                )
                            }

                            if (index < dayGroup.transactions.size - 1) {
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

// -------------------------------------------------------------------------------------------------
// CHART TAB (Matching Screenshot 2)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountDetailChartTab(
    account: Account,
    accountTransactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allRelevantAccountIds: Set<Long>,
    languageMode: LanguageMode
) {
    var visualType by remember { mutableStateOf(ChartVisualType.BAR) }
    var futureProjection by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(ChartDateRange.LAST_30_DAYS) }
    var selectedFrequency by remember { mutableStateOf(ChartFrequency.DAILY) }

    var expandedRangeDropdown by remember { mutableStateOf(false) }
    var expandedFreqDropdown by remember { mutableStateOf(false) }

    // Build timeline points based on range and frequency
    val chartPoints = remember(
        account,
        accountTransactions,
        allAccounts,
        allRelevantAccountIds,
        selectedRange,
        selectedFrequency,
        futureProjection
    ) {
        computeTimelinePoints(
            account = account,
            transactions = accountTransactions,
            allAccounts = allAccounts,
            allRelevantAccountIds = allRelevantAccountIds,
            range = selectedRange,
            frequency = selectedFrequency,
            includeProjection = futureProjection
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Chart Canvas Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                if (chartPoints.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Not enough data to render chart", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    BalanceTimelineChartCanvas(
                        points = chartPoints,
                        visualType = visualType,
                        languageMode = languageMode
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Controls Row: Bar/Line Toggle + Future Projection Switch (Screenshot 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Type Toggle Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (visualType == ChartVisualType.BAR) SolidIncome else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { visualType = ChartVisualType.BAR }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (visualType == ChartVisualType.BAR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (visualType == ChartVisualType.LINE) SolidIncome else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { visualType = ChartVisualType.LINE }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Line Chart",
                            tint = if (visualType == ChartVisualType.LINE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Future Projection Switch (Screenshot 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Future Projection",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = futureProjection,
                    onCheckedChange = { futureProjection = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SolidIncome
                    ),
                    modifier = Modifier.testTag("chart_future_projection_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Range Selector (Screenshot 2)
        Column {
            Text(
                text = "Date Range",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedCard(
                onClick = { expandedRangeDropdown = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) selectedRange.labelBn else selectedRange.labelEn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text("▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }

                DropdownMenu(
                    expanded = expandedRangeDropdown,
                    onDismissRequest = { expandedRangeDropdown = false }
                ) {
                    ChartDateRange.values().forEach { range ->
                        DropdownMenuItem(
                            text = { Text(if (languageMode == LanguageMode.BANGLA) range.labelBn else range.labelEn) },
                            onClick = {
                                selectedRange = range
                                expandedRangeDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Frequency Selector (Screenshot 2)
        Column {
            Text(
                text = "Frequency",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedCard(
                onClick = { expandedFreqDropdown = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) selectedFrequency.labelBn else selectedFrequency.labelEn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text("▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }

                DropdownMenu(
                    expanded = expandedFreqDropdown,
                    onDismissRequest = { expandedFreqDropdown = false }
                ) {
                    ChartFrequency.values().forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(if (languageMode == LanguageMode.BANGLA) freq.labelBn else freq.labelEn) },
                            onClick = {
                                selectedFrequency = freq
                                expandedFreqDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Card for the selected timeline
        if (chartPoints.isNotEmpty()) {
            val startBal = chartPoints.first().balance
            val endBal = chartPoints.last().balance
            val netChange = endBal - startBal
            val minBal = chartPoints.minOf { it.balance }
            val maxBal = chartPoints.maxOf { it.balance }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Starting", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = LanguageHelper.formatCurrency(startBal, languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Net Change", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = (if (netChange >= 0) "+" else "") + LanguageHelper.formatCurrency(netChange, languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netChange >= 0) SolidIncome else SolidExpense
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ending", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = LanguageHelper.formatCurrency(endBal, languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// CANVAS CHART (Faithful Emerald Bars & Axes in Screenshot 2)
// -------------------------------------------------------------------------------------------------
@Composable
private fun BalanceTimelineChartCanvas(
    points: List<BalancePoint>,
    visualType: ChartVisualType,
    languageMode: LanguageMode
) {
    val barColor = Color(0xFF10B981) // Emerald Green matching Screenshot 2
    val projectedBarColor = Color(0xFF10B981).copy(alpha = 0.45f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val textColor = MaterialTheme.colorScheme.outline
    val baselineColor = MaterialTheme.colorScheme.outline
    val textArgb = textColor.toArgb()

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas

        val leftPadding = 50.dp.toPx()
        val bottomPadding = 36.dp.toPx()
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding

        val minVal = min(0.0, points.minOf { it.balance })
        val rawMaxVal = points.maxOf { it.balance }
        val maxVal = if (rawMaxVal <= minVal) minVal + 100.0 else rawMaxVal * 1.15 // 15% headroom
        val valRange = max(1.0, maxVal - minVal)

        // Draw horizontal grid lines & Y-axis labels
        val gridLinesCount = 4
        val paint = android.graphics.Paint().apply {
            color = textArgb
            textSize = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (i in 0..gridLinesCount) {
            val ratio = i.toFloat() / gridLinesCount
            val y = chartHeight - (ratio * chartHeight)
            val value = minVal + (ratio * valRange)

            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )

            val label = String.format(Locale.US, "%,.0f", value)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftPadding - 8.dp.toPx(),
                y + 3.dp.toPx(),
                paint
            )
        }

        // Draw Baseline (y = 0)
        val baselineY = chartHeight - (((0.0 - minVal) / valRange).toFloat() * chartHeight)
        drawLine(
            color = baselineColor,
            start = Offset(leftPadding, baselineY),
            end = Offset(size.width, baselineY),
            strokeWidth = 1.5.dp.toPx()
        )

        val count = points.size
        val stepX = chartWidth / count
        val barWidth = min(stepX * 0.65f, 16.dp.toPx())

        // Draw Bars or Line
        if (visualType == ChartVisualType.BAR) {
            points.forEachIndexed { index, pt ->
                val centerX = leftPadding + (index * stepX) + (stepX / 2f)
                val ptRatio = ((pt.balance - minVal) / valRange).toFloat().coerceIn(0f, 1f)
                val barTopY = chartHeight - (ptRatio * chartHeight)
                val barHeight = abs(baselineY - barTopY)
                val topY = min(baselineY, barTopY)

                val color = if (pt.isProjected) projectedBarColor else barColor

                drawRoundRect(
                    color = color,
                    topLeft = Offset(centerX - (barWidth / 2f), topY),
                    size = Size(barWidth, max(barHeight, 2.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        } else {
            // LINE CHART
            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, pt ->
                val x = leftPadding + (index * stepX) + (stepX / 2f)
                val ptRatio = ((pt.balance - minVal) / valRange).toFloat().coerceIn(0f, 1f)
                val y = chartHeight - (ptRatio * chartHeight)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, baselineY)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            val lastX = leftPadding + ((count - 1) * stepX) + (stepX / 2f)
            fillPath.lineTo(lastX, baselineY)
            fillPath.close()

            // Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(barColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            // Line Stroke
            drawPath(
                path = path,
                color = barColor,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // Draw X-axis date labels (e.g. "08/14", "08/19", "08/24", Screenshot 2)
        val xLabelPaint = android.graphics.Paint().apply {
            color = textColor.hashCode()
            textSize = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val stepLabelInterval = max(1, count / 7)
        points.forEachIndexed { index, pt ->
            if (index % stepLabelInterval == 0 || index == count - 1) {
                val x = leftPadding + (index * stepX) + (stepX / 2f)
                val y = size.height - 8.dp.toPx()

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(90f, x, y - 8.dp.toPx())
                drawContext.canvas.nativeCanvas.drawText(pt.dateLabel, x, y - 8.dp.toPx(), xLabelPaint)
                drawContext.canvas.nativeCanvas.restore()
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TIMELINE DATA CALCULATOR
// -------------------------------------------------------------------------------------------------
private fun computeTimelinePoints(
    account: Account,
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allRelevantAccountIds: Set<Long>,
    range: ChartDateRange,
    frequency: ChartFrequency,
    includeProjection: Boolean
): List<BalancePoint> {
    val subAccountInitial = if (account.parentId == null) {
        allAccounts.filter { it.parentId == account.id }.sumOf { it.initialBalance }
    } else 0.0
    val baseInitial = if (account.type == AccountType.LIABILITY) -(account.initialBalance + subAccountInitial) else (account.initialBalance + subAccountInitial)

    val chronological = transactions.sortedWith(
        compareBy<TransactionWithDetails> { it.transaction.dateEpochMs }
            .thenBy { it.transaction.id }
    )

    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()

    val startTimeMs = when (range) {
        ChartDateRange.LAST_7_DAYS -> now - (7L * 86400000L)
        ChartDateRange.LAST_30_DAYS -> now - (30L * 86400000L)
        ChartDateRange.THIS_MONTH -> DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        ChartDateRange.LAST_90_DAYS -> now - (90L * 86400000L)
        ChartDateRange.THIS_YEAR -> {
            val c = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            c.timeInMillis
        }
        ChartDateRange.ALL_TIME -> chronological.firstOrNull()?.transaction?.dateEpochMs ?: (now - 30L * 86400000L)
    }

    // Step size in milliseconds based on frequency
    val stepMs = when (frequency) {
        ChartFrequency.DAILY -> 86400000L
        ChartFrequency.WEEKLY -> 7L * 86400000L
        ChartFrequency.MONTHLY -> 30L * 86400000L
    }

    val points = mutableListOf<BalancePoint>()
    val dateFormat = SimpleDateFormat("MM/dd", Locale.US)

    var curTime = startTimeMs
    var txIdx = 0
    var runningBal = baseInitial

    // Bring running balance forward to startTimeMs
    while (txIdx < chronological.size && chronological[txIdx].transaction.dateEpochMs < startTimeMs) {
        val tx = chronological[txIdx].transaction
        val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
        val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)
        if (isDebit && !isCredit) runningBal += tx.amount
        else if (isCredit && !isDebit) runningBal -= tx.amount
        txIdx++
    }

    // Historical slots
    while (curTime <= now) {
        val nextSlotTime = curTime + stepMs
        while (txIdx < chronological.size && chronological[txIdx].transaction.dateEpochMs < nextSlotTime) {
            val tx = chronological[txIdx].transaction
            val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
            val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)
            if (isDebit && !isCredit) runningBal += tx.amount
            else if (isCredit && !isDebit) runningBal -= tx.amount
            txIdx++
        }

        points.add(
            BalancePoint(
                epochMs = curTime,
                dateLabel = dateFormat.format(Date(curTime)),
                balance = runningBal,
                isProjected = false
            )
        )
        curTime = nextSlotTime
    }

    // Future Projection (Screenshot 2 Future Projection Switch)
    if (includeProjection && points.isNotEmpty()) {
        val historicalDays = max(1L, (now - startTimeMs) / 86400000L)
        val totalNetChange = points.last().balance - points.first().balance
        val dailyAvgChange = totalNetChange / historicalDays

        var projTime = now + stepMs
        var projBal = points.last().balance
        val projectionDaysCount = when (range) {
            ChartDateRange.LAST_7_DAYS -> 7
            ChartDateRange.LAST_30_DAYS -> 15
            else -> 30
        }
        val projEnd = now + (projectionDaysCount * 86400000L)

        while (projTime <= projEnd) {
            val daysAdvanced = (stepMs / 86400000L).toDouble()
            projBal += (dailyAvgChange * daysAdvanced)
            points.add(
                BalancePoint(
                    epochMs = projTime,
                    dateLabel = dateFormat.format(Date(projTime)),
                    balance = projBal,
                    isProjected = true
                )
            )
            projTime += stepMs
        }
    }

    return points
}

// -------------------------------------------------------------------------------------------------
// RECONCILE BOTTOM SHEET (Screenshot 3 / Image 2)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountReconcileBottomSheet(
    account: Account,
    latestBalance: Double,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (targetBalance: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isPositive by remember { mutableStateOf(latestBalance >= 0) }
    var rawAmountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", abs(latestBalance))) }

    val parsedAmt = rawAmountText.toDoubleOrNull() ?: abs(latestBalance)
    val targetBal = if (isPositive) abs(parsedAmt) else -abs(parsedAmt)
    val diff = abs(targetBal - latestBalance)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("account_reconcile_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Title
            Text(
                text = "Update account balance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Latest Balance
            Text(
                text = "Latest Balance",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolidIncome
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = LanguageHelper.formatCurrency(latestBalance, languageMode),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. New Ending Balance
            Text(
                text = "New Ending Balance",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolidIncome
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Sign Toggle (+ / −)
                Surface(
                    shape = CircleShape,
                    color = SolidIncome,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { isPositive = !isPositive }
                        .testTag("reconcile_sign_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isPositive) "+" else "−",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Outlined Amount Box with Currency Prefix
                OutlinedTextField(
                    value = rawAmountText,
                    onValueChange = { rawAmountText = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(24.dp),
                    prefix = {
                        Text(
                            text = "BDT ",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolidIncome,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reconcile_amount_input")
                )
            }

            // Notice under amount when amount is changed (Image 2)
            if (diff >= 0.01) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "${LanguageHelper.formatCurrency(diff, languageMode)} পরিমাণের একটি সমন্বয় লেনদেন তৈরি করা হবে"
                    } else {
                        "An adjustment transaction amounting to ${LanguageHelper.formatCurrency(diff, languageMode)} will be created"
                    },
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Reconcile Section
            Text(
                text = "Reconcile",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolidIncome
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Information note (Screenshot 3 / Image 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Information: You can also long press transactions to reconcile.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Action Buttons (Cancel & OK)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reconcile_cancel_btn")
                ) {
                    Text("Cancel", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        val parsed = rawAmountText.toDoubleOrNull() ?: abs(latestBalance)
                        val target = if (isPositive) parsed else -parsed
                        onConfirm(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reconcile_ok_btn")
                ) {
                    Text("OK", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// FILTER DIALOG (Name, Amount From/To, Category, Category Group, Labels, Status)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDetailFilterDialog(
    currentState: AccountDetailFilterState,
    allCategories: List<Category>,
    availableLabels: List<String>,
    languageMode: LanguageMode,
    onApply: (AccountDetailFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var nameQuery by remember { mutableStateOf(currentState.nameQuery) }
    var amountFromText by remember { mutableStateOf(currentState.amountFrom?.let { "$it" } ?: "") }
    var amountToText by remember { mutableStateOf(currentState.amountTo?.let { "$it" } ?: "") }
    var selectedCatIds by remember { mutableStateOf(currentState.selectedCategoryIds) }
    var selectedGroupIds by remember { mutableStateOf(currentState.selectedCategoryGroupIds) }
    var selectedLabels by remember { mutableStateOf(currentState.selectedLabels) }
    var selectedStatuses by remember { mutableStateOf(currentState.selectedStatuses) }
    var selectedTypes by remember { mutableStateOf(currentState.selectedTypes) }
    var dateRange by remember { mutableStateOf(currentState.dateRange) }

    val parentCategories = remember(allCategories) {
        allCategories.filter { it.parentId == null }
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
                .fillMaxSize(0.88f)
                .padding(vertical = 12.dp)
                .testTag("account_filter_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Account Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Name / Payee / Note Search
                    Column {
                        Text("Search (Name, Note, Payee)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nameQuery,
                            onValueChange = { nameQuery = it },
                            placeholder = { Text("Filter by payee, note...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Amount Range (From / To)
                    Column {
                        Text("Amount Range", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = amountFromText,
                                onValueChange = { amountFromText = it },
                                label = { Text("From") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = amountToText,
                                onValueChange = { amountToText = it },
                                label = { Text("To") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Category Groups
                    if (parentCategories.isNotEmpty()) {
                        Column {
                            Text("Category Groups", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(parentCategories) { cat ->
                                    val isSelected = selectedGroupIds.contains(cat.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedGroupIds = if (isSelected) selectedGroupIds - cat.id else selectedGroupIds + cat.id
                                        },
                                        label = { Text(cat.nameEn, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidIncome,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 4. Specific Categories
                    if (allCategories.isNotEmpty()) {
                        Column {
                            Text("Categories", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(allCategories.take(15)) { cat ->
                                    val isSelected = selectedCatIds.contains(cat.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedCatIds = if (isSelected) selectedCatIds - cat.id else selectedCatIds + cat.id
                                        },
                                        label = { Text(cat.nameEn, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidIncome,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 5. Labels / Tags
                    if (availableLabels.isNotEmpty()) {
                        Column {
                            Text("Labels / Tags", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(availableLabels) { tag ->
                                    val isSelected = selectedLabels.contains(tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLabels = if (isSelected) selectedLabels - tag else selectedLabels + tag
                                        },
                                        label = { Text("#$tag", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidIncome,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 6. Transaction Status
                    Column {
                        Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TransactionStatus.values().forEach { st ->
                                val isSelected = selectedStatuses.contains(st)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedStatuses = if (isSelected) selectedStatuses - st else selectedStatuses + st
                                    },
                                    label = { Text(st.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 7. Transaction Type
                    Column {
                        Text("Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TransactionType.values().forEach { typ ->
                                val isSelected = selectedTypes.contains(typ)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTypes = if (isSelected) selectedTypes - typ else selectedTypes + typ
                                    },
                                    label = { Text(typ.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 8. Date Range
                    Column {
                        Text("Date Range", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SolidIncome)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ChartDateRange.values()) { r ->
                                val isSelected = dateRange == r
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { dateRange = r },
                                    label = { Text(r.labelEn, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            nameQuery = ""
                            amountFromText = ""
                            amountToText = ""
                            selectedCatIds = emptySet()
                            selectedGroupIds = emptySet()
                            selectedLabels = emptySet()
                            selectedStatuses = emptySet()
                            selectedTypes = emptySet()
                            dateRange = ChartDateRange.ALL_TIME
                        }
                    ) {
                        Text("Reset All", color = SolidExpense)
                    }

                    Button(
                        onClick = {
                            val from = amountFromText.toDoubleOrNull()
                            val to = amountToText.toDoubleOrNull()
                            onApply(
                                AccountDetailFilterState(
                                    nameQuery = nameQuery,
                                    amountFrom = from,
                                    amountTo = to,
                                    selectedCategoryIds = selectedCatIds,
                                    selectedCategoryGroupIds = selectedGroupIds,
                                    selectedLabels = selectedLabels,
                                    selectedStatuses = selectedStatuses,
                                    selectedTypes = selectedTypes,
                                    dateRange = dateRange
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// BOTTOM NAVIGATION BAR (Table vs Chart pill selector in Screenshot 1 & 2)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountDetailBottomNavBar(
    selectedTab: AccountDetailTab,
    onSelectTab: (AccountDetailTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Table" Tab Pill
                val isTable = selectedTab == AccountDetailTab.TABLE
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isTable) SolidIncome else Color.Transparent,
                    modifier = Modifier
                        .clickable { onSelectTab(AccountDetailTab.TABLE) }
                        .testTag("account_tab_table")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = if (isTable) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Table",
                            fontSize = 13.sp,
                            fontWeight = if (isTable) FontWeight.Bold else FontWeight.Medium,
                            color = if (isTable) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // "Chart" Tab Pill
                val isChart = selectedTab == AccountDetailTab.CHART
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isChart) SolidIncome else Color.Transparent,
                    modifier = Modifier
                        .clickable { onSelectTab(AccountDetailTab.CHART) }
                        .testTag("account_tab_chart")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (isChart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chart",
                            fontSize = 13.sp,
                            fontWeight = if (isChart) FontWeight.Bold else FontWeight.Medium,
                            color = if (isChart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PDF EXPORT HTML GENERATOR
// -------------------------------------------------------------------------------------------------
private fun buildAccountStatementHtml(
    account: Account,
    accountName: String,
    transactions: List<TransactionWithDetails>,
    runningBalanceMap: Map<Long, Double>,
    currentBalance: Double,
    filterState: AccountDetailFilterState,
    languageMode: LanguageMode
): String {
    val dateStr = SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.US).format(Date())

    val totalInflow = transactions.filter {
        val isDebit = it.transaction.debitAccountId == account.id
        val isCredit = it.transaction.creditAccountId == account.id
        if (account.type == AccountType.ASSET) (isDebit && !isCredit && it.transaction.amount >= 0)
        else (isDebit && !isCredit && it.transaction.amount < 0)
    }.sumOf { abs(it.transaction.amount) }

    val totalOutflow = transactions.filter {
        val isDebit = it.transaction.debitAccountId == account.id
        val isCredit = it.transaction.creditAccountId == account.id
        if (account.type == AccountType.ASSET) (isCredit && !isDebit && it.transaction.amount >= 0)
        else (isCredit && !isDebit && it.transaction.amount < 0)
    }.sumOf { abs(it.transaction.amount) }

    val filterSummary = if (filterState.isActive) {
        val parts = mutableListOf<String>()
        if (filterState.nameQuery.isNotBlank()) parts.add("Query: \"${filterState.nameQuery}\"")
        if (filterState.amountFrom != null || filterState.amountTo != null) parts.add("Amount: ${filterState.amountFrom ?: "0"} - ${filterState.amountTo ?: "∞"}")
        if (filterState.selectedStatuses.isNotEmpty()) parts.add("Status: ${filterState.selectedStatuses.joinToString { it.name }}")
        if (filterState.dateRange != ChartDateRange.ALL_TIME) parts.add("Range: ${filterState.dateRange.labelEn}")
        parts.joinToString(" • ")
    } else "All recorded transactions"

    val rowsHtml = StringBuilder()
    transactions.forEachIndexed { idx, item ->
        val tx = item.transaction
        val isDebit = tx.debitAccountId == account.id
        val isCredit = tx.creditAccountId == account.id
        val isIncrease = if (account.type == AccountType.ASSET) {
            if (isDebit && !isCredit) tx.amount >= 0 else if (isCredit && !isDebit) tx.amount < 0 else false
        } else {
            if (isCredit && !isDebit) tx.amount >= 0 else if (isDebit && !isCredit) tx.amount < 0 else false
        }

        val sign = if (isIncrease) "+" else "−"
        val amtColor = if (isIncrease) "#059669" else "#DC2626"
        val runningBal = runningBalanceMap[tx.id]?.let { LanguageHelper.formatCurrency(it, languageMode) } ?: "-"

        val txDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(tx.dateEpochMs))
        val payee = tx.payeeOrPayer.ifBlank { item.category?.nameEn ?: "Transaction" }
        val cat = item.category?.nameEn ?: "-"
        val note = tx.note.ifBlank { "" }
        val status = tx.status.name

        rowsHtml.append(
            """
            <tr>
                <td>${idx + 1}</td>
                <td>$txDate</td>
                <td><strong>$payee</strong>${if (note.isNotBlank()) "<br/><small style='color:#666;'>$note</small>" else ""}</td>
                <td>$cat</td>
                <td><span class="badge ${if (tx.status == TransactionStatus.RECONCILED) "badge-reconciled" else "badge-normal"}">$status</span></td>
                <td style="text-align:right; color:$amtColor; font-weight:bold;">$sign${LanguageHelper.formatCurrency(abs(tx.amount), languageMode)}</td>
                <td style="text-align:right; font-weight:bold;">$runningBal</td>
            </tr>
            """.trimIndent()
        )
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8"/>
            <title>Statement - $accountName</title>
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 24px; color: #1E293B; background: #FFF; }
                .header { border-bottom: 2px solid #059669; padding-bottom: 12px; margin-bottom: 18px; display: flex; justify-content: space-between; align-items: flex-end; }
                .title { font-size: 24px; font-weight: bold; color: #059669; margin: 0; }
                .subtitle { font-size: 13px; color: #64748B; margin-top: 4px; }
                .meta { text-align: right; font-size: 12px; color: #64748B; }
                .kpi-container { display: flex; gap: 12px; margin-bottom: 20px; }
                .kpi-box { flex: 1; padding: 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; text-align: center; }
                .kpi-label { font-size: 11px; text-transform: uppercase; color: #64748B; font-weight: 600; }
                .kpi-value { font-size: 18px; font-weight: bold; margin-top: 4px; }
                .filter-note { font-size: 12px; background: #F1F5F9; padding: 8px 12px; border-radius: 6px; margin-bottom: 16px; color: #475569; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; }
                th { background: #059669; color: #FFF; text-align: left; padding: 8px 10px; font-weight: 600; }
                td { padding: 8px 10px; border-bottom: 1px solid #E2E8F0; }
                tr:nth-child(even) { background-color: #F8FAFC; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 10px; border-radius: 4px; font-weight: 600; }
                .badge-reconciled { background: #DCFCE7; color: #059669; }
                .badge-normal { background: #F1F5F9; color: #64748B; }
                .footer { margin-top: 24px; text-align: center; font-size: 11px; color: #94A3B8; border-top: 1px solid #E2E8F0; padding-top: 12px; }
                @media print {
                    body { margin: 0; }
                    tr { page-break-inside: avoid; }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div>
                    <h1 class="title">$accountName Statement</h1>
                    <div class="subtitle">${account.type.name} Account • Budgeter Ledger</div>
                </div>
                <div class="meta">
                    <div>Generated: $dateStr</div>
                    <div>Transactions: ${transactions.size}</div>
                </div>
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-label">Current Ending Balance</div>
                    <div class="kpi-value" style="color: #059669;">${LanguageHelper.formatCurrency(currentBalance, languageMode)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-label">Total Inflow / Received</div>
                    <div class="kpi-value" style="color: #059669;">+${LanguageHelper.formatCurrency(totalInflow, languageMode)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-label">Total Outflow / Spent</div>
                    <div class="kpi-value" style="color: #DC2626;">-${LanguageHelper.formatCurrency(totalOutflow, languageMode)}</div>
                </div>
            </div>

            <div class="filter-note">
                <strong>Filter:</strong> $filterSummary
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 30px;">#</th>
                        <th style="width: 85px;">Date</th>
                        <th>Payee / Description</th>
                        <th>Category</th>
                        <th style="width: 80px;">Status</th>
                        <th style="text-align: right; width: 110px;">Amount</th>
                        <th style="text-align: right; width: 110px;">Balance</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            <div class="footer">
                Printed via Budgeter • Comprehensive Financial Records
            </div>
        </body>
        </html>
    """.trimIndent()
}

// -------------------------------------------------------------------------------------------------
// SELECTION TOP APP BAR (Faithful to Image 1)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccountDetailSelectionTopAppBar(
    selectedAmountText: String,
    languageMode: LanguageMode,
    onBack: () -> Unit,
    onSelectAll: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: (ExportFormat) -> Unit,
    onChangeName: () -> Unit,
    onChangeDate: () -> Unit,
    onChangeCategory: () -> Unit,
    onChangeAccount: () -> Unit,
    onChangeAmount: () -> Unit,
    onAddLabel: () -> Unit,
    onReconcile: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("account_selection_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Clear selection",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = selectedAmountText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onSelectAll,
                modifier = Modifier.testTag("account_selection_select_all_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Select All",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDuplicate,
                modifier = Modifier.testTag("account_selection_duplicate_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ExportMenuButton(
                onExport = onExport,
                languageMode = languageMode,
                testTag = "account_selection_export_btn"
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("account_selection_more_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Change name") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onChangeName()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onChangeDate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onChangeCategory()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change account") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onChangeAccount()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change amount") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onChangeAmount()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add label") },
                        leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onAddLabel()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reconcile") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onReconcile()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// BATCH ACTION DIALOGS (Change Name, Category, Account, Amount, Label, Delete)
// -------------------------------------------------------------------------------------------------
@Composable
private fun BatchChangeNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Name", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Payee / Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = SolidIncome)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BatchChangeCategoryDialog(
    allCategories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectCategory: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Category", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(allCategories, key = { it.id }) { cat ->
                    val catName = if (languageMode == LanguageMode.BANGLA && cat.nameBn.isNotBlank()) cat.nameBn else cat.nameEn
                    val catIcon = IconHelper.getIconByName(cat.iconName)
                    val catColor = try {
                        if (!cat.colorHex.isNullOrBlank()) Color(android.graphics.Color.parseColor(cat.colorHex)) else SolidIncome
                    } catch (e: Exception) { SolidIncome }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCategory(cat) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(catIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(catName, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BatchChangeAccountDialog(
    allAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectAccount: (Account) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Account", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(allAccounts, key = { it.id }) { acc ->
                    val accName = if (languageMode == LanguageMode.BANGLA && acc.nameBn.isNotBlank()) acc.nameBn else acc.nameEn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccount(acc) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SolidIncome,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(accName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(acc.type.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BatchChangeAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Amount", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("New Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("BDT ") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onConfirm(amt)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = SolidIncome)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BatchAddLabelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var labelText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Label", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                label = { Text("Label (e.g. grocery, vacation)") },
                singleLine = true,
                prefix = { Text("#") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (labelText.isNotBlank()) {
                        onConfirm(labelText.trim())
                    }
                },
                enabled = labelText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SolidIncome)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BatchDeleteConfirmDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transactions", fontWeight = FontWeight.Bold) },
        text = { Text("Are you sure you want to delete $count selected transactions? This cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
