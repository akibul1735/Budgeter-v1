package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlin.math.abs

private data class AccountGroupSuggestion(
    val group: com.example.data.model.Account,
    val accounts: List<AccountWithBalance>
)

@Composable
fun FavoriteAccountsSelectionDialog(
    allAccounts: List<AccountWithBalance>,
    initialSelectedIds: List<Long>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    accountActivityTimestamps: Map<Long, Long> = emptyMap(),
    deselectedAccountTimestamps: Map<Long, Long> = emptyMap(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (List<Long>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var orderedSelectedIds by remember(initialSelectedIds) { mutableStateOf(initialSelectedIds.distinct()) }
    val selectedIds by remember { derivedStateOf { orderedSelectedIds.toSet() } }
    var manualViewMode by remember { mutableIntStateOf(if (initialSelectedIds.isNotEmpty()) 0 else 1) }

    // Flatten all active individual accounts (both excluded and included from calculation)
    val flatIndividualAccounts = remember(allAccounts) {
        val list = mutableListOf<AccountWithBalance>()
        for (item in allAccounts) {
            if (item.subAccounts.isNotEmpty()) {
                list.addAll(item.subAccounts.filter { it.account.isActive })
            } else if (item.account.isActive) {
                list.add(item)
            }
        }
        list
    }

    // Map account id to parent group account
    val accountParentMap = remember(allAccounts) {
        val map = mutableMapOf<Long, com.example.data.model.Account>()
        for (group in allAccounts) {
            for (sub in group.subAccounts) {
                map[sub.account.id] = group.account
            }
        }
        map
    }

    // Group accounts for manual selection suggestions: Group -> Accounts
    val groupedAccounts = remember(allAccounts, searchQuery) {
        val list = mutableListOf<AccountGroupSuggestion>()
        for (groupItem in allAccounts) {
            val activeSubs = groupItem.subAccounts.filter { it.account.isActive }
            if (activeSubs.isNotEmpty()) {
                val matchingSubs = if (searchQuery.isBlank()) {
                    activeSubs
                } else {
                    val groupMatches = groupItem.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                            groupItem.account.nameBn.contains(searchQuery, ignoreCase = true)
                    if (groupMatches) {
                        activeSubs
                    } else {
                        activeSubs.filter {
                            it.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                                    it.account.nameBn.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
                if (matchingSubs.isNotEmpty()) {
                    list.add(AccountGroupSuggestion(group = groupItem.account, accounts = matchingSubs))
                }
            } else if (groupItem.account.isActive) {
                val matches = searchQuery.isBlank() ||
                        groupItem.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                        groupItem.account.nameBn.contains(searchQuery, ignoreCase = true)
                if (matches) {
                    list.add(AccountGroupSuggestion(group = groupItem.account, accounts = listOf(groupItem)))
                }
            }
        }
        list
    }

    val totalVisibleAccounts = remember(groupedAccounts) {
        groupedAccounts.sumOf { it.accounts.size }
    }

    // Compute Auto-Added accounts dynamically
    // Rule:
    // 1. Excluded accounts: If an account is excluded from the Accounts tab, do not auto-add.
    // 2. If the calculated balance remains positive or negative and it has recent activity, it appears.
    // 3. Keep it in the Favorite Account Card while it has a non-zero balance.
    val autoAddedAccounts = remember(flatIndividualAccounts, selectedIds, accountCalcConfig, accountActivityTimestamps, deselectedAccountTimestamps) {
        flatIndividualAccounts.filter { item ->
            val id = item.account.id
            val isExcluded = !accountCalcConfig.isIncluded(id)
            if (isExcluded || id in selectedIds) {
                false
            } else {
                val effectiveBal = accountCalcConfig.getEffectiveBalance(id, item.currentBalance)
                val isNonZero = abs(effectiveBal) > 0.0001
                val actTs = accountActivityTimestamps[id] ?: 0L
                val deselTs = deselectedAccountTimestamps[id] ?: 0L
                val hasRecentActivity = actTs > 0L && actTs >= deselTs
                isNonZero && hasRecentActivity
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .testTag("favorite_accounts_settings_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(34.dp)
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = LanguageHelper.getString("favorite_account_card_settings", languageMode),
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedIds.size} ${LanguageHelper.getString("manually_added", languageMode)} • ${autoAddedAccounts.size} ${LanguageHelper.getString("auto_added", languageMode)}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Two Tabs: Manually Added vs Auto Added
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    // Tab 0: Manually Added
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("manually_added", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                                if (selectedIds.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ) {
                                        Text(
                                            text = "${selectedIds.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Tab 1: Auto Added
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Color(0xFF3B82F6) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("auto_added", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                                if (autoAddedAccounts.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedTab == 1) Color(0xFF2563EB) else MaterialTheme.colorScheme.outlineVariant
                                    ) {
                                        Text(
                                            text = "${autoAddedAccounts.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                if (selectedTab == 0) {
                    // ==========================================
                    // SECTION 1: MANUALLY ADDED ACCOUNTS
                    // ==========================================
                    Column(modifier = Modifier.weight(1f)) {
                        // Sub-navigation: Drag & Reorder vs Select Accounts
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Mode 0: Drag & Reorder
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (manualViewMode == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { manualViewMode = 0 },
                                shadowElevation = if (manualViewMode == 0) 1.5.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 7.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = null,
                                        tint = if (manualViewMode == 0) SolidPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${LanguageHelper.getString("drag_to_reorder", languageMode)} (${orderedSelectedIds.size})",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (manualViewMode == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (manualViewMode == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Mode 1: Select Accounts
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (manualViewMode == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { manualViewMode = 1 },
                                shadowElevation = if (manualViewMode == 1) 1.5.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 7.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = null,
                                        tint = if (manualViewMode == 1) SolidPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = LanguageHelper.getString("browse_select", languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (manualViewMode == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (manualViewMode == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (manualViewMode == 0) {
                            FavoriteAccountsReorderSection(
                                orderedSelectedIds = orderedSelectedIds,
                                flatIndividualAccounts = flatIndividualAccounts,
                                accountParentMap = accountParentMap,
                                accountCalcConfig = accountCalcConfig,
                                languageMode = languageMode,
                                onReorder = { orderedSelectedIds = it },
                                onRemove = { idToRemove -> orderedSelectedIds = orderedSelectedIds - idToRemove },
                                onSwitchToBrowse = { manualViewMode = 1 },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            FavoriteAccountsBrowseSection(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                groupedAccounts = groupedAccounts,
                                flatIndividualAccounts = flatIndividualAccounts,
                                totalVisibleAccounts = totalVisibleAccounts,
                                orderedSelectedIds = orderedSelectedIds,
                                accountCalcConfig = accountCalcConfig,
                                accountActivityTimestamps = accountActivityTimestamps,
                                deselectedAccountTimestamps = deselectedAccountTimestamps,
                                languageMode = languageMode,
                                onToggleAccount = { id ->
                                    orderedSelectedIds = if (id in orderedSelectedIds) {
                                        orderedSelectedIds - id
                                    } else {
                                        orderedSelectedIds + id
                                    }
                                },
                                onToggleGroup = { groupIds, isAllSelected ->
                                    orderedSelectedIds = if (isAllSelected) {
                                        orderedSelectedIds.filter { it !in groupIds }
                                    } else {
                                        val toAdd = groupIds.filter { it !in orderedSelectedIds }
                                        orderedSelectedIds + toAdd
                                    }
                                },
                                onSelectAll = {
                                    val allIds = flatIndividualAccounts.map { it.account.id }
                                    val toAdd = allIds.filter { it !in orderedSelectedIds }
                                    orderedSelectedIds = orderedSelectedIds + toAdd
                                },
                                onClearAll = {
                                    orderedSelectedIds = emptyList()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // SECTION 2: AUTO ADDED ACCOUNTS
                    // ==========================================
                    Column(modifier = Modifier.weight(1f)) {
                        // Explanatory Logic Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageHelper.getString("auto_add_rules_title", languageMode),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• ${LanguageHelper.getString("auto_add_rule_activity", languageMode)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${LanguageHelper.getString("auto_add_rule_balance", languageMode)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${LanguageHelper.getString("auto_add_rule_zero", languageMode)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${LanguageHelper.getString("auto_add_rule_excluded", languageMode)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• ${LanguageHelper.getString("auto_add_rule_independent", languageMode)}",
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${LanguageHelper.getString("auto_added", languageMode)} (${autoAddedAccounts.size})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        if (autoAddedAccounts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = LanguageHelper.getString("no_auto_accounts_hint", languageMode),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        lineHeight = 16.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(autoAddedAccounts, key = { it.account.id }) { item ->
                                    val effectiveBal = accountCalcConfig.getEffectiveBalance(item.account.id, item.currentBalance)
                                    val balanceColor = when {
                                        effectiveBal > 0 -> Color(0xFF10B981)
                                        effectiveBal < 0 -> Color(0xFFEF4444)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    val parentGroup = accountParentMap[item.account.id]
                                    val groupLabel = parentGroup?.localizedName(languageMode)

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    IconHelper.AppIcon(
                                                        iconName = item.account.iconName,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2563EB),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                                    if (!groupLabel.isNullOrBlank()) {
                                                        Text(
                                                            text = groupLabel,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = if (!groupLabel.isNullOrBlank()) "   ${item.account.localizedName(languageMode)}" else item.account.localizedName(languageMode),
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Bolt,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF3B82F6),
                                                                    modifier = Modifier.size(10.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(2.dp))
                                                                Text(
                                                                    text = LanguageHelper.getString("auto_added", languageMode),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = Color(0xFF2563EB)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = item.account.type.name,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = LanguageHelper.formatCurrency(effectiveBal, languageMode),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = balanceColor
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Quick action to promote to manual favorite
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            if (item.account.id !in orderedSelectedIds) {
                                                                orderedSelectedIds = orderedSelectedIds + item.account.id
                                                            }
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(
                                                            text = LanguageHelper.getString("add_to_manual", languageMode),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.primary
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
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSave(orderedSelectedIds.distinct()) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageHelper.getString("save", languageMode))
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAccountsReorderSection(
    orderedSelectedIds: List<Long>,
    flatIndividualAccounts: List<AccountWithBalance>,
    accountParentMap: Map<Long, com.example.data.model.Account>,
    accountCalcConfig: AccountCalcConfig,
    languageMode: LanguageMode,
    onReorder: (List<Long>) -> Unit,
    onRemove: (Long) -> Unit,
    onSwitchToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (orderedSelectedIds.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = LanguageHelper.getString("no_manual_favorites_hint", languageMode),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onSwitchToBrowse,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageHelper.getString("browse_select", languageMode), fontSize = 12.5.sp)
                }
            }
        }
    } else {
        val accountMap = remember(flatIndividualAccounts) {
            flatIndividualAccounts.associateBy { it.account.id }
        }
        val currentOrderedIds by rememberUpdatedState(orderedSelectedIds)
        val currentOnReorder by rememberUpdatedState(onReorder)
        var draggingId by remember { mutableStateOf<Long?>(null) }
        var dragOffsetY by remember { mutableFloatStateOf(0f) }
        val density = LocalDensity.current
        val itemHeightPx = with(density) { 68.dp.toPx() }

        Column(modifier = modifier) {
            // Reorder helper header & Sort A-Z button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("drag_reorder_hint", languageMode),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val sorted = orderedSelectedIds.sortedBy {
                                accountMap[it]?.account?.localizedName(languageMode)?.lowercase() ?: ""
                            }
                            onReorder(sorted)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SortByAlpha,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = LanguageHelper.getString("sort_alphabetically", languageMode),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(orderedSelectedIds, key = { _, id -> id }) { index, accountId ->
                    val item = accountMap[accountId]
                    if (item != null) {
                        val isIncludedInCalc = accountCalcConfig.isIncluded(item.account.id)
                        val effectiveBal = if (isIncludedInCalc) {
                            accountCalcConfig.getEffectiveBalance(item.account.id, item.currentBalance)
                        } else {
                            item.currentBalance
                        }
                        val isBeingDragged = draggingId == accountId

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBeingDragged) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isBeingDragged) 1.5.dp else 1.dp,
                                color = if (isBeingDragged) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(if (isBeingDragged) 10f else 1f)
                                .graphicsLayer {
                                    if (isBeingDragged) {
                                        translationY = dragOffsetY
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                        shadowElevation = 8.dp.toPx()
                                    } else {
                                        translationY = 0f
                                        scaleX = 1f
                                        scaleY = 1f
                                        shadowElevation = 0f
                                    }
                                }
                                .pointerInput(accountId) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingId = accountId
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                            val list = currentOrderedIds
                                            val currentIdx = list.indexOf(accountId)
                                            if (currentIdx != -1) {
                                                val swapThreshold = itemHeightPx * 0.45f
                                                if (dragOffsetY > swapThreshold && currentIdx < list.size - 1) {
                                                    val targetIdx = currentIdx + 1
                                                    val mutable = list.toMutableList()
                                                    val moved = mutable.removeAt(currentIdx)
                                                    mutable.add(targetIdx, moved)
                                                    currentOnReorder(mutable)
                                                    dragOffsetY -= itemHeightPx
                                                } else if (dragOffsetY < -swapThreshold && currentIdx > 0) {
                                                    val targetIdx = currentIdx - 1
                                                    val mutable = list.toMutableList()
                                                    val moved = mutable.removeAt(currentIdx)
                                                    mutable.add(targetIdx, moved)
                                                    currentOnReorder(mutable)
                                                    dragOffsetY += itemHeightPx
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingId = null
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggingId = null
                                            dragOffsetY = 0f
                                        }
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Position number & Account details
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isBeingDragged) SolidPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBeingDragged) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SolidPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconHelper.AppIcon(
                                            iconName = item.account.iconName,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        val parentGroup = accountParentMap[item.account.id]
                                        val groupLabel = parentGroup?.localizedName(languageMode)
                                        if (!groupLabel.isNullOrBlank()) {
                                            Text(
                                                text = groupLabel,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = item.account.localizedName(languageMode),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val balanceColor = when {
                                            effectiveBal > 0 -> Color(0xFF10B981)
                                            effectiveBal < 0 -> Color(0xFFEF4444)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                        Text(
                                            text = LanguageHelper.formatCurrency(effectiveBal, languageMode),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = balanceColor
                                        )
                                    }
                                }

                                // Right: Up, Down, Drag Handle, Remove
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = orderedSelectedIds.toMutableList()
                                                val moved = list.removeAt(index)
                                                list.add(index - 1, moved)
                                                onReorder(list)
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = LanguageHelper.getString("move_up", languageMode),
                                            modifier = Modifier.size(15.dp),
                                            tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < orderedSelectedIds.size - 1) {
                                                val list = orderedSelectedIds.toMutableList()
                                                val moved = list.removeAt(index)
                                                list.add(index + 1, moved)
                                                onReorder(list)
                                            }
                                        },
                                        enabled = index < orderedSelectedIds.size - 1,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = LanguageHelper.getString("move_down", languageMode),
                                            modifier = Modifier.size(15.dp),
                                            tint = if (index < orderedSelectedIds.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    // Drag Handle (touch & drag directly)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isBeingDragged) SolidPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                            .pointerInput(accountId) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        draggingId = accountId
                                                        dragOffsetY = 0f
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffsetY += dragAmount.y
                                                        val list = currentOrderedIds
                                                        val currentIdx = list.indexOf(accountId)
                                                        if (currentIdx != -1) {
                                                            val swapThreshold = itemHeightPx * 0.45f
                                                            if (dragOffsetY > swapThreshold && currentIdx < list.size - 1) {
                                                                val targetIdx = currentIdx + 1
                                                                val mutable = list.toMutableList()
                                                                val moved = mutable.removeAt(currentIdx)
                                                                mutable.add(targetIdx, moved)
                                                                currentOnReorder(mutable)
                                                                dragOffsetY -= itemHeightPx
                                                            } else if (dragOffsetY < -swapThreshold && currentIdx > 0) {
                                                                val targetIdx = currentIdx - 1
                                                                val mutable = list.toMutableList()
                                                                val moved = mutable.removeAt(currentIdx)
                                                                mutable.add(targetIdx, moved)
                                                                currentOnReorder(mutable)
                                                                dragOffsetY += itemHeightPx
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        draggingId = null
                                                        dragOffsetY = 0f
                                                    },
                                                    onDragCancel = {
                                                        draggingId = null
                                                        dragOffsetY = 0f
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag to reorder",
                                            tint = if (isBeingDragged) SolidPrimary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    IconButton(
                                        onClick = { onRemove(accountId) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = LanguageHelper.getString("remove", languageMode),
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "add_more_action") {
                    OutlinedButton(
                        onClick = onSwitchToBrowse,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageHelper.getString("browse_select", languageMode), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAccountsBrowseSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    groupedAccounts: List<AccountGroupSuggestion>,
    flatIndividualAccounts: List<AccountWithBalance>,
    totalVisibleAccounts: Int,
    orderedSelectedIds: List<Long>,
    accountCalcConfig: AccountCalcConfig,
    accountActivityTimestamps: Map<Long, Long>,
    deselectedAccountTimestamps: Map<Long, Long>,
    languageMode: LanguageMode,
    onToggleAccount: (Long) -> Unit,
    onToggleGroup: (List<Long>, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIds = remember(orderedSelectedIds) { orderedSelectedIds.toSet() }

    Column(modifier = modifier) {
        Text(
            text = LanguageHelper.getString("manually_added_desc", languageMode),
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(LanguageHelper.getString("search_accounts", languageMode), fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Select All / Clear All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$totalVisibleAccounts ${LanguageHelper.getString("accounts", languageMode).lowercase()} • ${LanguageHelper.getString("selected", languageMode)}: ${selectedIds.size}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onSelectAll,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(LanguageHelper.getString("select_all", languageMode), fontSize = 11.5.sp)
                }
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(LanguageHelper.getString("clear_all", languageMode), fontSize = 11.5.sp)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

        // Manual Accounts List organized by Group
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            groupedAccounts.forEach { groupItem ->
                val groupAccountIds = groupItem.accounts.map { it.account.id }
                val allGroupSelected = groupAccountIds.isNotEmpty() && groupAccountIds.all { it in selectedIds }

                item(key = "group_header_${groupItem.group.id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onToggleGroup(groupAccountIds, allGroupSelected)
                            }
                            .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconHelper.AppIcon(
                                    iconName = groupItem.group.iconName,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = groupItem.group.localizedName(languageMode),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${groupItem.accounts.count { it.account.id in selectedIds }}/${groupItem.accounts.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                items(groupItem.accounts, key = { it.account.id }) { item ->
                    val isSelected = item.account.id in selectedIds
                    val isIncludedInCalc = accountCalcConfig.isIncluded(item.account.id)
                    val effectiveBal = if (isIncludedInCalc) {
                        accountCalcConfig.getEffectiveBalance(item.account.id, item.currentBalance)
                    } else {
                        item.currentBalance
                    }
                    val isNonZero = abs(effectiveBal) > 0.0001
                    val actTs = accountActivityTimestamps[item.account.id] ?: 0L
                    val deselTs = deselectedAccountTimestamps[item.account.id] ?: 0L
                    val isAutoEligible = !isSelected && isNonZero && actTs > 0L && actTs >= deselTs

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onToggleAccount(item.account.id)
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleAccount(item.account.id) },
                                colors = CheckboxDefaults.colors(checkedColor = SolidPrimary),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SolidPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconHelper.AppIcon(
                                    iconName = item.account.iconName,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.account.localizedName(languageMode),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isAutoEligible) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = LanguageHelper.getString("auto_added", languageMode),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isIncludedInCalc) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = LanguageHelper.getString("excluded", languageMode),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = item.account.type.name,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        val balanceColor = when {
                            effectiveBal > 0 -> Color(0xFF10B981)
                            effectiveBal < 0 -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Text(
                            text = LanguageHelper.formatCurrency(effectiveBal, languageMode),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = balanceColor
                        )
                    }
                }
            }
        }
    }
}
