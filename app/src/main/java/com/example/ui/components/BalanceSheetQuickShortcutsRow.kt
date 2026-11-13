package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedFilterChipItem
import com.example.ui.components.filter.activeChips
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.clear
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.removeActiveChip
import com.example.ui.components.filter.specs.BalanceSheetFilterSpec
import com.example.ui.theme.SolidPrimary
import com.example.util.BalanceSheetSectionFilter
import com.example.util.BalanceSheetSortOrder

/**
 * Compact filter and sorting bar placed directly below the Balance Sheet summary card.
 * Features independent, simultaneous controls:
 * 1. Section cycling pill (All -> Groups -> Accounts -> All)
 * 2. Sort dropdown (Low to High, High to Low, A to Z, Z to A)
 * 3. Hide 0 toggle pill
 * 4. Calc mode toggle pill
 * Plus active dialog filter chips.
 */
@Composable
fun BalanceSheetQuickShortcutsRow(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onSectionChanged: (BalanceSheetSectionFilter) -> Unit = {},
    isCalcMode: Boolean = false,
    onToggleCalcMode: () -> Unit = {},
    onOpenFilterDialog: () -> Unit,
    onScrollToTop: () -> Unit = {},
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    spec: FilterSpec<*>? = null,
    onClearAll: (() -> Unit)? = null
) {
    val isBn = languageMode == LanguageMode.BANGLA
    var showSortMenu by remember { mutableStateOf(false) }

    val sectionFilter = BalanceSheetFilterSpec.getSectionFilter(filterState)
    val sortOrder = BalanceSheetFilterSpec.getSortOrder(filterState)
    val excludeZero = BalanceSheetFilterSpec.getExcludeZero(filterState)

    val isFilterActive = spec?.isActive(filterState) ?: (BalanceSheetFilterSpec.activeFilterCount(filterState) > 0)
    val activeCount = spec?.activeFilterCount(filterState) ?: BalanceSheetFilterSpec.activeFilterCount(filterState)
    val extraChips = remember(spec, filterState, languageMode) {
        (spec?.activeChips(filterState, languageMode) ?: emptyList()).filterNot { chip ->
            chip.fieldId == BalanceSheetFilterSpec.FIELD_SORT ||
                chip.fieldId == BalanceSheetFilterSpec.FIELD_AMOUNT_VIEW ||
                chip.id == "toggle_display_options_${BalanceSheetFilterSpec.FIELD_EXCLUDE_ZERO}" ||
                chip.id == "toggle_display_options_${BalanceSheetFilterSpec.FIELD_WITHOUT_GROUPS}" ||
                chip.id == "toggle_display_options_${BalanceSheetFilterSpec.FIELD_ONLY_GROUPS}" ||
                chip.id == "toggle_display_options_${BalanceSheetFilterSpec.FIELD_COMPARE_ENABLED}"
        }
    }

    val consumeHorizontalScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                return Offset(available.x, 0f)
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(available.x, 0f)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(consumeHorizontalScroll)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---------------------------------------------------------------------
        // 1. Section Cycling Pill: All -> Groups -> Accounts -> All
        // ---------------------------------------------------------------------
        val sectionIcon = when (sectionFilter) {
            BalanceSheetSectionFilter.ALL -> Icons.Default.Layers
            BalanceSheetSectionFilter.GROUPS -> Icons.Default.Folder
            BalanceSheetSectionFilter.ACCOUNTS -> Icons.Default.AccountBalanceWallet
        }
        val sectionLabel = if (isBn) sectionFilter.titleBn else sectionFilter.titleEn
        CompactToolbarPill(
            icon = sectionIcon,
            label = sectionLabel,
            hasDropdown = false,
            isActive = sectionFilter != BalanceSheetSectionFilter.ALL,
            activeColor = SolidPrimary,
            modifier = Modifier.testTag("balance_sheet_section_cycle_button"),
            onClick = {
                val nextSection = when (sectionFilter) {
                    BalanceSheetSectionFilter.ALL -> BalanceSheetSectionFilter.GROUPS
                    BalanceSheetSectionFilter.GROUPS -> BalanceSheetSectionFilter.ACCOUNTS
                    BalanceSheetSectionFilter.ACCOUNTS -> BalanceSheetSectionFilter.ALL
                }
                onSectionChanged(nextSection)
                onFilterChange(BalanceSheetFilterSpec.withSectionFilter(filterState, nextSection))
            }
        )

        // ---------------------------------------------------------------------
        // 2. Sort Dropdown: Low to High, High to Low, A to Z, Z to A
        // ---------------------------------------------------------------------
        val sortIcon = when (sortOrder) {
            BalanceSheetSortOrder.AMOUNT_ASC -> Icons.Default.ArrowUpward
            BalanceSheetSortOrder.AMOUNT_DESC -> Icons.Default.ArrowDownward
            else -> Icons.AutoMirrored.Filled.Sort
        }
        val sortLabel = when (sortOrder) {
            BalanceSheetSortOrder.AMOUNT_ASC -> if (isBn) "কম → বেশি" else "Low to High"
            BalanceSheetSortOrder.AMOUNT_DESC -> if (isBn) "বেশি → কম" else "High to Low"
            BalanceSheetSortOrder.NAME_ASC -> if (isBn) "A → Z" else "A to Z"
            BalanceSheetSortOrder.NAME_DESC -> if (isBn) "Z → A" else "Z to A"
            BalanceSheetSortOrder.DEFAULT -> if (isBn) "A → Z" else "A to Z"
        }
        val isSortActive = sortOrder != BalanceSheetSortOrder.AMOUNT_DESC

        Box {
            CompactToolbarPill(
                icon = sortIcon,
                label = sortLabel,
                hasDropdown = true,
                isActive = isSortActive,
                activeColor = SolidPrimary,
                modifier = Modifier.testTag("balance_sheet_sort_dropdown_button"),
                onClick = { showSortMenu = true }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortMenuItem(
                    title = if (isBn) "কম → বেশি (Low to High)" else "Low to High",
                    isSelected = sortOrder == BalanceSheetSortOrder.AMOUNT_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.AMOUNT_ASC))
                        onScrollToTop()
                    }
                )
                SortMenuItem(
                    title = if (isBn) "বেশি → কম (High to Low)" else "High to Low",
                    isSelected = sortOrder == BalanceSheetSortOrder.AMOUNT_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.AMOUNT_DESC))
                        onScrollToTop()
                    }
                )
                SortMenuItem(
                    title = if (isBn) "A → Z" else "A to Z",
                    isSelected = sortOrder == BalanceSheetSortOrder.NAME_ASC || sortOrder == BalanceSheetSortOrder.DEFAULT,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.NAME_ASC))
                        onScrollToTop()
                    }
                )
                SortMenuItem(
                    title = if (isBn) "Z → A" else "Z to A",
                    isSelected = sortOrder == BalanceSheetSortOrder.NAME_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.NAME_DESC))
                        onScrollToTop()
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 3. Hide 0 Toggle Pill
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = if (excludeZero) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            label = if (isBn) "০ লুকান" else "Hide 0",
            hasDropdown = false,
            isActive = excludeZero,
            activeColor = SolidPrimary,
            modifier = Modifier.testTag("balance_sheet_hide_zero_button"),
            onClick = {
                onFilterChange(BalanceSheetFilterSpec.withExcludeZero(filterState, !excludeZero))
            }
        )

        // ---------------------------------------------------------------------
        // 4. Calc Mode Toggle Pill
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = Icons.Default.Tune,
            label = if (isBn) "হিসাব" else "Calc",
            hasDropdown = false,
            isActive = isCalcMode,
            activeColor = SolidPrimary,
            modifier = Modifier.testTag("balance_sheet_calc_toggle_button"),
            onClick = onToggleCalcMode
        )

        // ---------------------------------------------------------------------
        // 5. Active Dialog Filter Status / Reset / Extra Chips
        // ---------------------------------------------------------------------
        if (isFilterActive || extraChips.isNotEmpty()) {
            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (extraChips.isNotEmpty()) {
                CompactToolbarPill(
                    icon = Icons.Default.FilterAlt,
                    label = if (isBn) "ফিল্টার ($activeCount)" else "Filter ($activeCount)",
                    hasDropdown = false,
                    isActive = true,
                    isEmphasized = true,
                    activeColor = SolidPrimary,
                    modifier = Modifier.testTag("active_filter_badge"),
                    onClick = onOpenFilterDialog
                )
            }

            CompactToolbarPill(
                icon = Icons.Default.RestartAlt,
                label = if (isBn) "রিসেট" else "Reset",
                hasDropdown = false,
                isActive = false,
                modifier = Modifier.testTag("filter_reset_btn"),
                onClick = {
                    if (onClearAll != null) onClearAll()
                    else if (spec != null) onFilterChange(spec.clear(filterState))
                    else onFilterChange(emptyMap())
                }
            )

            extraChips.forEach { chip ->
                CompactActiveFilterChip(
                    chip = chip,
                    accentColor = SolidPrimary,
                    onRemove = {
                        val newState = spec?.removeActiveChip(filterState, chip.id) ?: filterState
                        onFilterChange(newState)
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactToolbarPill(
    icon: ImageVector,
    label: String,
    hasDropdown: Boolean = false,
    isActive: Boolean = false,
    isEmphasized: Boolean = false,
    activeColor: Color = SolidPrimary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isEmphasized -> activeColor
        isActive -> activeColor.copy(alpha = 0.16f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val borderColor = when {
        isEmphasized -> activeColor
        isActive -> activeColor.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }
    val contentColor = when {
        isEmphasized -> Color.White
        isActive -> activeColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(0.85.dp, borderColor),
        modifier = modifier
            .height(29.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.5.dp)
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = if (isActive || isEmphasized) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hasDropdown) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactActiveFilterChip(
    chip: UnifiedFilterChipItem,
    accentColor: Color,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .height(27.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(start = 7.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (chip.icon != null) {
                Icon(
                    imageVector = chip.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = chip.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(9.5.dp)
                )
            }
        }
    }
}

@Composable
private fun SortMenuItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurface
            )
        },
        trailingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        },
        onClick = onClick
    )
}
