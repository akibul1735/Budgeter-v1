package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.specs.AccountsFilterSpec
import com.example.ui.screens.AccountActiveStatusFilter
import com.example.ui.screens.AccountSortFilter
import com.example.ui.screens.AccountViewHierarchyFilter
import com.example.ui.theme.SolidPrimary
import com.example.util.LanguageHelper

/**
 * Simplified "Sort & filter" bottom sheet for the Accounts tab.
 * Replaces the long dropdown menu:
 *  - Sort: 4 chips (Default / Balance / Usage / Name). Tapping the selected chip flips direction.
 *  - Show: All / Active / Inactive + "Exclude with 0 balance" switch.
 *  - Footer: More filters + Clear filters.
 * Changes apply immediately; the sheet stays open until dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountsSortFilterSheet(
    sortFilter: AccountSortFilter,
    statusFilter: AccountActiveStatusFilter,
    excludeZeroBalance: Boolean,
    hasActiveFilters: Boolean,
    languageMode: LanguageMode,
    onSortChange: (AccountSortFilter) -> Unit,
    onStatusChange: (AccountActiveStatusFilter) -> Unit,
    onExcludeZeroChange: (Boolean) -> Unit,
    onMoreFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .testTag("accounts_sort_filter_sheet")
        ) {
            Text(
                text = if (isBn) "সাজানো ও ফিল্টার" else "Sort & filter",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ---------- Sort ----------
            SheetSectionLabel(if (isBn) "সাজান" else "Sort by")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SortChip(
                    selected = sortFilter == AccountSortFilter.DEFAULT,
                    label = if (isBn) "ডিফল্ট" else "Default",
                    tag = "sort_chip_default",
                    onClick = { onSortChange(AccountSortFilter.DEFAULT) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.AMOUNT_HIGH_TO_LOW ||
                        sortFilter == AccountSortFilter.AMOUNT_LOW_TO_HIGH,
                    label = (if (isBn) "ব্যালেন্স" else "Balance") + when (sortFilter) {
                        AccountSortFilter.AMOUNT_HIGH_TO_LOW -> " ↓"
                        AccountSortFilter.AMOUNT_LOW_TO_HIGH -> " ↑"
                        else -> ""
                    },
                    tag = "sort_chip_balance",
                    onClick = {
                        onSortChange(
                            if (sortFilter == AccountSortFilter.AMOUNT_HIGH_TO_LOW)
                                AccountSortFilter.AMOUNT_LOW_TO_HIGH
                            else AccountSortFilter.AMOUNT_HIGH_TO_LOW
                        )
                    }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.MOST_USED ||
                        sortFilter == AccountSortFilter.LEAST_USED,
                    label = (if (isBn) "ব্যবহার" else "Usage") + when (sortFilter) {
                        AccountSortFilter.MOST_USED -> " ↓"
                        AccountSortFilter.LEAST_USED -> " ↑"
                        else -> ""
                    },
                    tag = "sort_chip_usage",
                    onClick = {
                        onSortChange(
                            if (sortFilter == AccountSortFilter.MOST_USED)
                                AccountSortFilter.LEAST_USED
                            else AccountSortFilter.MOST_USED
                        )
                    }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.NAME_AZ ||
                        sortFilter == AccountSortFilter.NAME_ZA,
                    label = (if (isBn) "নাম" else "Name") + when (sortFilter) {
                        AccountSortFilter.NAME_AZ -> " A–Z"
                        AccountSortFilter.NAME_ZA -> " Z–A"
                        else -> ""
                    },
                    tag = "sort_chip_name",
                    onClick = {
                        onSortChange(
                            if (sortFilter == AccountSortFilter.NAME_AZ)
                                AccountSortFilter.NAME_ZA
                            else AccountSortFilter.NAME_AZ
                        )
                    }
                )
            }

            // ---------- Show ----------
            SheetSectionLabel(if (isBn) "দেখান" else "Show")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.ALL,
                    label = if (isBn) "সব" else "All",
                    tag = "status_all",
                    onClick = { onStatusChange(AccountActiveStatusFilter.ALL) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY,
                    label = if (isBn) "সক্রিয়" else "Active",
                    tag = "status_active",
                    onClick = { onStatusChange(AccountActiveStatusFilter.ACTIVE_ONLY) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY,
                    label = if (isBn) "নিষ্ক্রিয়" else "Inactive",
                    tag = "status_inactive",
                    onClick = { onStatusChange(AccountActiveStatusFilter.INACTIVE_ONLY) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("filter_exclude_zero"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("exclude_zero_balance", languageMode),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = excludeZeroBalance,
                    onCheckedChange = onExcludeZeroChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = SolidPrimary)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // ---------- Footer ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onMoreFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.dp, SolidPrimary)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (isBn) "আরও ফিল্টার" else "More filters",
                        fontWeight = FontWeight.SemiBold,
                        color = SolidPrimary
                    )
                }
                TextButton(
                    onClick = onClearFilters,
                    enabled = hasActiveFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("filter_clear_all")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = LanguageHelper.getString("clear_filters", languageMode),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChip(
    selected: Boolean,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 14.sp) },
        modifier = Modifier
            .height(40.dp)
            .testTag(tag),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SolidPrimary,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun StatusSegment(
    selected: Boolean,
    label: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}

/**
 * "More filters" bottom sheet for the Accounts tab.
 * Follows the exact design, typography, spacing, and day/night theme of AccountsSortFilterSheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountsMoreFilterSheet(
    filterState: FilterState,
    languageMode: LanguageMode,
    resultCount: Int? = null,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftState by remember { mutableStateOf(filterState) }

    val sortFilter = AccountsFilterSpec.getSort(draftState)
    val statusFilter = AccountsFilterSpec.getStatus(draftState)
    val typeFilter = AccountsFilterSpec.getType(draftState)
    val hierarchyFilter = AccountsFilterSpec.getHierarchy(draftState)
    val excludeZeroBalance = AccountsFilterSpec.getExcludeZeroBalance(draftState)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("accounts_more_filter_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "সকল ফিল্টার ও সাজানো" else "All filters & sort",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (draftState.isNotEmpty()) {
                    TextButton(
                        onClick = { draftState = emptyMap() },
                        modifier = Modifier.testTag("more_filter_reset_btn")
                    ) {
                        Text(
                            text = if (isBn) "রিসেট" else "Reset",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ---------- 1. Account Nature (Type) ----------
            SheetSectionLabel(if (isBn) "অ্যাকাউন্টের প্রকৃতি" else "Account nature")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = typeFilter == null,
                    label = if (isBn) "সব" else "All",
                    tag = "type_all",
                    onClick = { draftState = AccountsFilterSpec.withType(draftState, null) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = typeFilter == AccountType.ASSET,
                    label = if (isBn) "সম্পদ" else "Assets",
                    tag = "type_asset",
                    onClick = { draftState = AccountsFilterSpec.withType(draftState, AccountType.ASSET) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = typeFilter == AccountType.LIABILITY,
                    label = if (isBn) "দায়" else "Liabilities",
                    tag = "type_liability",
                    onClick = { draftState = AccountsFilterSpec.withType(draftState, AccountType.LIABILITY) }
                )
            }

            // ---------- 2. View Scope (Hierarchy) ----------
            SheetSectionLabel(if (isBn) "প্রদর্শন পরিধি" else "View scope")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SortChip(
                    selected = hierarchyFilter == AccountViewHierarchyFilter.ALL,
                    label = if (isBn) "সব প্রদর্শন" else "All view",
                    tag = "hierarchy_all",
                    onClick = { draftState = AccountsFilterSpec.withHierarchy(draftState, AccountViewHierarchyFilter.ALL) }
                )
                SortChip(
                    selected = hierarchyFilter == AccountViewHierarchyFilter.ONLY_GROUPS,
                    label = if (isBn) "শুধুমাত্র গ্রুপ" else "Only groups",
                    tag = "hierarchy_groups",
                    onClick = { draftState = AccountsFilterSpec.withHierarchy(draftState, AccountViewHierarchyFilter.ONLY_GROUPS) }
                )
                SortChip(
                    selected = hierarchyFilter == AccountViewHierarchyFilter.ONLY_ACCOUNTS,
                    label = if (isBn) "শুধুমাত্র অ্যাকাউন্ট" else "Only accounts",
                    tag = "hierarchy_accounts",
                    onClick = { draftState = AccountsFilterSpec.withHierarchy(draftState, AccountViewHierarchyFilter.ONLY_ACCOUNTS) }
                )
                SortChip(
                    selected = hierarchyFilter == AccountViewHierarchyFilter.EXCLUDED,
                    label = if (isBn) "বর্জিত" else "Excluded",
                    tag = "hierarchy_excluded",
                    onClick = { draftState = AccountsFilterSpec.withHierarchy(draftState, AccountViewHierarchyFilter.EXCLUDED) }
                )
            }

            // ---------- 3. Account Status ----------
            SheetSectionLabel(if (isBn) "অ্যাকাউন্টের অবস্থা" else "Account status")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.ALL,
                    label = if (isBn) "সব" else "All",
                    tag = "more_status_all",
                    onClick = { draftState = AccountsFilterSpec.withStatus(draftState, AccountActiveStatusFilter.ALL) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY,
                    label = if (isBn) "সক্রিয়" else "Active",
                    tag = "more_status_active",
                    onClick = { draftState = AccountsFilterSpec.withStatus(draftState, AccountActiveStatusFilter.ACTIVE_ONLY) }
                )
                StatusSegment(
                    modifier = Modifier.weight(1f),
                    selected = statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY,
                    label = if (isBn) "নিষ্ক্রিয়" else "Inactive",
                    tag = "more_status_inactive",
                    onClick = { draftState = AccountsFilterSpec.withStatus(draftState, AccountActiveStatusFilter.INACTIVE_ONLY) }
                )
            }

            // ---------- 4. Sort By ----------
            SheetSectionLabel(if (isBn) "সাজান" else "Sort by")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SortChip(
                    selected = sortFilter == AccountSortFilter.DEFAULT,
                    label = if (isBn) "ডিফল্ট" else "Default",
                    tag = "more_sort_default",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.DEFAULT) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.AMOUNT_HIGH_TO_LOW,
                    label = if (isBn) "ব্যালেন্স: বেশি → কম" else "Balance: High → Low",
                    tag = "more_sort_amount_desc",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.AMOUNT_HIGH_TO_LOW) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.AMOUNT_LOW_TO_HIGH,
                    label = if (isBn) "ব্যালেন্স: কম → বেশি" else "Balance: Low → High",
                    tag = "more_sort_amount_asc",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.AMOUNT_LOW_TO_HIGH) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.MOST_USED,
                    label = if (isBn) "সর্বাধিক ব্যবহৃত" else "Most used",
                    tag = "more_sort_most_used",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.MOST_USED) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.LEAST_USED,
                    label = if (isBn) "সর্বনিম্ন ব্যবহৃত" else "Least used",
                    tag = "more_sort_least_used",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.LEAST_USED) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.NAME_AZ,
                    label = if (isBn) "নাম: ক → য়" else "Name: A → Z",
                    tag = "more_sort_name_az",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.NAME_AZ) }
                )
                SortChip(
                    selected = sortFilter == AccountSortFilter.NAME_ZA,
                    label = if (isBn) "নাম: য় → ক" else "Name: Z → A",
                    tag = "more_sort_name_za",
                    onClick = { draftState = AccountsFilterSpec.withSort(draftState, AccountSortFilter.NAME_ZA) }
                )
            }

            // ---------- 5. Zero Balance Switch ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("more_filter_exclude_zero"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("exclude_zero_balance", languageMode),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = excludeZeroBalance,
                    onCheckedChange = { checked ->
                        draftState = AccountsFilterSpec.withExcludeZero(draftState, checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // ---------- 6. Footer Buttons ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("cancel", languageMode),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = { onApply(draftState) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("more_filter_apply_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (resultCount != null) {
                            if (isBn) "প্রয়োগ করুন ($resultCount)" else "Apply ($resultCount)"
                        } else {
                            if (isBn) "প্রয়োগ করুন" else "Apply"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
