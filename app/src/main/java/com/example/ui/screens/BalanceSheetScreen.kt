package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.ui.components.rememberCollapsibleCardState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedActiveFilterBar
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.specs.BalanceSheetFilterSpec
import com.example.ui.components.BalanceSheetQuickShortcutsRow
import com.example.util.FilterStore
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CreditCard
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.AppTabHeader
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.dialogs.AccountCalculationDialog
import com.example.ui.dialogs.ExcludedAccountsDialog
import com.example.ui.dialogs.InactiveAccountsDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import androidx.compose.material.icons.filled.PauseCircle
import com.example.util.AccountCalcConfig
import com.example.util.BalanceSheetAccountRow
import com.example.util.BalanceSheetComparisonData
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetGroup
import com.example.util.BalanceSheetHelper
import com.example.util.BalanceSheetSortOrder
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import com.example.util.TabFilterPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class BalanceSheetFilterState(
    val preset: BalanceSheetComparisonPreset = BalanceSheetComparisonPreset.THIS_MONTH,
    val customBaseDateMs: Long? = null,
    val customCompareDateMs: Long? = null,
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),
    val excludeZeroAmounts: Boolean = true,
    val filterNonZeroGroups: Boolean = false,
    val displayCurrency: Boolean = true,
    val displayCurrencySymbol: Boolean = true,
    val sortOrder: BalanceSheetSortOrder = BalanceSheetSortOrder.NAME_ASC,
    val showHiddenAccounts: Boolean = false,
    val showOnlyCurrentBalance: Boolean = false,
    val showOnlyAccountsWithoutGroups: Boolean = false
) {
    val isFilterActive: Boolean
        get() = preset != BalanceSheetComparisonPreset.THIS_MONTH ||
                selectedAccountIds.isNotEmpty() ||
                selectedStatusSet.isNotEmpty() ||
                !excludeZeroAmounts ||
                filterNonZeroGroups ||
                !displayCurrency ||
                !displayCurrencySymbol ||
                (sortOrder != BalanceSheetSortOrder.NAME_ASC && sortOrder != BalanceSheetSortOrder.DEFAULT) ||
                showHiddenAccounts ||
                showOnlyCurrentBalance ||
                showOnlyAccountsWithoutGroups
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit = {},
    onAddTransactionClick: () -> Unit,
    onAccountClick: ((Account) -> Unit)? = null,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    onToggleActiveStatus: ((Account, Boolean) -> Unit)? = null,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)? = null,
    onSaveCalculationSetting: ((Account, Boolean, Double) -> Unit)? = null,
    onResetAccountCalculation: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current
    val filterStore = remember { FilterStore.getInstance(context) }
    var filterState by remember { mutableStateOf(filterStore.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)) }
    val updateFilterState: (FilterState) -> Unit = { newState ->
        filterState = newState
        filterStore.saveFilterState(BalanceSheetFilterSpec.SPEC_KEY, newState)
    }

    val balanceSheetSpec = remember(accounts) { BalanceSheetFilterSpec.createSpec(accounts) }

    var showTimelineScreen by remember { mutableStateOf(false) }

    if (showTimelineScreen) {
        AccountTimelineScreen(
            accounts = accounts,
            transactions = transactions,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false }
        )
        return
    }

    var baseDateMs by remember {
        val defaultBase = BalanceSheetHelper.getPresetDateRanges(BalanceSheetFilterSpec.getDatePreset(filterState)).first
        mutableStateOf(defaultBase)
    }
    var compareDateMs by remember {
        val defaultCompare = BalanceSheetHelper.getPresetDateRanges(BalanceSheetFilterSpec.getDatePreset(filterState)).second
        mutableStateOf(defaultCompare)
    }

    val (resolvedBaseMs, resolvedCompareMs) = remember(balanceSheetSpec, filterState, baseDateMs, compareDateMs) {
        BalanceSheetFilterSpec.resolveComparisonDates(balanceSheetSpec, filterState, baseDateMs, compareDateMs)
    }

    val selectedPreset = remember(filterState) { BalanceSheetFilterSpec.getDatePreset(filterState) }
    val selectedAccountIds = remember(filterState) { BalanceSheetFilterSpec.getSelectedAccountIds(filterState) }
    val selectedStatusSet = remember(filterState) { BalanceSheetFilterSpec.getSelectedStatuses(filterState) }
    val sortOrder = remember(filterState) { BalanceSheetFilterSpec.getSortOrder(filterState) }
    val excludeZeroAmounts = remember(filterState) { BalanceSheetFilterSpec.getExcludeZero(filterState) }
    val filterNonZeroGroups = remember(filterState) { BalanceSheetFilterSpec.getFilterNonZeroGroups(filterState) }
    val showHiddenAccounts = remember(filterState) { BalanceSheetFilterSpec.getShowHidden(filterState) }
    val showOnlyCurrentBalance = remember(filterState) { BalanceSheetFilterSpec.getShowOnlyCurrent(filterState) }
    val showOnlyAccountsWithoutGroups = remember(filterState) { BalanceSheetFilterSpec.getShowWithoutGroups(filterState) }
    val activeTabMode = remember(filterState) { BalanceSheetFilterSpec.getActiveTab(filterState) }

    // Modal Filter Dialog & Calculation Mode
    var showFilterDialog by remember { mutableStateOf(false) }
    var showExcludedAccountsDialog by remember { mutableStateOf(false) }
    var showInactiveAccountsDialog by remember { mutableStateOf(false) }
    var isCalcMode by remember { mutableStateOf(false) }
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }
    var isDualDateFlow by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }

    // Counts for Excluded and Inactive accounts
    val excludedAccountsCount = remember(accounts, accountCalcConfig) {
        accounts.count { !accountCalcConfig.isIncluded(it.id) || accountCalcConfig.getSetting(it.id).adjustmentAmount != 0.0 }
    }
    val inactiveAccountsCount = remember(accounts) {
        accounts.count { !it.isActive }
    }

    // Date Pickers for Custom Mode
    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }

    // Collapsed/Expanded parent account rows
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }
    // Calculate balance sheet data
    val balanceSheetData = remember(
        accounts,
        transactions,
        resolvedBaseMs,
        resolvedCompareMs,
        selectedPreset,
        selectedAccountIds,
        selectedStatusSet,
        showHiddenAccounts,
        excludeZeroAmounts,
        filterNonZeroGroups,
        sortOrder,
        searchQuery,
        accountCalcConfig,
        showOnlyAccountsWithoutGroups,
        languageMode
    ) {
        BalanceSheetHelper.calculateBalanceSheet(
            accounts = accounts,
            transactions = transactions,
            baseDateEpochMs = resolvedBaseMs,
            compareDateEpochMs = resolvedCompareMs,
            preset = selectedPreset,
            selectedAccountIds = selectedAccountIds,
            selectedStatusSet = selectedStatusSet,
            activeOnly = !showHiddenAccounts,
            showHiddenAccounts = showHiddenAccounts,
            excludeZeroAmounts = excludeZeroAmounts,
            filterNonZeroGroups = filterNonZeroGroups,
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            accountCalcConfig = accountCalcConfig,
            showOnlyAccountsWithoutGroups = showOnlyAccountsWithoutGroups,
            languageMode = languageMode
        )
    }

    val allGroupsExpanded = remember(expandedMap, balanceSheetData) {
        val allParentIds = (balanceSheetData.assetGroups.map { it.parentAccount.id } +
                balanceSheetData.liabilityGroups.map { it.parentAccount.id }).toSet()
        if (allParentIds.isEmpty()) true
        else allParentIds.all { expandedMap[it] != false }
    }

    val listState = rememberLazyListState()
    val collapsibleCardState = rememberCollapsibleCardState(collapseRangePx = 220f)

    Box(modifier = Modifier.fillMaxSize().testTag("balance_sheet_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar with Search, Filter, Timeline and Export buttons
            AppTabHeader(
                title = LanguageHelper.getString("balance_sheet", languageMode),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search accounts...",
                showSearchButton = true,
                showFilterButton = true,
                isFilterActive = balanceSheetSpec.isActive(filterState),
                activeFilterCount = balanceSheetSpec.activeFilterCount(filterState),
                onFilterClick = { showFilterDialog = true },
                showTimelineButton = true,
                onTimelineClick = { showTimelineScreen = true },
                onOpenDrawer = onOpenDrawer,
                actions = {
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            val isComparison = !showOnlyCurrentBalance && balanceSheetData.baseDateLabel.isNotBlank()
                            val asOfDateLabel = if (isComparison) {
                                "${balanceSheetData.compareDateLabel} vs ${balanceSheetData.baseDateLabel}"
                            } else {
                                balanceSheetData.compareDateLabel
                            }
                            TabExportHelper.exportBalanceSheet(
                                context = context,
                                format = format,
                                asOfDateLabel = asOfDateLabel,
                                assetGroups = balanceSheetData.assetGroups,
                                totalAssets = balanceSheetData.totalAssetsCurrent,
                                liabilityGroups = balanceSheetData.liabilityGroups,
                                totalLiabilities = balanceSheetData.totalLiabilitiesCurrent,
                                netWorth = balanceSheetData.netWorthCurrent,
                                comparisonEnabled = isComparison,
                                baseDateLabel = balanceSheetData.baseDateLabel,
                                compareDateLabel = balanceSheetData.compareDateLabel,
                                totalAssetsBase = balanceSheetData.totalAssetsBase,
                                totalLiabilitiesBase = balanceSheetData.totalLiabilitiesBase,
                                netWorthBase = balanceSheetData.netWorthBase,
                                netWorthDelta = balanceSheetData.netWorthDelta,
                                languageMode = languageMode
                            )
                        }
                    )
                }
            )

            // Fixed Timeline Control & Date Columns Card at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                ComparisonDatesCard(
                    preset = selectedPreset,
                    baseDateLabel = balanceSheetData.baseDateLabel,
                    compareDateLabel = balanceSheetData.compareDateLabel,
                    showOnlyCurrentBalance = showOnlyCurrentBalance,
                    languageMode = languageMode,
                    isCalcMode = isCalcMode,
                    sortOrder = sortOrder,
                    onToggleSortOrder = {
                        val nextSort = if (sortOrder == BalanceSheetSortOrder.NAME_ASC || sortOrder == BalanceSheetSortOrder.DEFAULT) {
                            BalanceSheetSortOrder.AMOUNT_DESC
                        } else {
                            BalanceSheetSortOrder.NAME_ASC
                        }
                        updateFilterState(BalanceSheetFilterSpec.withSortOrder(filterState, nextSort))
                    },
                    onToggleCalcMode = { isCalcMode = !isCalcMode },
                    onOpenFilter = { showFilterDialog = true },
                    onPickBaseDate = {
                        isDualDateFlow = false
                        showBaseDatePicker = true
                    },
                    onPickCompareDate = {
                        isDualDateFlow = false
                        showCompareDatePicker = true
                    },
                    onPickBothDates = {
                        isDualDateFlow = true
                        showBaseDatePicker = true
                    }
                )
            }

            // Quick Shortcuts Toolbar (Fast 1-tap presets, view toggle, sort, and zero-balance filter)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                BalanceSheetQuickShortcutsRow(
                    filterState = filterState,
                    onFilterChange = { updateFilterState(it) },
                    isCalcMode = isCalcMode,
                    onToggleCalcMode = { isCalcMode = !isCalcMode },
                    allGroupsExpanded = allGroupsExpanded,
                    onToggleExpandAll = { expand ->
                        val allParentIds = (balanceSheetData.assetGroups.map { it.parentAccount.id } +
                                balanceSheetData.liabilityGroups.map { it.parentAccount.id }).toSet()
                        allParentIds.forEach { parentId ->
                            expandedMap[parentId] = expand
                        }
                    },
                    onOpenFilterDialog = { showFilterDialog = true },
                    languageMode = languageMode
                )
            }

            // Active Filter Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                UnifiedActiveFilterBar(
                    spec = balanceSheetSpec,
                    state = filterState,
                    onFilterChange = { updateFilterState(it) },
                    languageMode = languageMode,
                    onOpenFilterDialog = { showFilterDialog = true }
                )
            }

            // Top Collapsible Summary Card (Smoothly transitions between full NetWorthSummaryCard and MiniNetWorthSummaryCard)
            AnimatedContent(
                targetState = collapsibleCardState.isCollapsed,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(220)))
                },
                label = "balance_sheet_summary_transition"
            ) { isCollapsed ->
                if (!isCollapsed) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                        NetWorthSummaryCard(
                            data = balanceSheetData,
                            displayCurrency = true,
                            displayCurrencySymbol = true,
                            showOnlyCurrentBalance = showOnlyCurrentBalance,
                            languageMode = languageMode
                        )
                    }
                } else {
                    MiniNetWorthSummaryCard(
                        data = balanceSheetData,
                        displayCurrency = true,
                        displayCurrencySymbol = true,
                        languageMode = languageMode
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .nestedScroll(collapsibleCardState.createNestedScrollConnection(listState)),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp)
            ) {
                // Sections based on activeTabMode ("all", "assets", or "liabilities")
                if (activeTabMode == BalanceSheetFilterSpec.TAB_ALL || activeTabMode == BalanceSheetFilterSpec.TAB_ASSETS) {
                    item(key = "section_header_assets") {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "সম্পদ (ASSETS)" else "ASSETS",
                            baseAmount = balanceSheetData.totalAssetsBase,
                            currentAmount = balanceSheetData.totalAssetsCurrent,
                            displayCurrency = true,
                            displayCurrencySymbol = true,
                            showOnlyCurrentBalance = showOnlyCurrentBalance,
                            headerColor = MaterialTheme.colorScheme.primary,
                            languageMode = languageMode
                        )
                    }

                    if (balanceSheetData.assetGroups.isEmpty()) {
                        item(key = "empty_assets") {
                            EmptySectionPlaceholder(
                                message = if (languageMode == LanguageMode.BANGLA) "কোন সম্পদ অ্যাকাউন্ট পাওয়া যায়নি" else "No asset accounts found"
                            )
                        }
                    } else {
                        itemsIndexed(balanceSheetData.assetGroups, key = { _, group -> "asset_${group.parentAccount.id}" }) { index, group ->
                            val isExpanded = expandedMap[group.parentAccount.id] ?: true
                            BalanceSheetGroupItem(
                                group = group,
                                isExpanded = isExpanded,
                                isCalcMode = isCalcMode,
                                accountCalcConfig = accountCalcConfig,
                                displayCurrency = true,
                                displayCurrencySymbol = true,
                                showOnlyCurrentBalance = showOnlyCurrentBalance,
                                languageMode = languageMode,
                                onToggleExpand = {
                                    expandedMap[group.parentAccount.id] = !isExpanded
                                },
                                onToggleIncludeStatus = onToggleIncludeStatus,
                                onRequestAdjustCalculation = { acc, bal ->
                                    calcDialogTarget = Pair(acc, bal)
                                },
                                onAccountClick = onAccountClick
                            )
                            if (index < balanceSheetData.assetGroups.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                if (activeTabMode == BalanceSheetFilterSpec.TAB_ALL) {
                    item(key = "assets_liabilities_spacer") {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                if (activeTabMode == BalanceSheetFilterSpec.TAB_ALL || activeTabMode == BalanceSheetFilterSpec.TAB_LIABILITIES) {
                    item(key = "section_header_liabilities") {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "দায় (LIABILITIES)" else "LIABILITIES",
                            baseAmount = balanceSheetData.totalLiabilitiesBase,
                            currentAmount = balanceSheetData.totalLiabilitiesCurrent,
                            displayCurrency = true,
                            displayCurrencySymbol = true,
                            showOnlyCurrentBalance = showOnlyCurrentBalance,
                            headerColor = SolidExpense,
                            languageMode = languageMode
                        )
                    }

                    if (balanceSheetData.liabilityGroups.isEmpty()) {
                        item(key = "empty_liabilities") {
                            EmptySectionPlaceholder(
                                message = if (languageMode == LanguageMode.BANGLA) "কোন দায় অ্যাকাউন্ট পাওয়া যায়নি" else "No liability accounts found"
                            )
                        }
                    } else {
                        itemsIndexed(balanceSheetData.liabilityGroups, key = { _, group -> "liability_${group.parentAccount.id}" }) { index, group ->
                            val isExpanded = expandedMap[group.parentAccount.id] ?: true
                            BalanceSheetGroupItem(
                                group = group,
                                isExpanded = isExpanded,
                                isCalcMode = isCalcMode,
                                accountCalcConfig = accountCalcConfig,
                                displayCurrency = true,
                                displayCurrencySymbol = true,
                                showOnlyCurrentBalance = showOnlyCurrentBalance,
                                languageMode = languageMode,
                                onToggleExpand = {
                                    expandedMap[group.parentAccount.id] = !isExpanded
                                },
                                onToggleIncludeStatus = onToggleIncludeStatus,
                                onRequestAdjustCalculation = { acc, bal ->
                                    calcDialogTarget = Pair(acc, bal)
                                },
                                onAccountClick = onAccountClick
                            )
                            if (index < balanceSheetData.liabilityGroups.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Speed Dial FAB & Segmented [ Assets | Liabilities ] Buttons at bottom
        val headerScrollState = LocalHeaderScrollState.current
        val rotationAngle by animateFloatAsState(
            targetValue = if (isSpeedDialExpanded) 45f else 0f,
            label = "fab_rotation"
        )

        AutoHidingBottomContainer(
            headerScrollState = headerScrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Expanded Speed Dial Options
                AnimatedVisibility(
                    visible = isSpeedDialExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        // Option 1: Inactive Accounts
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    showInactiveAccountsDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                val label = if (languageMode == LanguageMode.BANGLA) {
                                    if (inactiveAccountsCount > 0) "নিষ্ক্রিয় অ্যাকাউন্ট ($inactiveAccountsCount)" else "নিষ্ক্রিয় অ্যাকাউন্ট"
                                } else {
                                    if (inactiveAccountsCount > 0) "Inactive Accounts ($inactiveAccountsCount)" else "Inactive Accounts"
                                }
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    showInactiveAccountsDialog = true
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PauseCircle,
                                        contentDescription = "Inactive Accounts",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Option 2: Excluded Accounts
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    showExcludedAccountsDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                val label = if (languageMode == LanguageMode.BANGLA) {
                                    if (excludedAccountsCount > 0) "বাদ দেওয়া অ্যাকাউন্ট ($excludedAccountsCount)" else "বাদ দেওয়া অ্যাকাউন্ট"
                                } else {
                                    if (excludedAccountsCount > 0) "Excluded Accounts ($excludedAccountsCount)" else "Excluded Accounts"
                                }
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    showExcludedAccountsDialog = true
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Excluded Accounts",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Option 3: Add Account
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    onAddAccountClick()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "নতুন অ্যাকাউন্ট" else "New Account",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    onAddAccountClick()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "New Account",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Option 4: Add Transaction
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    onAddTransactionClick()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "নতুন লেনদেন" else "New Transaction",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                onClick = {
                                    isSpeedDialExpanded = false
                                    onAddTransactionClick()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New Transaction",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Floating Action Button (Three horizontal lines menu icon)
                Surface(
                    onClick = { isSpeedDialExpanded = !isSpeedDialExpanded },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSpeedDialExpanded) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Quick Actions",
                            tint = Color.White,
                            modifier = Modifier
                                .size(26.dp)
                                .rotate(if (isSpeedDialExpanded) rotationAngle else 0f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Segmented Toggle: [ Assets (সম্পদ) | All (সকল) | Liabilities (দায়) ]
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Assets Button
                        val isAssets = activeTabMode == BalanceSheetFilterSpec.TAB_ASSETS
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAssets) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { updateFilterState(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_ASSETS)) }
                                .testTag("balance_sheet_tab_assets")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = if (isAssets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "Assets",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isAssets) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAssets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }

                        // 2. All Button (Middle - Default)
                        val isAll = activeTabMode == BalanceSheetFilterSpec.TAB_ALL
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAll) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { updateFilterState(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_ALL)) }
                                .testTag("balance_sheet_tab_all")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সকল" else "All",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }

                        // 3. Liabilities Button
                        val isLiabilities = activeTabMode == BalanceSheetFilterSpec.TAB_LIABILITIES
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiabilities) SolidExpense.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { updateFilterState(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_LIABILITIES)) }
                                .testTag("balance_sheet_tab_liabilities")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (isLiabilities) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "দায়" else "Liabilities",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isLiabilities) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLiabilities) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Account Calculation Inclusion/Adjustment Dialog
    if (calcDialogTarget != null) {
        val (targetAccount, actualBal) = calcDialogTarget!!
        AccountCalculationDialog(
            account = targetAccount,
            actualBalance = actualBal,
            currentSetting = accountCalcConfig.getSetting(targetAccount.id),
            languageMode = languageMode,
            onDismiss = { calcDialogTarget = null },
            onSave = { isIncluded, adjustment ->
                onSaveCalculationSetting?.invoke(targetAccount, isIncluded, adjustment)
                calcDialogTarget = null
            },
            onReset = {
                onResetAccountCalculation?.invoke(targetAccount)
                calcDialogTarget = null
            }
        )
    }

    // Excluded Accounts Dialog
    if (showExcludedAccountsDialog) {
        ExcludedAccountsDialog(
            allAccounts = accounts,
            transactions = transactions,
            accountCalcConfig = accountCalcConfig,
            languageMode = languageMode,
            onDismiss = { showExcludedAccountsDialog = false },
            onToggleIncludeStatus = { acc, isIncluded ->
                onToggleIncludeStatus?.invoke(acc, isIncluded)
            },
            onAdjustCalculation = { acc, bal ->
                calcDialogTarget = Pair(acc, bal)
                showExcludedAccountsDialog = false
            },
            onResetAccountCalculation = { acc ->
                onResetAccountCalculation?.invoke(acc)
            },
            onAccountClick = onAccountClick
        )
    }

    // Inactive Accounts Dialog
    if (showInactiveAccountsDialog) {
        InactiveAccountsDialog(
            allAccounts = accounts,
            transactions = transactions,
            languageMode = languageMode,
            onDismiss = { showInactiveAccountsDialog = false },
            onToggleActiveStatus = { acc, isActive ->
                onToggleActiveStatus?.invoke(acc, isActive)
            },
            onEditAccountClick = onEditAccountClick,
            onAccountClick = onAccountClick
        )
    }

    // Unified Filter Dialog
    if (showFilterDialog) {
        UnifiedFilterDialog(
            spec = balanceSheetSpec,
            initialState = filterState,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                updateFilterState(newFilter)
                showFilterDialog = false
            },
            countProvider = { state ->
                val accIds = BalanceSheetFilterSpec.getSelectedAccountIds(state)
                if (accIds.isEmpty()) accounts.size else accIds.size
            }
        )
    }

    // Custom Date Range Pickers
    if (showBaseDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = baseDateMs)
        DatePickerDialog(
            onDismissRequest = { showBaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        baseDateMs = it
                        showBaseDatePicker = false
                        if (isDualDateFlow) {
                            showCompareDatePicker = true
                        } else {
                            updateFilterState(BalanceSheetFilterSpec.withCustomDates(filterState, it, compareDateMs))
                        }
                    }
                }) {
                    Text(
                        if (isDualDateFlow) {
                            if (languageMode == LanguageMode.BANGLA) "পরবর্তী" else "Next"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "প্রয়োগ" else "Apply"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "বেস তারিখ নির্বাচন করুন (তুলনার শুরুর তারিখ)" else "Select Base Comparison Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                DatePicker(state = dateState)
            }
        }
    }

    if (showCompareDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = compareDateMs)
        DatePickerDialog(
            onDismissRequest = { showCompareDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        compareDateMs = it
                        showCompareDatePicker = false
                        updateFilterState(BalanceSheetFilterSpec.withCustomDates(filterState, baseDateMs, it))
                    }
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ" else "Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বর্তমান তারিখ নির্বাচন করুন" else "Select Compare Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                DatePicker(state = dateState)
            }
        }
    }
}

