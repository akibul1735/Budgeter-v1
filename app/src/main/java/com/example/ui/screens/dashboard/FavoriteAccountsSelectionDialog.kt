package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlin.math.abs

@Composable
fun FavoriteAccountsSelectionDialog(
    allAccounts: List<AccountWithBalance>,
    initialSelectedIds: Set<Long>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    accountActivityTimestamps: Map<Long, Long> = emptyMap(),
    deselectedAccountTimestamps: Map<Long, Long> = emptyMap(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(initialSelectedIds.toMutableSet()) }

    // Flatten all active, included individual accounts (excluding those hidden from Accounts tab)
    val flatIndividualAccounts = remember(allAccounts, accountCalcConfig) {
        val list = mutableListOf<AccountWithBalance>()
        for (item in allAccounts) {
            if (item.subAccounts.isNotEmpty()) {
                list.addAll(item.subAccounts.filter { it.account.isActive && accountCalcConfig.isIncluded(it.account.id) })
            } else if (item.account.isActive && accountCalcConfig.isIncluded(item.account.id)) {
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

    // Filtered accounts for manual selection search
    val filteredAccounts = remember(flatIndividualAccounts, searchQuery) {
        if (searchQuery.isBlank()) {
            flatIndividualAccounts
        } else {
            flatIndividualAccounts.filter {
                it.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.account.nameBn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Compute Auto-Added accounts dynamically
    // Rule:
    // 1. Excluded accounts: If an account is excluded from the Accounts tab, do not auto-add or show it (enforced by flatIndividualAccounts).
    // 2. If the account's amount is excluded and its calculated balance becomes 0, do not show it.
    // 3. If the calculated balance remains positive or negative and it has recent activity, it can still appear.
    // 4. Automatically add an account when it has recent activity.
    // 5. Keep it in the Favorite Account Card while it has a non-zero balance.
    // 6. When its balance becomes 0, automatically deselect/remove it.
    // 7. Manual selection should remain independent from the auto-added list.
    val autoAddedAccounts = remember(flatIndividualAccounts, selectedIds, accountCalcConfig, accountActivityTimestamps, deselectedAccountTimestamps) {
        flatIndividualAccounts.filter { item ->
            val id = item.account.id
            if (id in selectedIds) {
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
                            onValueChange = { searchQuery = it },
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
                                text = "${filteredAccounts.size} ${LanguageHelper.getString("selected", languageMode).lowercase()}: ${selectedIds.size}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { selectedIds = flatIndividualAccounts.map { it.account.id }.toMutableSet() },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(LanguageHelper.getString("select_all", languageMode), fontSize = 11.5.sp)
                                }
                                TextButton(
                                    onClick = { selectedIds = mutableSetOf() },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(LanguageHelper.getString("clear_all", languageMode), fontSize = 11.5.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        // Manual Accounts List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(filteredAccounts, key = { it.account.id }) { item ->
                                val isSelected = item.account.id in selectedIds
                                val effectiveBal = accountCalcConfig.getEffectiveBalance(item.account.id, item.currentBalance)
                                val isNonZero = abs(effectiveBal) > 0.0001
                                val actTs = accountActivityTimestamps[item.account.id] ?: 0L
                                val deselTs = deselectedAccountTimestamps[item.account.id] ?: 0L
                                val isAutoEligible = !isSelected && isNonZero && actTs > 0L && actTs >= deselTs
                                val parentGroup = accountParentMap[item.account.id]
                                val groupLabel = parentGroup?.localizedName(languageMode)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedIds = if (isSelected) {
                                                (selectedIds - item.account.id).toMutableSet()
                                            } else {
                                                (selectedIds + item.account.id).toMutableSet()
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedIds = if (checked) {
                                                    (selectedIds + item.account.id).toMutableSet()
                                                } else {
                                                    (selectedIds - item.account.id).toMutableSet()
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
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
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isAutoEligible) {
                                                    Spacer(modifier = Modifier.width(6.dp))
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
                                                }
                                            }
                                            Text(
                                                text = item.account.type.name,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
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
                                                            selectedIds = (selectedIds + item.account.id).toMutableSet()
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
                        onClick = { onSave(selectedIds) },
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
