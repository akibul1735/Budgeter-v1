package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CreditCard
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.AppTabHeader
import com.example.ui.components.ExportMenuButton
import com.example.ui.dialogs.AccountCalculationDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
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
    val sortOrder: BalanceSheetSortOrder = BalanceSheetSortOrder.AMOUNT_DESC,
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
                (sortOrder != BalanceSheetSortOrder.DEFAULT && sortOrder != BalanceSheetSortOrder.AMOUNT_DESC) ||
                showHiddenAccounts ||
                showOnlyCurrentBalance ||
                showOnlyAccountsWithoutGroups
}

data class SavedBalanceSheetPreset(
    val id: String,
    val name: String,
    val filterState: BalanceSheetFilterState
)

object BalanceSheetPresetsStorage {
    private const val PREFS_NAME = "bs_filter_presets"
    private const val KEY_PRESETS = "presets_list"

    fun loadPresets(context: Context): List<SavedBalanceSheetPreset> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            val jsonArr = JSONArray(raw)
            val list = mutableListOf<SavedBalanceSheetPreset>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val presetName = obj.optString("preset", BalanceSheetComparisonPreset.END_OF_LAST_MONTH.name)
                val preset = try {
                    BalanceSheetComparisonPreset.valueOf(presetName)
                } catch (e: Exception) {
                    BalanceSheetComparisonPreset.END_OF_LAST_MONTH
                }
                val customBase = if (obj.has("baseDate")) obj.getLong("baseDate") else null
                val customCompare = if (obj.has("compareDate")) obj.getLong("compareDate") else null

                val accIds = mutableSetOf<Long>()
                val accJson = obj.optJSONArray("accountIds")
                if (accJson != null) {
                    for (j in 0 until accJson.length()) {
                        accIds.add(accJson.getLong(j))
                    }
                }

                val statusSet = mutableSetOf<TransactionStatus>()
                val statusJson = obj.optJSONArray("statuses")
                if (statusJson != null) {
                    for (j in 0 until statusJson.length()) {
                        try {
                            statusSet.add(TransactionStatus.valueOf(statusJson.getString(j)))
                        } catch (_: Exception) {}
                    }
                }

                val excludeZero = obj.optBoolean("excludeZero", false)
                val filterNonZero = obj.optBoolean("filterNonZero", false)
                val displayCurr = obj.optBoolean("displayCurr", true)
                val displayCurrSym = obj.optBoolean("displayCurrSym", true)
                val sortOrderName = obj.optString("sortOrder", BalanceSheetSortOrder.DEFAULT.name)
                val sortOrder = try {
                    BalanceSheetSortOrder.valueOf(sortOrderName)
                } catch (e: Exception) {
                    BalanceSheetSortOrder.DEFAULT
                }
                val showHidden = obj.optBoolean("showHidden", false)
                val onlyCurrent = obj.optBoolean("onlyCurrent", false)
                val onlyWithoutGroups = obj.optBoolean("onlyWithoutGroups", false)

