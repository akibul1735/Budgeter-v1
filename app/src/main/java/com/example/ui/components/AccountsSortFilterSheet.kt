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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.screens.AccountActiveStatusFilter
import com.example.ui.screens.AccountSortFilter
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
        color = if (selected) SolidPrimary else Color.Transparent
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
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}