/**
 * Format balance with or without currency based on displayCurrency toggle
 */
fun formatBalance(
    amount: Double,
    displayCurrency: Boolean,
    languageMode: LanguageMode,
    displayCurrencySymbol: Boolean = true
): String {
    return if (displayCurrency) {
        if (displayCurrencySymbol) {
            LanguageHelper.formatCurrency(amount, languageMode)
        } else {
            LanguageHelper.formatNumber(amount, languageMode)
        }
    } else {
        LanguageHelper.formatNumber(amount, languageMode)
    }
}


@Composable
private fun NetWorthSummaryCard(
    data: BalanceSheetComparisonData,
    displayCurrency: Boolean,
    displayCurrencySymbol: Boolean = true,
    showOnlyCurrentBalance: Boolean,
    languageMode: LanguageMode
) {
    val isPositive = data.netWorthDelta >= 0
    val deltaPercent = if (Math.abs(data.netWorthBase) > 0.001) {
        (data.netWorthDelta / Math.abs(data.netWorthBase)) * 100.0
    } else 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Total Assets (Left)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets",
                    fontSize = 12.sp,
                    color = Color(0xFF0D9488),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatBalance(data.totalAssetsCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!showOnlyCurrentBalance) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Prev: ${formatBalance(data.totalAssetsBase, displayCurrency, languageMode, displayCurrencySymbol)}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // Divider 1
            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )

            // 2. Total Net Worth (Center)
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ (নেট ওর্থ)" else "Total Net Worth",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatBalance(data.netWorthCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (data.netWorthCurrent >= 0) SolidPrimary else SolidExpense,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!showOnlyCurrentBalance) {
                    Spacer(modifier = Modifier.height(3.dp))
                    val sign = if (isPositive) "+" else ""
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPositive) SolidIncome.copy(alpha = 0.18f) else SolidExpense.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isPositive) SolidIncome else SolidExpense,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$sign${LanguageHelper.formatNumber(deltaPercent, languageMode)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPositive) SolidIncome else SolidExpense
                            )
                        }
                    }
                }
            }

            // Divider 2
            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )

            // 3. Total Liabilities (Right)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatBalance(data.totalLiabilitiesCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!showOnlyCurrentBalance) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Prev: ${formatBalance(data.totalLiabilitiesBase, displayCurrency, languageMode, displayCurrencySymbol)}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniNetWorthSummaryCard(
    data: BalanceSheetComparisonData,
    displayCurrency: Boolean,
    displayCurrencySymbol: Boolean = true,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Assets
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets",
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatBalance(data.totalAssetsCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 2.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            // Net Worth
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নেট ওর্থ" else "Net Worth",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatBalance(data.netWorthCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (data.netWorthCurrent >= 0) MaterialTheme.colorScheme.primary else SolidExpense,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 2.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            // Liabilities
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities",
                    fontSize = 9.5.sp,
                    color = SolidExpense,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatBalance(data.totalLiabilitiesCurrent, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolidExpense,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ComparisonDatesCard(
    preset: BalanceSheetComparisonPreset,
    baseDateLabel: String,
    compareDateLabel: String,
    showOnlyCurrentBalance: Boolean,
    languageMode: LanguageMode,
    isCalcMode: Boolean,
    sortOrder: BalanceSheetSortOrder = BalanceSheetSortOrder.NAME_ASC,
    onToggleSortOrder: () -> Unit,
    onToggleCalcMode: () -> Unit,
    onOpenFilter: () -> Unit,
    onPickBaseDate: () -> Unit,
    onPickCompareDate: () -> Unit,
    onPickBothDates: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset Capsule / Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { onOpenFilter() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val presetName = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn
                        Text(
                            text = presetName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action Buttons: Sort (Icon only) + Calc Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sort Button beside Calc Button (Default: A to Z, Click: Sort by Amount - icon only, no text)
                    val isSortedByAmount = sortOrder == BalanceSheetSortOrder.AMOUNT_DESC || sortOrder == BalanceSheetSortOrder.AMOUNT_ASC
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSortedByAmount) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSortOrder() }
                            .testTag("balance_sheet_sort_toggle")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = if (isSortedByAmount) "Sort by Amount" else "Sort A to Z",
                                tint = if (isSortedByAmount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Calculation Inclusion/Exclusion Toggle Button (replaces legacy Edit button)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCalcMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onToggleCalcMode() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Calculation Settings",
                                tint = if (isCalcMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "হিসাব গণনা" else "Calc",
                                fontSize = 11.sp,
                                fontWeight = if (isCalcMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCalcMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Range Display with two clickable dates to compare
            if (showOnlyCurrentBalance) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onPickCompareDate() }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বর্তমান ব্যালেন্স স্ন্যাপশট" else "Current Balance Snapshot",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = compareDateLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Change Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clickable Base Date Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPickBaseDate() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বেস তারিখ" else "Base Date",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = baseDateLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Edit Base Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Dual arrow separator
                    IconButton(
                        onClick = onPickBothDates,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Pick Both Dates",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Clickable Compare Date Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPickCompareDate() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক তারিখ" else "Compare Date",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = compareDateLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Edit Compare Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    baseAmount: Double,
    currentAmount: Double,
    displayCurrency: Boolean,
    displayCurrencySymbol: Boolean = true,
    showOnlyCurrentBalance: Boolean,
    headerColor: Color,
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    val delta = currentAmount - baseAmount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = headerColor,
            letterSpacing = 0.5.sp
        )

        if (showOnlyCurrentBalance) {
            Box(
                modifier = Modifier.widthIn(min = 85.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = formatBalance(currentAmount, displayCurrency, languageMode, displayCurrencySymbol),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.widthIn(min = 75.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = formatBalance(baseAmount, displayCurrency, languageMode, displayCurrencySymbol),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier.widthIn(min = 85.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = formatBalance(currentAmount, displayCurrency, languageMode, displayCurrencySymbol),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        ChangeIndicator(delta = delta)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceSheetGroupItem(
    group: BalanceSheetGroup,
    isExpanded: Boolean,
    isCalcMode: Boolean,
    accountCalcConfig: AccountCalcConfig,
    displayCurrency: Boolean,
    displayCurrencySymbol: Boolean = true,
    showOnlyCurrentBalance: Boolean,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)?,
    onRequestAdjustCalculation: (Account, Double) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val isIncluded = accountCalcConfig.isIncluded(group.parentAccount.id)
    val isAdjusted = accountCalcConfig.getAdjustment(group.parentAccount.id) != 0.0 || group.adjustmentAmount != 0.0
    val adjustment = if (accountCalcConfig.getAdjustment(group.parentAccount.id) != 0.0) {
        accountCalcConfig.getAdjustment(group.parentAccount.id)
    } else {
        group.adjustmentAmount
    }
    val defaultPrimaryColor = MaterialTheme.colorScheme.primary
    val parentAccColor = remember(group.parentAccount.colorHex, defaultPrimaryColor) {
        IconHelper.parseColorHex(group.parentAccount.colorHex, defaultPrimaryColor)
    }

    if (group.subAccounts.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 3.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                // Group Header Banner
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExpand() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Group Icon Squircle + Group Name (with Show/Hide Chevron right after name) + Percentage Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            // Group Icon (Squircle shaped border, 22dp icon with 0.65dp hairline border)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.5.dp))
                                    .background(if (isIncluded) parentAccColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 0.65.dp,
                                        color = if (isIncluded) parentAccColor.copy(alpha = 0.30f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(6.5.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                IconHelper.AppIcon(
                                    iconName = group.parentAccount.iconName,
                                    contentDescription = null,
                                    tint = if (isIncluded) parentAccColor else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                // Top Line: Group Name + Show/Hide Chevron right after name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    val chevronRotation by animateFloatAsState(
                                        targetValue = if (isExpanded) 0f else -90f,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "group_chevron"
                                    )

                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        modifier = Modifier
                                            .size(17.dp)
                                            .rotate(chevronRotation)
                                    )

                                    // Excluded badge
                                    if (!isIncluded) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SolidExpense.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excluded",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense,
                                                modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    // Dynamic Reverse Group Tag (e.g. From Liability / From Asset)
                                    if (group.dynamicTag != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SolidIncome.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = group.dynamicTag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidIncome,
                                                modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                 // Bottom Line: Group Percentage Pill + Adjustment Pill under group name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isIncluded) {
                                        val pctStr = String.format(java.util.Locale.US, "%.1f%%", group.percentageShare)
                                        val localizedPct = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(pctStr) else pctStr
                                        val totalLabel = if (languageMode == LanguageMode.BANGLA) "মোট" else "in total"
                                        Surface(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "$localizedPct $totalLabel",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                                modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (isAdjusted && isIncluded) {
                                        val sign = if (adjustment > 0) "+" else ""
                                        val adjFormatted = if (displayCurrencySymbol) {
                                            "${sign}${LanguageHelper.formatCurrency(adjustment, languageMode)}"
                                        } else {
                                            "${sign}${String.format("%.2f", adjustment)}"
                                        }
                                        val adjLabel = if (languageMode == LanguageMode.BANGLA) "সমন্বয়" else "Adj"
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        ) {
                                            Text(
                                                text = "$adjLabel: $adjFormatted",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Right: Group Total Amount + Calc Mode actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                if (showOnlyCurrentBalance) {
                                    Text(
                                        text = formatBalance(group.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                        maxLines = 1
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = formatBalance(group.effectiveBaseBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.outline,
                                            textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = " → ",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = formatBalance(group.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                            textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        ChangeIndicator(delta = group.delta)
                                    }
                                }
                            }

                            if (isCalcMode) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onToggleIncludeStatus?.invoke(group.parentAccount, !isIncluded) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isIncluded) "Exclude" else "Include",
                                        tint = if (isIncluded) SolidIncome else SolidExpense,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onRequestAdjustCalculation(group.parentAccount, group.currentBalance) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Adjust",
                                        tint = if (isAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub Accounts Indented Cards List
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 6.dp, top = 2.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        group.subAccounts.forEach { subRow ->
                            SubAccountRowItem(
                                row = subRow,
                                isCalcMode = isCalcMode,
                                accountCalcConfig = accountCalcConfig,
                                displayCurrency = displayCurrency,
                                displayCurrencySymbol = displayCurrencySymbol,
                                showOnlyCurrentBalance = showOnlyCurrentBalance,
                                languageMode = languageMode,
                                onToggleIncludeStatus = onToggleIncludeStatus,
                                onRequestAdjustCalculation = onRequestAdjustCalculation,
                                onAccountClick = onAccountClick
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Standalone Account Card
        val standaloneShape = RoundedCornerShape(12.dp)
        Card(
            shape = standaloneShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                .clickable { onAccountClick?.invoke(group.parentAccount) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Account Icon + Name Column (with percentage pill under name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Standalone Account Icon (Squircle shaped border, 20dp icon with 0.65dp hairline border)
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .clip(RoundedCornerShape(5.5.dp))
                            .background(if (isIncluded) parentAccColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 0.65.dp,
                                color = if (isIncluded) parentAccColor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(5.5.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconHelper.AppIcon(
                            iconName = group.parentAccount.iconName,
                            contentDescription = null,
                            tint = if (isIncluded) parentAccColor else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Excluded badge
                            if (!isIncluded) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SolidExpense.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excluded",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidExpense,
                                        modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Dynamic Reverse Group Tag (e.g. From Liability / From Asset)
                            if (group.dynamicTag != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SolidIncome.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = group.dynamicTag,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidIncome,
                                        modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Bottom Line: Percentage Pill + Adjustment Pill under standalone account name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isIncluded) {
                                val pctStr = String.format(java.util.Locale.US, "%.1f%%", group.percentageShare)
                                val localizedPct = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(pctStr) else pctStr
                                val totalLabel = if (languageMode == LanguageMode.BANGLA) "মোট" else "in total"
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "$localizedPct $totalLabel",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (isAdjusted && isIncluded) {
                                val sign = if (adjustment > 0) "+" else ""
                                val adjFormatted = if (displayCurrencySymbol) {
                                    "${sign}${LanguageHelper.formatCurrency(adjustment, languageMode)}"
                                } else {
                                    "${sign}${String.format("%.2f", adjustment)}"
                                }
                                val adjLabel = if (languageMode == LanguageMode.BANGLA) "সমন্বয়" else "Adj"
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "$adjLabel: $adjFormatted",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Amounts + Calc Mode actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showOnlyCurrentBalance) {
                        Box(
                            modifier = Modifier.widthIn(min = 85.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatBalance(group.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier.widthIn(min = 75.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = formatBalance(group.effectiveBaseBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = if (isIncluded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                                    textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.widthIn(min = 85.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = formatBalance(group.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        ChangeIndicator(delta = group.delta)
                                    }
                                }
                            }
                        }
                    }

                    if (isCalcMode) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onToggleIncludeStatus?.invoke(group.parentAccount, !isIncluded) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isIncluded) "Exclude" else "Include",
                                tint = if (isIncluded) SolidIncome else SolidExpense,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(
                            onClick = { onRequestAdjustCalculation(group.parentAccount, group.currentBalance) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Adjust",
                                tint = if (isAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubAccountRowItem(
    row: BalanceSheetAccountRow,
    isCalcMode: Boolean,
    accountCalcConfig: AccountCalcConfig,
    displayCurrency: Boolean,
    displayCurrencySymbol: Boolean = true,
    showOnlyCurrentBalance: Boolean,
    languageMode: LanguageMode,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)?,
    onRequestAdjustCalculation: (Account, Double) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val isIncluded = accountCalcConfig.isIncluded(row.account.id)
    val isAdjusted = accountCalcConfig.getAdjustment(row.account.id) != 0.0 || row.adjustmentAmount != 0.0
    val adjustment = if (accountCalcConfig.getAdjustment(row.account.id) != 0.0) {
        accountCalcConfig.getAdjustment(row.account.id)
    } else {
        row.adjustmentAmount
    }
    val defaultPrimaryColor = MaterialTheme.colorScheme.primary
    val subAccColor = remember(row.account.colorHex, defaultPrimaryColor) {
        IconHelper.parseColorHex(row.account.colorHex, defaultPrimaryColor)
    }

    // Sub-Account nested white/surface Card
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAccountClick?.invoke(row.account) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Account Icon + Name Column (with percentage pill: "X%, Y% in total")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Sub-Account Icon (Squircle shaped border, 20dp icon with 0.65dp hairline border)
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(5.5.dp))
                        .background(if (isIncluded) subAccColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 0.65.dp,
                            color = if (isIncluded) subAccColor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(5.5.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconHelper.AppIcon(
                        iconName = row.account.iconName,
                        contentDescription = null,
                        tint = if (isIncluded) subAccColor else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) row.account.nameBn else row.account.nameEn,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Excluded badge
                        if (!isIncluded) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SolidExpense.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excluded",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Dynamic Reverse Account Tag (e.g. From Liability / From Asset)
                        if (row.dynamicTag != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SolidIncome.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = row.dynamicTag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Bottom Line under account name: "18.5% · [5% in total] [Adj: ৳ -94]"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isIncluded) {
                            // 1. Group Percentage (bold colored text) if greater than 0
                            if (row.percentageShare > 0.0001) {
                                val grpPctStr = String.format(java.util.Locale.US, "%.1f%%", row.percentageShare)
                                val localizedGrp = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(grpPctStr) else grpPctStr

                                Text(
                                    text = localizedGrp,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = subAccColor
                                )

                                Text(
                                    text = "·",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            // 2. Percentage of Total (in subtle shady tight padding pill badge)
                            val totPctStr = String.format(java.util.Locale.US, "%.1f%%", row.totalPercentageShare)
                            val localizedTot = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(totPctStr) else totPctStr
                            val totalLabel = if (languageMode == LanguageMode.BANGLA) "মোট" else "in total"

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "$localizedTot $totalLabel",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // 3. Adjustment Pill (if adjusted and included)
                        if (isAdjusted && isIncluded) {
                            val sign = if (adjustment > 0) "+" else ""
                            val adjFormatted = if (displayCurrencySymbol) {
                                "${sign}${LanguageHelper.formatCurrency(adjustment, languageMode)}"
                            } else {
                                "${sign}${String.format("%.2f", adjustment)}"
                            }
                            val adjLabel = if (languageMode == LanguageMode.BANGLA) "সমন্বয়" else "Adj"

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "$adjLabel: $adjFormatted",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Balances + Calc Mode Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (showOnlyCurrentBalance) {
                    Box(
                        modifier = Modifier.widthIn(min = 85.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatBalance(row.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier.widthIn(min = 75.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = formatBalance(row.effectiveBaseBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                fontSize = 12.sp,
                                color = if (isIncluded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                                textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                textAlign = TextAlign.End
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.widthIn(min = 85.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = formatBalance(row.effectiveCurrentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    ChangeIndicator(delta = row.delta)
                                }
                            }
                        }
                    }
                }

                if (isCalcMode) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onToggleIncludeStatus?.invoke(row.account, !isIncluded) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isIncluded) "Exclude" else "Include",
                            tint = if (isIncluded) SolidIncome else SolidExpense,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = { onRequestAdjustCalculation(row.account, row.currentBalance) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust",
                            tint = if (isAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeIndicator(delta: Double) {
    when {
        delta > 0.001 -> {
            Text(
                text = "▲",
                fontSize = 9.sp,
                color = SolidIncome,
                fontWeight = FontWeight.Bold
            )
        }
        delta < -0.001 -> {
            Text(
                text = "▼",
                fontSize = 9.sp,
                color = SolidExpense,
                fontWeight = FontWeight.Bold
            )
        }
        else -> {
            Text(
                text = "—",
                fontSize = 9.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptySectionPlaceholder(message: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = message,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}
