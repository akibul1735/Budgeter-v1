package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import com.example.util.LanguageHelper
import com.example.util.RmManagerHelper
import com.example.util.RmManagerHelper.RmEntityBreakdown
import com.example.util.RmManagerHelper.RmFilterCategory
import com.example.util.RmManagerHelper.RmKhatianRow
import com.example.util.RmManagerHelper.RmRepaymentStatus
import com.example.util.RmManagerHelper.RmSortOption
import com.example.util.RmManagerHelper.RmTransactionItem
import com.example.util.RmManagerPreferences
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

enum class RmDetailViewMode {
    KHATIAN_LEDGER, // Side-by-side Khatian table with Date, Particulars/Type, Debit (Dr), Credit (Cr), Balance (Jer)
    TIMELINE        // Visual modern cards timeline
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RmManagerScreen(
    allAccounts: List<Account>,
    allTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onAddTransactionClick: (TransactionType) -> Unit = {},
    onExecuteRepayTransfer: (account: Account?, label: String?, defaultAmount: Double) -> Unit = { _, _, _ -> },
    onSaveTransaction: (Transaction) -> Unit = {},
    onUpdateTransactions: (List<Transaction>) -> Unit = {},
    onAddAccountClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val filterConfig by RmManagerPreferences.getInstance(context).config.collectAsState()
    val tabFilterPrefs = remember { com.example.util.TabFilterPreferences.getInstance(context) }

    var searchQuery by remember { mutableStateOf(tabFilterPrefs.rmSearchQuery) }
    var isSearchExpanded by remember { mutableStateOf(tabFilterPrefs.rmSearchQuery.isNotBlank()) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            delay(100)
            try {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }
    }
    var isUnifiedFilterOpen by remember { mutableStateOf(false) }
    var selectedFilterCategory by remember { mutableStateOf(tabFilterPrefs.rmFilterCategory) }
    var selectedSortOption by remember { mutableStateOf(tabFilterPrefs.rmSortOption) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var selectedEntityId by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery, selectedFilterCategory, selectedSortOption) {
        tabFilterPrefs.rmSearchQuery = searchQuery
        tabFilterPrefs.rmFilterCategory = selectedFilterCategory
        tabFilterPrefs.rmSortOption = selectedSortOption
    }

    // Reconcile Sheet State
    var entityToReconcile by remember { mutableStateOf<RmManagerHelper.RmEntityBreakdown?>(null) }
    var transactionForQuickReconcile by remember { mutableStateOf<Transaction?>(null) }

    // Recompute RM Data
    val rmData = remember(
        allAccounts,
        allTransactions,
        searchQuery,
        selectedFilterCategory,
        selectedSortOption,
        languageMode,
        filterConfig,
        refreshTrigger
    ) {
        RmManagerHelper.computeRmManagerData(
            allAccounts = allAccounts,
            allTransactions = allTransactions,
            searchQuery = searchQuery,
            filterCategory = selectedFilterCategory,
            sortOption = selectedSortOption,
            languageMode = languageMode,
            includeKeyword = filterConfig.includeKeyword,
            excludeKeyword = filterConfig.excludeKeyword
        )
    }

    val selectedEntity = remember(selectedEntityId, rmData) {
        if (selectedEntityId == null) null
        else rmData.allEntities.firstOrNull { it.id == selectedEntityId }
    }

    // Handle back button when viewing detail
    BackHandler(enabled = selectedEntity != null) {
        selectedEntityId = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Column {
                        Text(
                            text = if (selectedEntity != null) {
                                selectedEntity.name
                            } else {
                                LanguageHelper.getString("rm_manager", languageMode).ifEmpty { "RM Manager" }
                            },
                            fontSize = 18.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (selectedEntity != null) {
                            Text(
                                text = "${if (selectedEntity.isAccount) "RM Account" else "RM Label"} • ${LanguageHelper.getString("total_jer", languageMode)}: ${RmManagerHelper.formatSignedLiabilityAmount(selectedEntity.netBalance)}",
                                fontSize = 12.sp,
                                color = if (selectedEntity.netBalance < 0) MaterialTheme.colorScheme.error else SolidIncome,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "${LanguageHelper.getString("total_resting_liability", languageMode)}: -৳ ${RmManagerHelper.formatAmount(rmData.totalOutstandingLiability)}",
                                fontSize = 12.sp,
                                color = if (rmData.totalOutstandingLiability > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedEntity != null) {
                        IconButton(onClick = { selectedEntityId = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Drawer",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (selectedEntity != null) {
                        // Reconcile Account Button (if it's an account)
                        if (selectedEntity.isAccount && selectedEntity.account != null) {
                            IconButton(onClick = { entityToReconcile = selectedEntity }) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = LanguageHelper.getString("reconcile_account", languageMode),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Share statement button
                        IconButton(onClick = {
                            val text = RmManagerHelper.generateStatementText(selectedEntity, languageMode)
                            copyToClipboard(context, text)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    LanguageHelper.getString("statement_copied", languageMode)
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = LanguageHelper.getString("share_statement", languageMode),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Search Button
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) {
                                    searchQuery = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchExpanded || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Sort Menu Button
                        Box {
                            IconButton(onClick = { isSortMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(LanguageHelper.getString("sort_highest_due", languageMode)) },
                                    onClick = {
                                        selectedSortOption = RmSortOption.HIGHEST_DUE
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSortOption == RmSortOption.HIGHEST_DUE) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageHelper.getString("sort_lowest_due", languageMode)) },
                                    onClick = {
                                        selectedSortOption = RmSortOption.LOWEST_DUE
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSortOption == RmSortOption.LOWEST_DUE) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageHelper.getString("sort_most_repaid", languageMode)) },
                                    onClick = {
                                        selectedSortOption = RmSortOption.MOST_REPAID
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSortOption == RmSortOption.MOST_REPAID) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageHelper.getString("sort_name_az", languageMode)) },
                                    onClick = {
                                        selectedSortOption = RmSortOption.NAME_AZ
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSortOption == RmSortOption.NAME_AZ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(LanguageHelper.getString("sort_recent_activity", languageMode)) },
                                    onClick = {
                                        selectedSortOption = RmSortOption.RECENT_ACTIVITY
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSortOption == RmSortOption.RECENT_ACTIVITY) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }

                        // United Single Filter & Settings Button
                        IconButton(onClick = { isUnifiedFilterOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = if (languageMode == LanguageMode.BANGLA) "ফিল্টার ও সেটিংস" else "Filter & Settings",
                                tint = if (selectedFilterCategory != RmFilterCategory.ALL || filterConfig.includeKeyword != "RM" || filterConfig.excludeKeyword != "RM Others")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedEntity != null) {
                        onExecuteRepayTransfer(selectedEntity.account, if (!selectedEntity.isAccount) selectedEntity.name else null, selectedEntity.remainingLiability)
                    } else {
                        onAddTransactionClick(TransactionType.TRANSFER)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("rm_manager_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Transfer Repay")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LanguageHelper.getString("repay_liability", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Expandable Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "আরএম অ্যাকাউন্ট বা লেবেল খুঁজুন..." else "Search RM accounts, labels, notes...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(searchFocusRequester)
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedEntity != null) {
                // Drill-down Detail: Khatian Ledger (Side-by-Side Dr/Cr/Jer) & Timeline
                RmModernDetailView(
                    entity = selectedEntity,
                    languageMode = languageMode,
                    onTransactionClick = onTransactionClick,
                    onRepayClick = {
                        onExecuteRepayTransfer(selectedEntity.account, if (!selectedEntity.isAccount) selectedEntity.name else null, selectedEntity.remainingLiability)
                    },
                    onRecordBorrowClick = {
                        onAddTransactionClick(TransactionType.INCOME)
                    },
                    onReconcileEntityClick = {
                        entityToReconcile = selectedEntity
                    },
                    onQuickReconcileTransaction = { tx ->
                        transactionForQuickReconcile = tx
                    },
                    onShareStatement = {
                        val text = RmManagerHelper.generateStatementText(selectedEntity, languageMode)
                        copyToClipboard(context, text)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                LanguageHelper.getString("statement_copied", languageMode)
                            )
                        }
                    }
                )
            } else {
                // Main Executive Dashboard & Cards
                RmModernMainView(
                    rmData = rmData,
                    languageMode = languageMode,
                    selectedFilterCategory = selectedFilterCategory,
                    onSelectFilterCategory = { selectedFilterCategory = it },
                    onSelectEntity = { selectedEntityId = it.id },
                    onRepayEntity = { entity ->
                        onExecuteRepayTransfer(entity.account, if (!entity.isAccount) entity.name else null, entity.remainingLiability)
                    },
                    onReconcileEntity = { entity ->
                        entityToReconcile = entity
                    }
                )
            }
        }
    }

    // Quick Reconcile Confirmation Dialog for single transaction
    if (transactionForQuickReconcile != null) {
        val tx = transactionForQuickReconcile!!
        val isCurrentlyReconciled = tx.status == TransactionStatus.RECONCILED
        AlertDialog(
            onDismissRequest = { transactionForQuickReconcile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = if (isCurrentlyReconciled) MaterialTheme.colorScheme.outline else SolidIncome,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isCurrentlyReconciled)
                        LanguageHelper.getString("mark_as_unreconciled", languageMode)
                    else
                        LanguageHelper.getString("mark_as_reconciled", languageMode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "এই লেনদেনটি (৳ ${RmManagerHelper.formatAmount(tx.amount)}) মিলকরণ অবস্থা পরিবর্তন করবেন?"
                    } else {
                        "Toggle reconciled status for transaction (৳ ${RmManagerHelper.formatAmount(tx.amount)})?"
                    },
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStatus = if (isCurrentlyReconciled) TransactionStatus.NONE else TransactionStatus.RECONCILED
                        onUpdateTransactions(listOf(tx.copy(status = newStatus)))
                        transactionForQuickReconcile = null
                        refreshTrigger++
                        Toast.makeText(
                            context,
                            LanguageHelper.getString("reconcile_success", languageMode),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCurrentlyReconciled) MaterialTheme.colorScheme.error else SolidIncome)
                ) {
                    Text(
                        text = if (isCurrentlyReconciled)
                            LanguageHelper.getString("mark_as_unreconciled", languageMode)
                        else
                            LanguageHelper.getString("mark_as_reconciled", languageMode),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionForQuickReconcile = null }) {
                    Text(LanguageHelper.getString("cancel", languageMode).ifEmpty { "Cancel" })
                }
            }
        )
    }

    // Entity Balance Reconciliation BottomSheet (Accounts & RM Others Labels)
    if (entityToReconcile != null) {
        val targetEntity = entityToReconcile!!
        val currentLiability = targetEntity.remainingLiability

        RmEntityReconcileBottomSheet(
            entity = targetEntity,
            currentOutstandingLiability = currentLiability,
            languageMode = languageMode,
            onDismiss = { entityToReconcile = null },
            onConfirm = { targetRemainingDue ->
                val diff = targetRemainingDue - currentLiability
                if (abs(diff) >= 0.01) {
                    if (targetEntity.isAccount && targetEntity.account != null) {
                        val targetAcc = targetEntity.account
                        val adjustmentTx = if (diff > 0) {
                            // diff > 0: Liability increases -> Expense credited out of RM account (adds to Dr / borrowed in RM manager)
                            Transaction(
                                type = TransactionType.EXPENSE,
                                amount = abs(diff),
                                debitAccountId = null,
                                creditAccountId = targetAcc.id,
                                note = "RM Liability Adjustment (Reconciliation)",
                                payeeOrPayer = "${targetAcc.localizedName(languageMode)} Debit",
                                status = TransactionStatus.RECONCILED,
                                dateEpochMs = System.currentTimeMillis()
                            )
                        } else {
                            // diff < 0: Liability decreases (e.g. from 550 to 0) -> Income debited into RM account (adds to Cr / repaid in RM manager)
                            Transaction(
                                type = TransactionType.INCOME,
                                amount = abs(diff),
                                debitAccountId = targetAcc.id,
                                creditAccountId = null,
                                note = "RM Repayment Adjustment (Reconciliation)",
                                payeeOrPayer = "${targetAcc.localizedName(languageMode)} Credit",
                                status = TransactionStatus.RECONCILED,
                                dateEpochMs = System.currentTimeMillis()
                            )
                        }
                        onSaveTransaction(adjustmentTx)
                    } else {
                        // Label Reconciliation in RM Others:
                        // Only adjusts and reconciles this specific label (#targetEntity.name),
                        // keeping other RM Others balances intact!
                        val rmOthersAcc = allAccounts.firstOrNull { RmManagerHelper.isExcludedAccount(it) }
                            ?: allAccounts.firstOrNull { it.nameEn.contains("RM Others", ignoreCase = true) || it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) }
                            ?: allAccounts.firstOrNull()

                        val rmAccId = rmOthersAcc?.id
                        val adjustmentTx = if (diff > 0) {
                            // diff > 0: Liability increases -> Expense credited out of RM account
                            Transaction(
                                type = TransactionType.EXPENSE,
                                amount = abs(diff),
                                debitAccountId = null,
                                creditAccountId = rmAccId,
                                referenceNo = targetEntity.name,
                                note = "#${targetEntity.name} Reconcile Adjustment",
                                payeeOrPayer = "${targetEntity.name} Debit",
                                status = TransactionStatus.RECONCILED,
                                dateEpochMs = System.currentTimeMillis()
                            )
                        } else {
                            // diff < 0: Liability decreases (repaid) -> Income debited into RM account
                            Transaction(
                                type = TransactionType.INCOME,
                                amount = abs(diff),
                                debitAccountId = rmAccId,
                                creditAccountId = null,
                                referenceNo = targetEntity.name,
                                note = "#${targetEntity.name} Repay Adjustment",
                                payeeOrPayer = "${targetEntity.name} Credit",
                                status = TransactionStatus.RECONCILED,
                                dateEpochMs = System.currentTimeMillis()
                            )
                        }
                        onSaveTransaction(adjustmentTx)
                    }
                    refreshTrigger++
                }

                // Also mark all existing transactions of this specific entity (account or label) as RECONCILED
                val unreconciledTxs = targetEntity.allTransactions
                    .map { it.transactionWithDetails.transaction }
                    .filter { it.status != TransactionStatus.RECONCILED }

                if (unreconciledTxs.isNotEmpty()) {
                    val updated = unreconciledTxs.map { it.copy(status = TransactionStatus.RECONCILED) }
                    onUpdateTransactions(updated)
                    refreshTrigger++
                }

                Toast.makeText(
                    context,
                    LanguageHelper.getString("reconcile_success", languageMode),
                    Toast.LENGTH_SHORT
                ).show()
                entityToReconcile = null
            }
        )
    }

    // Unified RM Filter & Settings Dialog
    if (isUnifiedFilterOpen) {
        UnifiedRmFilterDialog(
            selectedCategory = selectedFilterCategory,
            currentIncludeKeyword = filterConfig.includeKeyword,
            currentExcludeKeyword = filterConfig.excludeKeyword,
            languageMode = languageMode,
            onDismiss = { isUnifiedFilterOpen = false },
            onApply = { newCategory, newInclude, newExclude ->
                selectedFilterCategory = newCategory
                RmManagerPreferences.getInstance(context).saveConfig(newInclude, newExclude)
                isUnifiedFilterOpen = false
                refreshTrigger++
            },
            onReset = {
                RmManagerPreferences.getInstance(context).resetToDefaults()
                selectedFilterCategory = RmFilterCategory.ALL
                isUnifiedFilterOpen = false
                refreshTrigger++
            }
        )
    }
}

/**
 * Main RM Dashboard with Negative Liability Summary and Quick Action Entity Cards.
 */
@Composable
private fun RmModernMainView(
    rmData: RmManagerHelper.RmManagerScreenData,
    languageMode: LanguageMode,
    selectedFilterCategory: RmFilterCategory,
    onSelectFilterCategory: (RmFilterCategory) -> Unit,
    onSelectEntity: (RmEntityBreakdown) -> Unit,
    onRepayEntity: (RmEntityBreakdown) -> Unit,
    onReconcileEntity: (RmEntityBreakdown) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // --- 1. Executive Hero Dashboard Card (Negative Liabilities) ---
        item {
            RmExecutiveHeroCard(
                rmData = rmData,
                languageMode = languageMode
            )
        }

        // --- 1.5. Analytics Graph Card (Liability & Repayment Trend) ---
        item {
            RmAnalyticsGraphCard(
                rmData = rmData,
                languageMode = languageMode
            )
        }

        // --- 2. Filter Category Chips ---
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterCategory == RmFilterCategory.ALL,
                        onClick = { onSelectFilterCategory(RmFilterCategory.ALL) },
                        label = { Text("${LanguageHelper.getString("all_entities", languageMode)} (${rmData.totalAllCount})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterCategory == RmFilterCategory.PENDING_ONLY,
                        onClick = { onSelectFilterCategory(RmFilterCategory.PENDING_ONLY) },
                        label = { Text("${LanguageHelper.getString("pending_repayment", languageMode)} (${rmData.totalPendingCount})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterCategory == RmFilterCategory.RM_ACCOUNTS,
                        onClick = { onSelectFilterCategory(RmFilterCategory.RM_ACCOUNTS) },
                        label = { Text("${LanguageHelper.getString("rm_accounts_tab", languageMode)} (${rmData.totalRmAccountsCount})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterCategory == RmFilterCategory.RM_OTHERS,
                        onClick = { onSelectFilterCategory(RmFilterCategory.RM_OTHERS) },
                        label = { Text("${LanguageHelper.getString("rm_others_tab", languageMode)} (${rmData.totalRmOthersCount})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilterCategory == RmFilterCategory.SETTLED_ONLY,
                        onClick = { onSelectFilterCategory(RmFilterCategory.SETTLED_ONLY) },
                        label = { Text("${LanguageHelper.getString("fully_settled", languageMode)} (${rmData.totalSettledCount})") }
                    )
                }
            }
        }

        // --- 3. Entities List Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "খতিয়ান" else "Ledger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${rmData.allEntities.size} ${if (languageMode == LanguageMode.BANGLA) "টি" else "entities"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // --- 4. Entity Cards ---
        if (rmData.allEntities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = LanguageHelper.getString("no_rm_found", languageMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "একাউন্টের নামের সাথে আলাদা 'RM' রাখুন অথবা লেনদেনে হ্যাশট্যাগ (#ট্যাগ) ব্যবহার করুন"
                            else
                                "Add accounts with 'RM' in name or add transactions with labels/payees",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(rmData.allEntities, key = { it.id }) { entity ->
                RmModernEntityCard(
                    entity = entity,
                    languageMode = languageMode,
                    onClick = { onSelectEntity(entity) },
                    onRepayClick = { onRepayEntity(entity) },
                    onReconcileClick = { onReconcileEntity(entity) }
                )
            }
        }
    }
}

/**
 * Executive Hero Dashboard Card with negative liability representation.
 */
@Composable
private fun RmExecutiveHeroCard(
    rmData: RmManagerHelper.RmManagerScreenData,
    languageMode: LanguageMode
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (rmData.totalOutstandingLiability > 0)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(12.dp)
        ) {
            // Header row with Icon & Pending Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (rmData.totalOutstandingLiability > 0)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else
                            SolidIncome.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (rmData.totalOutstandingLiability > 0) MaterialTheme.colorScheme.error else SolidIncome,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("total_resting_liability", languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (rmData.totalOutstandingLiability > 0)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    else
                        SolidIncome.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (rmData.totalOutstandingLiability > 0)
                            "${rmData.pendingCount + rmData.partiallyRepaidCount} ${if (languageMode == LanguageMode.BANGLA) "টি বকেয়া" else "Pending"}"
                        else
                            if (languageMode == LanguageMode.BANGLA) "সব পরিশোধিত" else "All Settled",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rmData.totalOutstandingLiability > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            SolidIncome,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Negative Outstanding Liability (-৳ X,XXX)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (rmData.totalOutstandingLiability > 0) "-৳" else "৳",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rmData.totalOutstandingLiability > 0) MaterialTheme.colorScheme.error else SolidIncome,
                    modifier = Modifier.padding(bottom = 2.dp, end = 3.dp)
                )
                Text(
                    text = RmManagerHelper.formatAmount(rmData.totalOutstandingLiability),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (rmData.totalOutstandingLiability > 0) MaterialTheme.colorScheme.error else SolidIncome
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পরিশোধ বাকি (জের)" else "due liability",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dual Inflow (Dr) vs Repaid (Cr)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Borrowed / Inflow (Dr)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "গৃহীত দেনা (Dr)" else "Total Liability (Dr)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "৳ ${RmManagerHelper.formatAmount(rmData.totalGrossBorrowed)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Total Repaid / Settled (Cr)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SolidIncome.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পরিশোধিত (Cr)" else "Total Repaid (Cr)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "৳ ${RmManagerHelper.formatAmount(rmData.totalGrossRepaid)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overall Repayment Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("repayment_progress", languageMode),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(rmData.overallRepaymentProgress * 100).toInt()}% ${if (languageMode == LanguageMode.BANGLA) "পরিশোধিত" else "Repaid"}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rmData.overallRepaymentProgress >= 1f) SolidIncome else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { rmData.overallRepaymentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (rmData.overallRepaymentProgress >= 1f) SolidIncome else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

/**
 * Modern Entity Card with negative liability balances, side-by-side metric pills, and Quick Actions.
 */
@Composable
private fun RmModernEntityCard(
    entity: RmEntityBreakdown,
    languageMode: LanguageMode,
    onClick: () -> Unit,
    onRepayClick: () -> Unit,
    onReconcileClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Avatar, Name, Type Chip, Reconcile/Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar Circle with Initials
                    Surface(
                        shape = CircleShape,
                        color = if (entity.isAccount)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = entity.name.take(2).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (entity.isAccount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = entity.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (entity.isAccount)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = if (entity.isAccount) {
                                        if (languageMode == LanguageMode.BANGLA) "আরএম একাউন্ট" else "RM Account"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "আরএম অন্যান্য" else "RM Others"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (entity.isAccount) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${entity.khatianLedgerRows.size} ${if (languageMode == LanguageMode.BANGLA) "টি এন্ট্রি" else "entries"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Status Badge
                when (entity.status) {
                    RmRepaymentStatus.SETTLED -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SolidIncome.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageHelper.getString("fully_settled", languageMode),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome
                                )
                            }
                        }
                    }
                    RmRepaymentStatus.PARTIALLY_REPAID -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = "${(entity.repaymentProgressPercent * 100).toInt()}% ${if (languageMode == LanguageMode.BANGLA) "পরিশোধ" else "Repaid"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    RmRepaymentStatus.UNPAID -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = LanguageHelper.getString("pending_repayment", languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Metrics Row (Side by side): Borrowed vs Repaid vs Running Jer Balance (Negative)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Borrowed (Dr)
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "গৃহীত দায় (Dr)" else "Borrowed (Dr)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "৳ ${RmManagerHelper.formatAmount(entity.totalBorrowed)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Total Repaid (Cr)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পরিশোধ (Cr)" else "Repaid (Cr)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "৳ ${RmManagerHelper.formatAmount(entity.totalRepaid)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SolidIncome
                        )
                    }

                    // Net Remaining Jer Balance (Negative if due)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LanguageHelper.getString("total_jer", languageMode),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = RmManagerHelper.formatSignedLiabilityAmount(entity.netBalance),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entity.netBalance < 0) MaterialTheme.colorScheme.error else SolidIncome
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { entity.repaymentProgressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (entity.status == RmRepaymentStatus.SETTLED) SolidIncome else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: From left: Ledger, Reconciled, Transfer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Ledger Button (Left)
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "খতিয়ান" else "Ledger",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 2. Reconciled Button (Middle)
                OutlinedButton(
                    onClick = onReconcileClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SolidIncome,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "মিলকরণ" else "Reconciled",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidIncome,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3. Transfer Button (Right)
                Button(
                    onClick = onRepayClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entity.remainingLiability > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (entity.remainingLiability > 0)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার" else "Transfer",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Drill-down Detail View featuring side-by-side Khatian (খতিয়ান - জের) Ledger and Modern Timeline.
 */
@Composable
private fun RmModernDetailView(
    entity: RmEntityBreakdown,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit,
    onRepayClick: () -> Unit,
    onRecordBorrowClick: () -> Unit,
    onReconcileEntityClick: () -> Unit,
    onQuickReconcileTransaction: (Transaction) -> Unit,
    onShareStatement: () -> Unit
) {
    val context = LocalContext.current
    val tabFilterPrefs = remember { com.example.util.TabFilterPreferences.getInstance(context) }
    var detailViewMode by remember { mutableStateOf(tabFilterPrefs.rmDetailViewMode) }
    var timelineFilter by remember { mutableStateOf(tabFilterPrefs.rmTimelineFilter) } // 0: All, 1: Repayments (Cr), 2: Liabilities (Dr)

    LaunchedEffect(detailViewMode, timelineFilter) {
        tabFilterPrefs.rmDetailViewMode = detailViewMode
        tabFilterPrefs.rmTimelineFilter = timelineFilter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // --- 1. Entity Hero Summary Card (Exact compact size & padding matching front card) ---
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    if (entity.netBalance < 0)
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                    else
                                        SolidIncome.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(12.dp)
                ) {
                    // Header: Name & Type + Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (entity.isAccount)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = entity.name.take(2).uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (entity.isAccount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = entity.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (entity.isAccount) {
                                        if (languageMode == LanguageMode.BANGLA) "আরএম একাউন্ট" else "RM Account"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "আরএম অন্যান্য" else "RM Others"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (entity.status) {
                                RmRepaymentStatus.SETTLED -> SolidIncome.copy(alpha = 0.15f)
                                RmRepaymentStatus.PARTIALLY_REPAID -> Color(0xFFFFF3E0)
                                RmRepaymentStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                            }
                        ) {
                            Text(
                                text = when (entity.status) {
                                    RmRepaymentStatus.SETTLED -> LanguageHelper.getString("fully_settled", languageMode)
                                    RmRepaymentStatus.PARTIALLY_REPAID -> "${(entity.repaymentProgressPercent * 100).toInt()}% ${if (languageMode == LanguageMode.BANGLA) "পরিশোধ" else "Repaid"}"
                                    RmRepaymentStatus.UNPAID -> if (languageMode == LanguageMode.BANGLA) "বকেয়া" else "Pending"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (entity.status) {
                                    RmRepaymentStatus.SETTLED -> SolidIncome
                                    RmRepaymentStatus.PARTIALLY_REPAID -> Color(0xFFE65100)
                                    RmRepaymentStatus.UNPAID -> MaterialTheme.colorScheme.onErrorContainer
                                },
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Negative Net Balance (জের)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (entity.netBalance < 0) "-৳" else "৳",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entity.netBalance < 0) MaterialTheme.colorScheme.error else SolidIncome,
                            modifier = Modifier.padding(bottom = 2.dp, end = 3.dp)
                        )
                        Text(
                            text = RmManagerHelper.formatAmount(entity.netBalance),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entity.netBalance < 0) MaterialTheme.colorScheme.error else SolidIncome
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বর্তমান জের (বকেয়া)" else "Current Balance (Jer)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dr vs Cr Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Dr
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = LanguageHelper.getString("debit_col", languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "৳ ${RmManagerHelper.formatAmount(entity.totalBorrowed)}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Total Cr
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SolidIncome.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SolidIncome,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = LanguageHelper.getString("credit_col", languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "৳ ${RmManagerHelper.formatAmount(entity.totalRepaid)}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidIncome
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageHelper.getString("repayment_progress", languageMode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(entity.repaymentProgressPercent * 100).toInt()}% ${if (languageMode == LanguageMode.BANGLA) "পরিশোধিত" else "Repaid"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (entity.status == RmRepaymentStatus.SETTLED) SolidIncome else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { entity.repaymentProgressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (entity.status == RmRepaymentStatus.SETTLED) SolidIncome else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Action Buttons: from left Ledger, Reconciled, Transfer, Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Ledger Button (Left)
                        val isLedgerSelected = detailViewMode == RmDetailViewMode.KHATIAN_LEDGER
                        OutlinedButton(
                            onClick = { detailViewMode = RmDetailViewMode.KHATIAN_LEDGER },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isLedgerSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isLedgerSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isLedgerSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "খতিয়ান" else "Ledger",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // 2. Reconciled Button (Middle)
                        OutlinedButton(
                            onClick = onReconcileEntityClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SolidIncome.copy(alpha = 0.12f),
                                contentColor = SolidIncome
                            ),
                            border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1.05f)
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SolidIncome,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মিলকরণ" else "Reconciled",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = SolidIncome
                            )
                        }

                        // 3. Transfer Button (Right)
                        Button(
                            onClick = onRepayClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1.05f)
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার" else "Transfer",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Share / Statement
                        IconButton(
                            onClick = onShareStatement,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Tab Switcher: Khatian (Side-by-Side Dr/Cr/Jer) vs Timeline ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Khatian Ledger Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER)
                            MaterialTheme.colorScheme.surface
                        else
                            Color.Transparent,
                        shadowElevation = if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { detailViewMode = RmDetailViewMode.KHATIAN_LEDGER }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageHelper.getString("khatian_view", languageMode),
                                fontSize = 13.5.sp,
                                fontWeight = if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER) FontWeight.Bold else FontWeight.Medium,
                                color = if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Timeline Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (detailViewMode == RmDetailViewMode.TIMELINE)
                            MaterialTheme.colorScheme.surface
                        else
                            Color.Transparent,
                        shadowElevation = if (detailViewMode == RmDetailViewMode.TIMELINE) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { detailViewMode = RmDetailViewMode.TIMELINE }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = if (detailViewMode == RmDetailViewMode.TIMELINE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageHelper.getString("timeline_view", languageMode),
                                fontSize = 13.5.sp,
                                fontWeight = if (detailViewMode == RmDetailViewMode.TIMELINE) FontWeight.Bold else FontWeight.Medium,
                                color = if (detailViewMode == RmDetailViewMode.TIMELINE) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Content: Khatian Ledger (Side-by-side Table) or Timeline ---
        if (detailViewMode == RmDetailViewMode.KHATIAN_LEDGER) {
            item {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "💡 দীর্ঘক্ষণ চেপে ধরে (Long Press) মিলকরণ (Reconcile) টগল করুন"
                    else
                        "💡 Long press any transaction row to toggle Reconciled status",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }

            item {
                RmKhatianLedgerTable(
                    entity = entity,
                    languageMode = languageMode,
                    onTransactionClick = onTransactionClick,
                    onQuickReconcile = onQuickReconcileTransaction
                )
            }
        } else {
            // Timeline view with filter pills (All / Cr / Dr)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = timelineFilter == 0,
                        onClick = { timelineFilter = 0 },
                        label = { Text("${LanguageHelper.getString("all", languageMode).ifEmpty { "All" }} (${entity.allTransactions.size})") }
                    )
                    FilterChip(
                        selected = timelineFilter == 1,
                        onClick = { timelineFilter = 1 },
                        label = { Text("${LanguageHelper.getString("credit_col", languageMode)} (${entity.creditTransactions.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidIncome.copy(alpha = 0.15f),
                            selectedLabelColor = SolidIncome
                        )
                    )
                    FilterChip(
                        selected = timelineFilter == 2,
                        onClick = { timelineFilter = 2 },
                        label = { Text("${LanguageHelper.getString("debit_col", languageMode)} (${entity.debitTransactions.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }

            val displayedTransactions = when (timelineFilter) {
                1 -> entity.creditTransactions
                2 -> entity.debitTransactions
                else -> entity.allTransactions
            }

            if (displayedTransactions.isEmpty()) {
                item {
                    Text(
                        text = LanguageHelper.getString("no_transactions_found", languageMode).ifEmpty { "No transactions found" },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(displayedTransactions, key = { "tx_${it.transactionWithDetails.transaction.id}_${it.isDebit}" }) { item ->
                    RmTimelineCard(
                        item = item,
                        languageMode = languageMode,
                        onClick = { onTransactionClick(item.transactionWithDetails.transaction) },
                        onLongClick = { onQuickReconcileTransaction(item.transactionWithDetails.transaction) }
                    )
                }
            }
        }
    }
}

/**
 * Side-by-Side Khatian Ledger Table (খতিয়ানের জের পদ্ধতি).
 * Compact columns: Date (mini date, oldest first) | Dr/Cr | Debit Amount | Credit Amount | Balance
 */
@Composable
private fun RmKhatianLedgerTable(
    entity: RmEntityBreakdown,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit,
    onQuickReconcile: (Transaction) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(vertical = 6.dp)
        ) {
            // Table Header Row: Date | Dr/Cr | Debit | Credit | Balance
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .padding(vertical = 7.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Date Column (mini date, oldest first)
                Text(
                    text = LanguageHelper.getString("date_col", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(66.dp)
                )
                // 2. Dr / Cr
                Text(
                    text = "Dr/Cr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(42.dp)
                )
                // 3. Debit (Dr) Amount
                Text(
                    text = LanguageHelper.getString("debit_col", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(74.dp)
                )
                // 4. Credit (Cr) Amount
                Text(
                    text = LanguageHelper.getString("credit_col", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolidIncome,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(74.dp)
                )
                // 5. Balance / জের
                Text(
                    text = LanguageHelper.getString("balance_col", languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(84.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Table Rows
            if (entity.khatianLedgerRows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("no_transactions_found", languageMode).ifEmpty { "No entries in ledger" },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                entity.khatianLedgerRows.forEachIndexed { index, row ->
                    val isEven = index % 2 == 0
                    val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .clickable {
                                row.transactionItem?.let { onTransactionClick(it.transactionWithDetails.transaction) }
                            }
                            .padding(vertical = 7.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Date (Mini Date, oldest first)
                        Text(
                            text = row.miniDateFormatted.ifEmpty { row.dateFormatted },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(66.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 2. Dr / Cr Badge with optional reconcile icon
                        Row(
                            modifier = Modifier.width(42.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (row.isDebit)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                else
                                    SolidIncome.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    if (row.isReconciled && row.id != "opening_balance") {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Reconciled",
                                            tint = SolidIncome,
                                            modifier = Modifier.size(10.dp).padding(end = 1.dp)
                                        )
                                    }
                                    Text(
                                        text = if (row.isDebit) "Dr" else "Cr",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.isDebit) MaterialTheme.colorScheme.error else SolidIncome
                                    )
                                }
                            }
                        }

                        // 3. Debit Amount (Dr)
                        Text(
                            text = if (row.debitAmount != null) "৳ ${RmManagerHelper.formatAmount(row.debitAmount)}" else "—",
                            fontSize = 11.5.sp,
                            fontWeight = if (row.debitAmount != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (row.debitAmount != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(74.dp),
                            maxLines = 1
                        )

                        // 4. Credit Amount (Cr)
                        Text(
                            text = if (row.creditAmount != null) "৳ ${RmManagerHelper.formatAmount(row.creditAmount)}" else "—",
                            fontSize = 11.5.sp,
                            fontWeight = if (row.creditAmount != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (row.creditAmount != null) SolidIncome else MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(74.dp),
                            maxLines = 1
                        )

                        // 5. Running Balance ("জের")
                        Text(
                            text = RmManagerHelper.formatSignedLiabilityAmount(row.balanceJer),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (row.balanceJer < -0.001) MaterialTheme.colorScheme.error else SolidIncome,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(84.dp),
                            maxLines = 1
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                }

                // Table Footer Total Row
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "মোট জের (Total)" else "Total / Net",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(108.dp),
                        maxLines = 1
                    )

                    // Total Dr Sum
                    Text(
                        text = "৳ ${RmManagerHelper.formatAmount(entity.totalBorrowed)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(74.dp),
                        maxLines = 1
                    )

                    // Total Cr Sum
                    Text(
                        text = "৳ ${RmManagerHelper.formatAmount(entity.totalRepaid)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidIncome,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(74.dp),
                        maxLines = 1
                    )

                    // Net Closing Balance
                    Text(
                        text = RmManagerHelper.formatSignedLiabilityAmount(entity.netBalance),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (entity.netBalance < -0.001) MaterialTheme.colorScheme.error else SolidIncome,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(84.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Timeline Card for individual transactions.
 */
@Composable
private fun RmTimelineCard(
    item: RmTransactionItem,
    languageMode: LanguageMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Dr / Cr Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (item.isDebit)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    else
                        SolidIncome.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isDebit) Icons.Default.ArrowDownward else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (item.isDebit) MaterialTheme.colorScheme.error else SolidIncome,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayName,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isReconciled) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Reconciled",
                                tint = SolidIncome,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.dateFormatted} • ${item.categoryName}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (item.note.isNotBlank()) {
                        Text(
                            text = item.note,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount (negative if debit liability increased)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (item.isDebit) "-৳ ${RmManagerHelper.formatAmount(item.amount)}" else "+৳ ${RmManagerHelper.formatAmount(item.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isDebit) MaterialTheme.colorScheme.error else SolidIncome
                )
                Text(
                    text = if (item.isDebit)
                        if (languageMode == LanguageMode.BANGLA) "দেনা বৃদ্ধি" else "Liability Inflow"
                    else
                        if (languageMode == LanguageMode.BANGLA) "পরিশোধ" else "Repaid Outflow",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Reconcile Balance BottomSheet for RM Accounts and RM Others Labels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RmEntityReconcileBottomSheet(
    entity: RmEntityBreakdown,
    currentOutstandingLiability: Double,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (targetRemainingLiability: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountInput by remember { mutableStateOf("") }
    val parsedAmount = abs(amountInput.trim().replace(",", "").toDoubleOrNull() ?: 0.0)
    val diff = parsedAmount - currentOutstandingLiability

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = SolidIncome,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (entity.isAccount) {
                            "${LanguageHelper.getString("reconcile_account", languageMode)} - ${entity.name}"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "লেবেল মিলকরণ - #${entity.name}" else "Reconcile Label - #${entity.name}"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!entity.isAccount) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "শুধুমাত্র #${entity.name} লেবেলের বকেয়া সমন্বয় ও মিলকরণ হবে"
                            else
                                "Only #${entity.name} label balance & transactions will be reconciled",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Outstanding Liability
            Text(
                text = LanguageHelper.getString("total_resting_liability", languageMode),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "-৳ ${RmManagerHelper.formatAmount(currentOutstandingLiability)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (currentOutstandingLiability > 0) MaterialTheme.colorScheme.error else SolidIncome
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Remaining Due Input
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "প্রকৃত বকেয়া দেনা লিখুন (New Ending Due)" else "Actual Outstanding Due (New Balance)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                placeholder = { Text("0.00") },
                prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (abs(diff) >= 0.01) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (diff > 0) {
                        if (languageMode == LanguageMode.BANGLA)
                            "৳ ${RmManagerHelper.formatAmount(diff)} পরিমাণের একটি দেনা বৃদ্ধি সমন্বয় তৈরি করা হবে"
                        else
                            "An adjustment of ৳ ${RmManagerHelper.formatAmount(diff)} will increase liability"
                    } else {
                        if (languageMode == LanguageMode.BANGLA)
                            "৳ ${RmManagerHelper.formatAmount(abs(diff))} পরিমাণের একটি পরিশোধ সমন্বয় তৈরি করা হবে"
                        else
                            "An adjustment of ৳ ${RmManagerHelper.formatAmount(abs(diff))} will be recorded as repayment"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(LanguageHelper.getString("cancel", languageMode).ifEmpty { "Cancel" })
                }

                Button(
                    onClick = {
                        val target = if (amountInput.isBlank()) 0.0 else parsedAmount
                        onConfirm(target)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("save", languageMode).ifEmpty { "Reconcile" },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Resting Money Statement", text)
    clipboard.setPrimaryClip(clip)
}

@Composable
private fun UnifiedRmFilterDialog(
    selectedCategory: RmFilterCategory,
    currentIncludeKeyword: String,
    currentExcludeKeyword: String,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (RmFilterCategory, String, String) -> Unit,
    onReset: () -> Unit
) {
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempInclude by remember { mutableStateOf(currentIncludeKeyword) }
    var tempExclude by remember { mutableStateOf(currentExcludeKeyword) }
    var showKeywordSettings by remember { mutableStateOf(false) }

    val filterOptions = remember(languageMode) {
        listOf(
            Triple(RmFilterCategory.ALL, if (languageMode == LanguageMode.BANGLA) "সকল আইটেম" else "All Entities", Icons.Default.DoneAll),
            Triple(RmFilterCategory.PENDING_ONLY, if (languageMode == LanguageMode.BANGLA) "বকেয়া দেনা" else "Pending Due Only", Icons.AutoMirrored.Filled.TrendingDown),
            Triple(RmFilterCategory.RM_ACCOUNTS, if (languageMode == LanguageMode.BANGLA) "আরএম অ্যাকাউন্ট" else "RM Accounts Only", Icons.Default.AccountBalance),
            Triple(RmFilterCategory.RM_OTHERS, if (languageMode == LanguageMode.BANGLA) "আরএম অন্যান্য লেবেল" else "RM Others Labels", Icons.AutoMirrored.Filled.ReceiptLong),
            Triple(RmFilterCategory.SETTLED_ONLY, if (languageMode == LanguageMode.BANGLA) "পরিশোধিত" else "Settled Only", Icons.Default.CheckCircle),
            Triple(RmFilterCategory.UNRECONCILED, if (languageMode == LanguageMode.BANGLA) "আমিলকৃত" else "Unreconciled Only", Icons.Default.History),
            Triple(RmFilterCategory.RECONCILED, if (languageMode == LanguageMode.BANGLA) "মিলকৃত" else "Reconciled Only", Icons.Default.Verified),
            Triple(RmFilterCategory.HIGH_LIABILITY, if (languageMode == LanguageMode.BANGLA) "বড় দেনা (> ৳১০,০০০)" else "High Due (> ৳10k)", Icons.Default.Payment),
            Triple(RmFilterCategory.RECENT_WEEK, if (languageMode == LanguageMode.BANGLA) "গত ৭ দিনে সক্রিয়" else "Active (Last 7 Days)", Icons.Default.Timeline)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার ও সেটিংস" else "Filter & Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "বিভাগ ফিল্টার নির্বাচন করুন:" else "Filter by Entity Status:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    filterOptions.forEach { (cat, title, icon) ->
                        val isSelected = tempCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tempCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Collapsible / Expandable Keyword Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showKeywordSettings = !showKeywordSettings }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কি-ওয়ার্ড ফিল্টার সেটিংস" else "Keyword Filter Rules",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (showKeywordSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showKeywordSettings) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = LanguageHelper.getString("rm_include_keyword", languageMode).ifEmpty { "Account Filter Keyword" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            OutlinedTextField(
                                value = tempInclude,
                                onValueChange = { tempInclude = it },
                                placeholder = { Text("RM") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column {
                            Text(
                                text = LanguageHelper.getString("rm_exclude_keyword", languageMode).ifEmpty { "Exclude Account Keyword" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            OutlinedTextField(
                                value = tempExclude,
                                onValueChange = { tempExclude = it },
                                placeholder = { Text("RM Others") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        TextButton(
                            onClick = onReset,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = LanguageHelper.getString("reset_defaults", languageMode).ifEmpty { "Reset Defaults" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(tempCategory, tempInclude, tempExclude) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply Filter",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(LanguageHelper.getString("cancel", languageMode).ifEmpty { "Cancel" })
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Visual Analytics Bar Graph Card showing monthly RM Borrowed vs Repaid trends.
 */
@Composable
private fun RmAnalyticsGraphCard(
    rmData: RmManagerHelper.RmManagerScreenData,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val bars = rmData.monthlyBars
    if (bars.isEmpty()) return

    val positiveGreen = Color(0xFF2E7D32)
    val negativeRed = Color(0xFFD32F2F)

    var isExpanded by remember { mutableStateOf(true) }

    val hasAnyActivity = remember(bars) {
        bars.any { it.borrowed > 0 || it.repaid > 0 }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("rm_analytics_graph_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "দায় ও পরিশোধ ট্রেন্ড" else "Liability & Repayment Trend",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "গত ৬ মাসের আরএম লেনদেন প্রবাহ" else "Last 6 months RM monthly activity",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mini Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(negativeRed))
                            Text(if (languageMode == LanguageMode.BANGLA) "দায়" else "Due", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(positiveGreen))
                            Text(if (languageMode == LanguageMode.BANGLA) "পরিশোধ" else "Repaid", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle graph",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (!hasAnyActivity) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বিগত ৬ মাসে কোনো আরএম লেনদেন নেই" else "No RM activity in the past 6 months",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        val maxVal = remember(bars) {
                            val m = bars.maxOfOrNull { bar -> kotlin.math.max(bar.borrowed, bar.repaid) } ?: 100.0
                            if (m <= 0.0) 100.0 else m
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .padding(vertical = 2.dp)
                        ) {
                            val barCount = bars.size
                            val barWidth = (size.width / (barCount * 3.2f)).coerceAtMost(16.dp.toPx())
                            val groupSpacing = size.width / barCount
                            val chartHeight = size.height - 16.dp.toPx()

                            // Base horizontal line
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, chartHeight),
                                end = Offset(size.width, chartHeight),
                                strokeWidth = 1.dp.toPx()
                            )

                            bars.forEachIndexed { idx, bar ->
                                val groupCenter = idx * groupSpacing + groupSpacing / 2f

                                // Borrowed / Liability Bar (Red)
                                val borrowedH = ((bar.borrowed / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                                if (borrowedH > 0f) {
                                    drawRoundRect(
                                        color = negativeRed,
                                        topLeft = Offset(groupCenter - barWidth - 1.5.dp.toPx(), chartHeight - borrowedH),
                                        size = Size(barWidth, borrowedH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }

                                // Repaid Bar (Green)
                                val repaidH = ((bar.repaid / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                                if (repaidH > 0f) {
                                    drawRoundRect(
                                        color = positiveGreen,
                                        topLeft = Offset(groupCenter + 1.5.dp.toPx(), chartHeight - repaidH),
                                        size = Size(barWidth, repaidH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Bar Labels (Month names)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            bars.forEach { bar ->
                                Text(
                                    text = bar.label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