                list.add(
                    SavedBalanceSheetPreset(
                        id = id,
                        name = name,
                        filterState = BalanceSheetFilterState(
                            preset = preset,
                            customBaseDateMs = customBase,
                            customCompareDateMs = customCompare,
                            selectedAccountIds = accIds,
                            selectedStatusSet = statusSet,
                            excludeZeroAmounts = excludeZero,
                            filterNonZeroGroups = filterNonZero,
                            displayCurrency = displayCurr,
                            displayCurrencySymbol = displayCurrSym,
                            sortOrder = sortOrder,
                            showHiddenAccounts = showHidden,
                            showOnlyCurrentBalance = onlyCurrent,
                            showOnlyAccountsWithoutGroups = onlyWithoutGroups
                        )
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePresets(context: Context, presets: List<SavedBalanceSheetPreset>) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArr = JSONArray()
        for (item in presets) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("preset", item.filterState.preset.name)
            item.filterState.customBaseDateMs?.let { obj.put("baseDate", it) }
            item.filterState.customCompareDateMs?.let { obj.put("compareDate", it) }

            val accArr = JSONArray()
            item.filterState.selectedAccountIds.forEach { accArr.put(it) }
            obj.put("accountIds", accArr)

            val statusArr = JSONArray()
            item.filterState.selectedStatusSet.forEach { statusArr.put(it.name) }
            obj.put("statuses", statusArr)

            obj.put("excludeZero", item.filterState.excludeZeroAmounts)
            obj.put("filterNonZero", item.filterState.filterNonZeroGroups)
            obj.put("displayCurr", item.filterState.displayCurrency)
            obj.put("displayCurrSym", item.filterState.displayCurrencySymbol)
            obj.put("sortOrder", item.filterState.sortOrder.name)
            obj.put("showHidden", item.filterState.showHiddenAccounts)
            obj.put("onlyCurrent", item.filterState.showOnlyCurrentBalance)
            obj.put("onlyWithoutGroups", item.filterState.showOnlyAccountsWithoutGroups)
            jsonArr.put(obj)
        }
        sp.edit().putString(KEY_PRESETS, jsonArr.toString()).apply()
    }
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
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)? = null,
    onSaveCalculationSetting: ((Account, Boolean, Double) -> Unit)? = null,
    onResetAccountCalculation: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current

    // Active Filter State
    var filterState by remember { mutableStateOf(BalanceSheetFilterState()) }
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
        val (base, _) = BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.THIS_MONTH)
        mutableStateOf(base)
    }
    var compareDateMs by remember {
        val (_, compare) = BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.THIS_MONTH)
        mutableStateOf(compare)
    }

    // Modal Filter Dialog & Calculation Mode
    var showFilterDialog by remember { mutableStateOf(false) }
    var isCalcMode by remember { mutableStateOf(false) }
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }
    var isDualDateFlow by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeTabMode by remember { mutableStateOf("ASSETS") } // "ASSETS" or "LIABILITIES"
    var isSpeedDialExpanded by remember { mutableStateOf(false) }

    // Date Pickers for Custom Mode
    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }

    // Collapsed/Expanded parent account rows
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // Calculate balance sheet data
    val balanceSheetData = remember(
        accounts,
        transactions,
        baseDateMs,
        compareDateMs,
        filterState,
        searchQuery,
        accountCalcConfig,
        languageMode
    ) {
        BalanceSheetHelper.calculateBalanceSheet(
            accounts = accounts,
            transactions = transactions,
            baseDateEpochMs = baseDateMs,
            compareDateEpochMs = compareDateMs,
            preset = filterState.preset,
            selectedAccountIds = filterState.selectedAccountIds,
            selectedStatusSet = filterState.selectedStatusSet,
            activeOnly = !filterState.showHiddenAccounts,
            showHiddenAccounts = filterState.showHiddenAccounts,
            excludeZeroAmounts = filterState.excludeZeroAmounts,
            filterNonZeroGroups = filterState.filterNonZeroGroups,
            sortOrder = filterState.sortOrder,
            searchQuery = searchQuery,
            accountCalcConfig = accountCalcConfig,
            showOnlyAccountsWithoutGroups = filterState.showOnlyAccountsWithoutGroups,
            languageMode = languageMode
        )
    }

    val listState = rememberLazyListState()
    val isMainSummaryVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

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
                isFilterActive = filterState.isFilterActive,
                onFilterClick = { showFilterDialog = true },
                showTimelineButton = true,
                onTimelineClick = { showTimelineScreen = true },
                onOpenDrawer = onOpenDrawer,
                actions = {
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            val isComparison = !filterState.showOnlyCurrentBalance && balanceSheetData.baseDateLabel.isNotBlank()
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
                    preset = filterState.preset,
                    baseDateLabel = balanceSheetData.baseDateLabel,
                    compareDateLabel = balanceSheetData.compareDateLabel,
                    showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
                    languageMode = languageMode,
                    isCalcMode = isCalcMode,
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

            // Mini height summary card (Only visible when main summary card is scrolled off screen)
            AnimatedVisibility(
                visible = !isMainSummaryVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                MiniNetWorthSummaryCard(
                    data = balanceSheetData,
                    displayCurrency = filterState.displayCurrency,
                    displayCurrencySymbol = filterState.displayCurrencySymbol,
                    languageMode = languageMode
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp)
            ) {
                // 1. Top Net Worth Card (Total Assets | Total Net Worth | Total Liabilities in one row)
                item(key = "main_net_worth_summary") {
                    NetWorthSummaryCard(
                        data = balanceSheetData,
                        displayCurrency = filterState.displayCurrency,
                        displayCurrencySymbol = filterState.displayCurrencySymbol,
                        showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
                        languageMode = languageMode
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Section based on activeTabMode ("ASSETS" or "LIABILITIES")
                if (activeTabMode == "ASSETS") {
                    item {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "সম্পদ (ASSETS)" else "ASSETS",
                            baseAmount = balanceSheetData.totalAssetsBase,
                            currentAmount = balanceSheetData.totalAssetsCurrent,
                            displayCurrency = filterState.displayCurrency,
                            displayCurrencySymbol = filterState.displayCurrencySymbol,
                            showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
                            headerColor = MaterialTheme.colorScheme.primary,
                            languageMode = languageMode
                        )
                    }

                    if (balanceSheetData.assetGroups.isEmpty()) {
                        item {
                            EmptySectionPlaceholder(
                                message = if (languageMode == LanguageMode.BANGLA) "কোন সম্পদ অ্যাকাউন্ট পাওয়া যায়নি" else "No asset accounts found"
                            )
                        }
                    } else {
                        items(balanceSheetData.assetGroups, key = { it.parentAccount.id }) { group ->
                            val isExpanded = expandedMap[group.parentAccount.id] ?: true
                            BalanceSheetGroupItem(
                                group = group,
                                isExpanded = isExpanded,
                                isCalcMode = isCalcMode,
                                accountCalcConfig = accountCalcConfig,
                                displayCurrency = filterState.displayCurrency,
                                displayCurrencySymbol = filterState.displayCurrencySymbol,
                                showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    item {
                        SectionHeader(
                            title = if (languageMode == LanguageMode.BANGLA) "দায় (LIABILITIES)" else "LIABILITIES",
                            baseAmount = balanceSheetData.totalLiabilitiesBase,
                            currentAmount = balanceSheetData.totalLiabilitiesCurrent,
                            displayCurrency = filterState.displayCurrency,
                            displayCurrencySymbol = filterState.displayCurrencySymbol,
                            showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
                            headerColor = SolidExpense,
                            languageMode = languageMode
                        )
                    }

                    if (balanceSheetData.liabilityGroups.isEmpty()) {
                        item {
                            EmptySectionPlaceholder(
                                message = if (languageMode == LanguageMode.BANGLA) "কোন দায় অ্যাকাউন্ট পাওয়া যায়নি" else "No liability accounts found"
                            )
                        }
                    } else {
                        items(balanceSheetData.liabilityGroups, key = { it.parentAccount.id }) { group ->
                            val isExpanded = expandedMap[group.parentAccount.id] ?: true
                            BalanceSheetGroupItem(
                                group = group,
                                isExpanded = isExpanded,
                                isCalcMode = isCalcMode,
                                accountCalcConfig = accountCalcConfig,
                                displayCurrency = filterState.displayCurrency,
                                displayCurrencySymbol = filterState.displayCurrencySymbol,
                                showOnlyCurrentBalance = filterState.showOnlyCurrentBalance,
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
                            Spacer(modifier = Modifier.height(8.dp))
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
                        // Option 1: Add Account
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
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

                        // Option 2: Add Transaction
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
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

                // Main Floating Action Button
                Surface(
                    onClick = { isSpeedDialExpanded = !isSpeedDialExpanded },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Actions",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Segmented Toggle: [ Assets (সম্পদ) | Liabilities (দায়) ]
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
                        // Assets Button
                        val isAssets = activeTabMode == "ASSETS"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAssets) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { activeTabMode = "ASSETS" }
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
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets",
                                    fontSize = 13.sp,
                                    fontWeight = if (isAssets) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAssets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Liabilities Button
                        val isLiabilities = activeTabMode == "LIABILITIES"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiabilities) SolidExpense.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { activeTabMode = "LIABILITIES" }
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
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities",
                                    fontSize = 13.sp,
                                    fontWeight = if (isLiabilities) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLiabilities) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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

    // Material Style Filter Dialog
    if (showFilterDialog) {
        MaterialBalanceSheetFilterDialog(
            currentState = filterState,
            accounts = accounts,
            languageMode = languageMode,
            baseDateMs = baseDateMs,
            compareDateMs = compareDateMs,
            onApply = { newState ->
                filterState = newState
                if (newState.preset == BalanceSheetComparisonPreset.CUSTOM &&
                    newState.customBaseDateMs != null && newState.customCompareDateMs != null) {
                    baseDateMs = newState.customBaseDateMs
                    compareDateMs = newState.customCompareDateMs
                } else {
                    val (b, c) = BalanceSheetHelper.getPresetDateRanges(newState.preset)
                    baseDateMs = b
                    compareDateMs = c
                }
                showFilterDialog = false
            },
            onSelectCustomDates = {
                showFilterDialog = false
                isDualDateFlow = true
                showBaseDatePicker = true
            },
            onSelectBaseDate = {
                showFilterDialog = false
                isDualDateFlow = false
                showBaseDatePicker = true
            },
            onSelectCompareDate = {
                showFilterDialog = false
                isDualDateFlow = false
                showCompareDatePicker = true
            },
            onDismiss = { showFilterDialog = false }
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
                            filterState = filterState.copy(
                                preset = BalanceSheetComparisonPreset.CUSTOM,
                                customBaseDateMs = it,
                                customCompareDateMs = compareDateMs
                            )
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
                        filterState = filterState.copy(
                            preset = BalanceSheetComparisonPreset.CUSTOM,
                            customBaseDateMs = baseDateMs,
                            customCompareDateMs = it
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBalanceSheetFilterDialog(
    currentState: BalanceSheetFilterState,
    accounts: List<Account>,
    languageMode: LanguageMode,
    baseDateMs: Long,
    compareDateMs: Long,
    onApply: (BalanceSheetFilterState) -> Unit,
    onSelectCustomDates: () -> Unit,
    onSelectBaseDate: () -> Unit,
    onSelectCompareDate: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var tempPreset by remember { mutableStateOf(currentState.preset) }
    var tempAccountIds by remember { mutableStateOf(currentState.selectedAccountIds) }
    var tempStatuses by remember { mutableStateOf(currentState.selectedStatusSet) }
    var tempExcludeZero by remember { mutableStateOf(currentState.excludeZeroAmounts) }
    var tempFilterNonZeroGroups by remember { mutableStateOf(currentState.filterNonZeroGroups) }
    var tempDisplayCurrency by remember { mutableStateOf(currentState.displayCurrency) }
    var tempDisplayCurrencySymbol by remember { mutableStateOf(currentState.displayCurrencySymbol) }
    var tempSortOrder by remember { mutableStateOf(currentState.sortOrder) }
    var tempShowHidden by remember { mutableStateOf(currentState.showHiddenAccounts) }
    var tempShowOnlyCurrent by remember { mutableStateOf(currentState.showOnlyCurrentBalance) }
    var tempShowOnlyWithoutGroups by remember { mutableStateOf(currentState.showOnlyAccountsWithoutGroups) }

    // Sub-dialog states
    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var showStatusPickerDialog by remember { mutableStateOf(false) }
    var showSortOrderPickerDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showOpenPresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var isDateRangeMenuExpanded by remember { mutableStateOf(false) }

    val savedPresets = remember { mutableStateListOf<SavedBalanceSheetPreset>().apply { addAll(BalanceSheetPresetsStorage.loadPresets(context)) } }

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স শিট ফিল্টার" else "Balance Sheet Filter",
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
                                    tempPreset = BalanceSheetComparisonPreset.END_OF_LAST_MONTH
                                    tempAccountIds = emptySet()
                                    tempStatuses = emptySet()
                                    tempExcludeZero = true
                                    tempFilterNonZeroGroups = false
                                    tempDisplayCurrency = true
                                    tempDisplayCurrencySymbol = true
                                    tempSortOrder = BalanceSheetSortOrder.AMOUNT_DESC
                                    tempShowHidden = false
                                    tempShowOnlyCurrent = false
                                    tempShowOnlyWithoutGroups = false
                                    Toast.makeText(context, if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default", Toast.LENGTH_SHORT).show()
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

                            // 2. Save (Custom filter save for reuse)
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                onClick = {
                                    presetNameInput = ""
                                    showSavePresetDialog = true
                                },
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

                            // 3. Open (Load saved presets)
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                onClick = {
                                    showOpenPresetDialog = true
                                },
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        val presetLabel = if (languageMode == LanguageMode.BANGLA) tempPreset.titleBn else tempPreset.titleEn
                                        Text(
                                            text = presetLabel,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
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
                                BalanceSheetComparisonPreset.values().forEach { preset ->
                                    val isSelected = tempPreset == preset
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn,
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
                                            tempPreset = preset
                                            if (preset == BalanceSheetComparisonPreset.CUSTOM) {
                                                onSelectCustomDates()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // If CUSTOM preset selected, show both dates comparison configuration
                        if (tempPreset == BalanceSheetComparisonPreset.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক দুটি তারিখ:" else "Select Two Comparison Dates:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1. Base Date Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectBaseDate() }
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "১. বেস তারিখ (শুরুর সময়কাল)" else "1. Base Date (Past Baseline)",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = DateUtils.formatDate(baseDateMs, languageMode),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Change Base Date",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 2. Compare Date Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectCompareDate() }
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "২. তুলনামূলক বর্তমান তারিখ" else "2. Compare Date (Target)",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = DateUtils.formatDate(compareDateMs, languageMode),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Change Compare Date",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Account Dropdown
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                    val label = if (tempAccountIds.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "সব অ্যাকাউন্ট (All Accounts)" else "All Accounts"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "${tempAccountIds.size} টি অ্যাকাউন্ট নির্বাচিত" else "${tempAccountIds.size} Accounts Selected"
                                    }
                                    Text(
                                        text = label,
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

                    // 3. Status Dropdown
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                    val label = if (tempStatuses.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "(কোন ফিল্টার নেই)" else "(No Filter)"
                                    } else {
                                        tempStatuses.joinToString(", ") { if (languageMode == LanguageMode.BANGLA) it.titleBn else it.titleEn }
                                    }
                                    Text(
                                        text = label,
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

                    // 4. Sort Order Row
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সর্টিং ক্রম" else "Sort Order",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { showSortOrderPickerDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val sortLabel = when (tempSortOrder) {
                                        BalanceSheetSortOrder.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "ডিফল্ট ক্রম" else "Default Order"
                                        BalanceSheetSortOrder.AMOUNT_DESC -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ: বেশি থেকে কম" else "Amount: High to Low"
                                        BalanceSheetSortOrder.AMOUNT_ASC -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ: কম থেকে বেশি" else "Amount: Low to High"
                                        BalanceSheetSortOrder.NAME_ASC -> if (languageMode == LanguageMode.BANGLA) "নাম: ক থেকে হ / A to Z" else "Name: A to Z"
                                    }
                                    Text(
                                        text = sortLabel,
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
                                            text = if (languageMode == LanguageMode.BANGLA) "নির্বাচন" else "Select",
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

                    // 5. Toggle Rows
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude zero amounts",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = tempExcludeZero,
                                    onCheckedChange = { tempExcludeZero = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 2: Filter non-zero groups
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র ব্যালেন্স থাকা গ্রুপ দেখান" else "Only show non-zero groups",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = tempFilterNonZeroGroups,
                                    onCheckedChange = { tempFilterNonZeroGroups = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 3: Display currency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "কারেন্সি প্রদর্শন করুন" else "Display currency",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = tempDisplayCurrency,
                                    onCheckedChange = { tempDisplayCurrency = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 4: Display currency symbol
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "কারেন্সি প্রতীক (৳) প্রদর্শন" else "Display currency symbol (৳)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = tempDisplayCurrencySymbol,
                                    onCheckedChange = { tempDisplayCurrencySymbol = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 5: Show hidden accounts
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "লুকানো অ্যাকাউন্ট দেখান" else "Show hidden accounts",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Switch(
                                    checked = tempShowHidden,
                                    onCheckedChange = { tempShowHidden = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 6: Show only current balance
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বর্তমান ব্যালেন্স দেখান" else "Show only current balance",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনা বন্ধ করে একক ব্যালেন্স প্রদর্শন করবে" else "Turn off comparison & show current snapshot",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                                Switch(
                                    checked = tempShowOnlyCurrent,
                                    onCheckedChange = { tempShowOnlyCurrent = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 7: Show only accounts without groups
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "গ্রুপ ছাড়া শুধুমাত্র অ্যাকাউন্ট দেখান" else "Show only accounts without groups",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "গ্রুপ বাদ দিয়ে সরাসরি প্রতিটি অ্যাকাউন্ট প্রদর্শন করবে" else "List individual accounts directly without groups",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                                Switch(
                                    checked = tempShowOnlyWithoutGroups,
                                    onCheckedChange = { tempShowOnlyWithoutGroups = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val newState = BalanceSheetFilterState(
                                preset = tempPreset,
                                customBaseDateMs = currentState.customBaseDateMs,
                                customCompareDateMs = currentState.customCompareDateMs,
                                selectedAccountIds = tempAccountIds,
                                selectedStatusSet = tempStatuses,
                                excludeZeroAmounts = tempExcludeZero,
                                filterNonZeroGroups = tempFilterNonZeroGroups,
                                displayCurrency = tempDisplayCurrency,
                                displayCurrencySymbol = tempDisplayCurrencySymbol,
                                sortOrder = tempSortOrder,
                                showHiddenAccounts = tempShowHidden,
                                showOnlyCurrentBalance = tempShowOnlyCurrent,
                                showOnlyAccountsWithoutGroups = tempShowOnlyWithoutGroups
                            )
                            onApply(newState)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
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

    // Sort Order Selection Dialog
    if (showSortOrderPickerDialog) {
        AlertDialog(
            onDismissRequest = { showSortOrderPickerDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সর্টিং ক্রম নির্বাচন করুন" else "Select Sort Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    BalanceSheetSortOrder.values().forEach { order ->
                        val isSelected = tempSortOrder == order
                        val title = when (order) {
                            BalanceSheetSortOrder.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "ডিফল্ট ক্রম (তৈরি করার ক্রম)" else "Default Order"
                            BalanceSheetSortOrder.AMOUNT_DESC -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ: বেশি থেকে কম" else "Amount: High to Low"
                            BalanceSheetSortOrder.AMOUNT_ASC -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ: কম থেকে বেশি" else "Amount: Low to High"
                            BalanceSheetSortOrder.NAME_ASC -> if (languageMode == LanguageMode.BANGLA) "নাম: ক থেকে হ / A to Z" else "Name: A to Z"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    tempSortOrder = order
                                    showSortOrderPickerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    tempSortOrder = order
                                    showSortOrderPickerDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortOrderPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Account Multi-Select Picker Dialog
    if (showAccountPickerDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val tempSelected = remember { mutableStateListOf<Long>().apply { addAll(tempAccountIds) } }

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

                    Spacer(modifier = Modifier.height(4.dp))

                    val filtered = accounts.filter {
                        searchQuery.isBlank() || it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(filtered, key = { it.id }) { acc ->
                            val isChecked = tempSelected.contains(acc.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isChecked) tempSelected.remove(acc.id) else tempSelected.add(acc.id)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) tempSelected.add(acc.id) else tempSelected.remove(acc.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = IconHelper.getIconByName(acc.iconName),
                                    contentDescription = null,
                                    tint = if (acc.type == AccountType.ASSET) MaterialTheme.colorScheme.primary else SolidExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) acc.nameBn else acc.nameEn,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (acc.parentId != null) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট" else "Sub-account",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    tempAccountIds = tempSelected.toSet()
                    showAccountPickerDialog = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Status Multi-Select Picker Dialog
    if (showStatusPickerDialog) {
        val tempStatusSelected = remember { mutableStateListOf<TransactionStatus>().apply { addAll(tempStatuses) } }
        val allStatuses = listOf(
            TransactionStatus.CLEARED,
            TransactionStatus.NONE,
            TransactionStatus.RECONCILED,
            TransactionStatus.VOID
        )

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
                            tempStatusSelected.clear()
                            tempStatusSelected.addAll(allStatuses)
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            tempStatusSelected.clear()
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear All", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    allStatuses.forEach { status ->
                        val isChecked = tempStatusSelected.contains(status)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) tempStatusSelected.remove(status) else tempStatusSelected.add(status)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) tempStatusSelected.add(status) else tempStatusSelected.remove(status)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) status.titleBn else status.titleEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    tempStatuses = tempStatusSelected.toSet()
                    showStatusPickerDialog = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Save Custom Filter Dialog Prompt
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রিসেট সংরক্ষণ করুন" else "Save Custom Filter Preset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ভবিষ্যতে দ্রুত ব্যবহারের জন্য এই ফিল্টারের একটি নাম দিন:" else "Give this filter configuration a name for quick reuse:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "প্রিসেটের নাম" else "Preset Name") },
                        placeholder = { Text("e.g., Active Assets Only") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = presetNameInput.trim().ifEmpty { "Filter Preset ${savedPresets.size + 1}" }
                        val newPreset = SavedBalanceSheetPreset(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            filterState = BalanceSheetFilterState(
                                preset = tempPreset,
                                customBaseDateMs = currentState.customBaseDateMs,
                                customCompareDateMs = currentState.customCompareDateMs,
                                selectedAccountIds = tempAccountIds,
                                selectedStatusSet = tempStatuses,
                                excludeZeroAmounts = tempExcludeZero,
                                filterNonZeroGroups = tempFilterNonZeroGroups,
                                displayCurrency = tempDisplayCurrency,
                                displayCurrencySymbol = tempDisplayCurrencySymbol,
                                sortOrder = tempSortOrder,
                                showHiddenAccounts = tempShowHidden,
                                showOnlyCurrentBalance = tempShowOnlyCurrent
                            )
                        )
                        savedPresets.add(newPreset)
                        BalanceSheetPresetsStorage.savePresets(context, savedPresets)
                        showSavePresetDialog = false
                        Toast.makeText(context, if (languageMode == LanguageMode.BANGLA) "প্রিসেট সংরক্ষিত হয়েছে" else "Filter preset saved successfully!", Toast.LENGTH_SHORT).show()
                    }
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

    // Open / Load Saved Filter Presets Dialog
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
                    if (savedPresets.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোন সংরক্ষিত ফিল্টার নেই। নতুন ফিল্টার সংরক্ষণ করতে 'সংরক্ষণ' আইকন ব্যবহার করুন।" else "No saved filters found. Tap the Save icon to save your current filter settings for quick reuse.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            items(savedPresets, key = { it.id }) { presetItem ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            val fs = presetItem.filterState
                                            tempPreset = fs.preset
                                            tempAccountIds = fs.selectedAccountIds
                                            tempStatuses = fs.selectedStatusSet
                                            tempExcludeZero = fs.excludeZeroAmounts
                                            tempFilterNonZeroGroups = fs.filterNonZeroGroups
                                            tempDisplayCurrency = fs.displayCurrency
                                            tempDisplayCurrencySymbol = fs.displayCurrencySymbol
                                            tempSortOrder = fs.sortOrder
                                            tempShowHidden = fs.showHiddenAccounts
                                            tempShowOnlyCurrent = fs.showOnlyCurrentBalance
                                            tempShowOnlyWithoutGroups = fs.showOnlyAccountsWithoutGroups
                                            showOpenPresetDialog = false
                                            Toast.makeText(context, if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রয়োগ করা হয়েছে" else "Loaded preset: ${presetItem.name}", Toast.LENGTH_SHORT).show()
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
                                            Text(
                                                text = presetItem.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val summary = if (languageMode == LanguageMode.BANGLA) presetItem.filterState.preset.titleBn else presetItem.filterState.preset.titleEn
                                            Text(
                                                text = summary,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                savedPresets.remove(presetItem)
                                                BalanceSheetPresetsStorage.savePresets(context, savedPresets)
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
            fontSize = 13.sp,
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
                    fontSize = 13.sp,
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
                        fontSize = 12.sp,
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
                            fontSize = 12.sp,
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
    val isAdjusted = accountCalcConfig.getAdjustment(group.parentAccount.id) != 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        // Parent Account Row
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/collapse icon
                if (group.subAccounts.isNotEmpty()) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                } else {
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Account Name with Group Icon Indicator
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onAccountClick?.invoke(group.parentAccount) }
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(group.parentAccount.iconName),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
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
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // % Share badge
                    if (group.percentageShare > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${group.percentageShare.toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Comparative or Single Amounts
                if (showOnlyCurrentBalance) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier.widthIn(min = 85.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = formatBalance(group.currentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                textAlign = TextAlign.End
                            )
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
                                text = formatBalance(group.baseBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
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
                                    text = formatBalance(group.currentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                ChangeIndicator(delta = group.delta)
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

        // Sub Accounts Nested Card
        AnimatedVisibility(
            visible = isExpanded && group.subAccounts.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 2.dp, bottom = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    group.subAccounts.forEachIndexed { index, subRow ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
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
    val isAdjusted = accountCalcConfig.getAdjustment(row.account.id) != 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onAccountClick?.invoke(row.account) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Account Name & % badge
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) row.account.nameBn else row.account.nameEn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                maxLines = 2,
                lineHeight = 15.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            // Excluded badge
            if (!isIncluded) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = SolidExpense.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excluded",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            if (row.percentageShare > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${row.percentageShare.toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amounts
        if (showOnlyCurrentBalance) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier.widthIn(min = 85.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = formatBalance(row.currentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                        textAlign = TextAlign.End
                    )
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
                        text = formatBalance(row.baseBalance, displayCurrency, languageMode, displayCurrencySymbol),
                        fontSize = 11.sp,
                        color = if (isIncluded) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                        textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
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
                            text = formatBalance(row.currentBalance, displayCurrency, languageMode, displayCurrencySymbol),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        ChangeIndicator(delta = row.delta)
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
