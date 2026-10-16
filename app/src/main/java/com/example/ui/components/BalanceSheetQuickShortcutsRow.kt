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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
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
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetSortOrder

/**
 * Unified, single-bar filter and shortcut toolbar for Balance Sheet.
 * Combines quick 1-tap shortcuts (Section, Timeline preset, Sort, ৳0 toggle, Calc, Expand)
 * and active filter state (active filter indicator badge, 1-tap reset, active chips with individual dismissal)
 * into a single compact, horizontally scrollable row without spawning a second bar.
 */
@Composable
fun BalanceSheetQuickShortcutsRow(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    isCalcMode: Boolean,
    onToggleCalcMode: () -> Unit,
    allGroupsExpanded: Boolean,
    onToggleExpandAll: (Boolean) -> Unit,
    onOpenFilterDialog: () -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    spec: FilterSpec<*>? = null,
    onClearAll: (() -> Unit)? = null
) {
    val isBn = languageMode == LanguageMode.BANGLA
    var showSectionMenu by remember { mutableStateOf(false) }
    var showTimelineMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val activeSection = BalanceSheetFilterSpec.getActiveTab(filterState)
    val datePreset = BalanceSheetFilterSpec.getDatePreset(filterState)
    val sortOrder = BalanceSheetFilterSpec.getSortOrder(filterState)
    val excludeZero = BalanceSheetFilterSpec.getExcludeZero(filterState)
    val withoutGroups = BalanceSheetFilterSpec.getShowWithoutGroups(filterState)

    val isFilterActive = spec?.isActive(filterState) ?: (BalanceSheetFilterSpec.activeFilterCount(filterState) > 0)
    val activeCount = spec?.activeFilterCount(filterState) ?: BalanceSheetFilterSpec.activeFilterCount(filterState)
    val activeChips = remember(spec, filterState, languageMode) {
        spec?.activeChips(filterState, languageMode) ?: emptyList()
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
        // 0. Unified Filter Trigger & Active Status Controls
        // ---------------------------------------------------------------------
        if (isFilterActive) {
            // Emphasized Active Filter Badge with count
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

            // Fast Reset / Clear All Pill
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

            // Active Filter Chips (if any custom dialog criteria like accounts, status, search, options)
            activeChips.forEach { chip ->
                CompactActiveFilterChip(
                    chip = chip,
                    accentColor = SolidPrimary,
                    onRemove = {
                        val newState = spec?.removeActiveChip(filterState, chip.id) ?: filterState
                        onFilterChange(newState)
                    }
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(16.dp)
                    .padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        } else {
            // Inactive Filter Dialog Trigger
            CompactToolbarPill(
                icon = Icons.Default.FilterAlt,
                label = if (isBn) "ফিল্টার" else "Filter",
                hasDropdown = false,
                isActive = false,
                modifier = Modifier.testTag("filter_dialog_trigger_btn"),
                onClick = onOpenFilterDialog
            )
        }

        // ---------------------------------------------------------------------
        // 1. Balance Sheet Section Selector: All / Assets / Liabilities
        // ---------------------------------------------------------------------
        val isSectionCustom = activeSection != BalanceSheetFilterSpec.TAB_ALL
        val (sectionIcon, sectionLabel) = when (activeSection) {
            BalanceSheetFilterSpec.TAB_ASSETS -> Icons.AutoMirrored.Filled.TrendingUp to (if (isBn) "সম্পদ" else "Assets")
            BalanceSheetFilterSpec.TAB_LIABILITIES -> Icons.AutoMirrored.Filled.TrendingDown to (if (isBn) "দায়" else "Liabilities")
            else -> Icons.Default.Layers to (if (isBn) "সেকশন: সব" else "Section: All")
        }

        Box {
            CompactToolbarPill(
                icon = sectionIcon,
                label = sectionLabel,
                hasDropdown = true,
                isActive = isSectionCustom,
                onClick = { showSectionMenu = true }
            )

            DropdownMenu(
                expanded = showSectionMenu,
                onDismissRequest = { showSectionMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (activeSection == BalanceSheetFilterSpec.TAB_ALL) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "সব (সম্পদ ও দায়)" else "All (Assets & Liabilities)",
                            fontSize = 12.sp,
                            fontWeight = if (activeSection == BalanceSheetFilterSpec.TAB_ALL) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeSection == BalanceSheetFilterSpec.TAB_ALL) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showSectionMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_ALL))
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (activeSection == BalanceSheetFilterSpec.TAB_ASSETS) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু সম্পদ (Assets Only)" else "Assets Only",
                            fontSize = 12.sp,
                            fontWeight = if (activeSection == BalanceSheetFilterSpec.TAB_ASSETS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeSection == BalanceSheetFilterSpec.TAB_ASSETS) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showSectionMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_ASSETS))
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (activeSection == BalanceSheetFilterSpec.TAB_LIABILITIES) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু দায় (Liabilities Only)" else "Liabilities Only",
                            fontSize = 12.sp,
                            fontWeight = if (activeSection == BalanceSheetFilterSpec.TAB_LIABILITIES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeSection == BalanceSheetFilterSpec.TAB_LIABILITIES) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showSectionMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withActiveTab(filterState, BalanceSheetFilterSpec.TAB_LIABILITIES))
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 2. Timeline Preset Dropdown (Quick Date Preset Selector)
        // ---------------------------------------------------------------------
        val isTimelineCustom = datePreset != BalanceSheetComparisonPreset.THIS_MONTH
        val timelineLabel = if (isBn) datePreset.titleBn else datePreset.titleEn

        Box {
            CompactToolbarPill(
                icon = Icons.Default.CalendarMonth,
                label = timelineLabel,
                hasDropdown = true,
                isActive = isTimelineCustom,
                onClick = { showTimelineMenu = true }
            )

            DropdownMenu(
                expanded = showTimelineMenu,
                onDismissRequest = { showTimelineMenu = false }
            ) {
                BalanceSheetFilterSpec.DATE_PRESET_OPTIONS.forEach { opt ->
                    val isSelected = when (opt.id) {
                        BalanceSheetFilterSpec.PRESET_THIS_MONTH -> datePreset == BalanceSheetComparisonPreset.THIS_MONTH
                        BalanceSheetFilterSpec.PRESET_END_OF_LAST_MONTH -> datePreset == BalanceSheetComparisonPreset.END_OF_LAST_MONTH
                        BalanceSheetFilterSpec.PRESET_BEGINNING_OF_MONTH -> datePreset == BalanceSheetComparisonPreset.BEGINNING_OF_MONTH
                        BalanceSheetFilterSpec.PRESET_LAST_12_MONTHS -> datePreset == BalanceSheetComparisonPreset.LAST_12_MONTHS
                        BalanceSheetFilterSpec.PRESET_PREVIOUS_MONTH -> datePreset == BalanceSheetComparisonPreset.PREVIOUS_MONTH
                        BalanceSheetFilterSpec.PRESET_BEGINNING_OF_YEAR -> datePreset == BalanceSheetComparisonPreset.BEGINNING_OF_YEAR
                        BalanceSheetFilterSpec.PRESET_LAST_30_DAYS -> datePreset == BalanceSheetComparisonPreset.LAST_30_DAYS
                        BalanceSheetFilterSpec.PRESET_TODAY -> datePreset == BalanceSheetComparisonPreset.TODAY
                        BalanceSheetFilterSpec.PRESET_CUSTOM -> datePreset == BalanceSheetComparisonPreset.CUSTOM
                        else -> false
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBn) opt.titleBn else opt.titleEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(15.dp))
                            }
                        },
                        onClick = {
                            showTimelineMenu = false
                            val newPreset = when (opt.id) {
                                BalanceSheetFilterSpec.PRESET_THIS_MONTH -> BalanceSheetComparisonPreset.THIS_MONTH
                                BalanceSheetFilterSpec.PRESET_END_OF_LAST_MONTH -> BalanceSheetComparisonPreset.END_OF_LAST_MONTH
                                BalanceSheetFilterSpec.PRESET_BEGINNING_OF_MONTH -> BalanceSheetComparisonPreset.BEGINNING_OF_MONTH
                                BalanceSheetFilterSpec.PRESET_LAST_12_MONTHS -> BalanceSheetComparisonPreset.LAST_12_MONTHS
                                BalanceSheetFilterSpec.PRESET_PREVIOUS_MONTH -> BalanceSheetComparisonPreset.PREVIOUS_MONTH
                                BalanceSheetFilterSpec.PRESET_BEGINNING_OF_YEAR -> BalanceSheetComparisonPreset.BEGINNING_OF_YEAR
                                BalanceSheetFilterSpec.PRESET_LAST_30_DAYS -> BalanceSheetComparisonPreset.LAST_30_DAYS
                                BalanceSheetFilterSpec.PRESET_TODAY -> BalanceSheetComparisonPreset.TODAY
                                else -> BalanceSheetComparisonPreset.CUSTOM
                            }
                            onFilterChange(BalanceSheetFilterSpec.withPreset(filterState, newPreset))
                        }
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. Sort Order Menu
        // ---------------------------------------------------------------------
        val isSortCustom = sortOrder != BalanceSheetSortOrder.AMOUNT_DESC
        val sortLabel = when (sortOrder) {
            BalanceSheetSortOrder.AMOUNT_DESC -> if (isBn) "পরিমাণ ↓" else "Amount ↓"
            BalanceSheetSortOrder.AMOUNT_ASC -> if (isBn) "পরিমাণ ↑" else "Amount ↑"
            BalanceSheetSortOrder.NAME_ASC -> if (isBn) "নাম A-Z" else "Name A-Z"
            BalanceSheetSortOrder.DEFAULT -> if (isBn) "ডিফল্ট ক্রম" else "Default"
        }

        Box {
            CompactToolbarPill(
                icon = Icons.AutoMirrored.Filled.Sort,
                label = sortLabel,
                hasDropdown = true,
                isActive = isSortCustom,
                onClick = { showSortMenu = true }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortMenuItem(
                    title = if (isBn) "পরিমাণ: বেশি থেকে কম" else "Amount: High to Low",
                    isSelected = sortOrder == BalanceSheetSortOrder.AMOUNT_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.AMOUNT_DESC))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "পরিমাণ: কম থেকে বেশি" else "Amount: Low to High",
                    isSelected = sortOrder == BalanceSheetSortOrder.AMOUNT_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.AMOUNT_ASC))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "নাম: ক থেকে ঁ / A to Z" else "Name: A to Z",
                    isSelected = sortOrder == BalanceSheetSortOrder.NAME_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.NAME_ASC))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "ডিফল্ট ক্রম" else "Default Order",
                    isSelected = sortOrder == BalanceSheetSortOrder.DEFAULT,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(BalanceSheetFilterSpec.withSortOrder(filterState, BalanceSheetSortOrder.DEFAULT))
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 4. Zero Balance Toggle (Exclude vs Show ৳0)
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = if (excludeZero) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            label = if (excludeZero) (if (isBn) "শূন্য লুকানো" else "Hide ৳0") else (if (isBn) "সকল ব্যালেন্স" else "Show ৳0"),
            hasDropdown = false,
            isActive = !excludeZero,
            activeColor = SolidPrimary,
            onClick = {
                onFilterChange(BalanceSheetFilterSpec.withExcludeZero(filterState, !excludeZero))
            }
        )

        // ---------------------------------------------------------------------
        // 5. Expand / Collapse All Parent Groups (If hierarchical view is active)
        // ---------------------------------------------------------------------
        if (!withoutGroups) {
            val expandIcon = if (allGroupsExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore
            val expandLabel = if (allGroupsExpanded) {
                if (isBn) "সব বন্ধ" else "Collapse"
            } else {
                if (isBn) "সব খুলুন" else "Expand"
            }

            CompactToolbarPill(
                icon = expandIcon,
                label = expandLabel,
                hasDropdown = false,
                isActive = false,
                onClick = { onToggleExpandAll(!allGroupsExpanded) }
            )
        }

        // ---------------------------------------------------------------------
        // 6. Calculation Mode Toggle ("হিসাব গণনা" / "Calc Mode")
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = Icons.Default.Tune,
            label = if (isBn) "হিসাব গণনা" else "Calc",
            hasDropdown = false,
            isActive = isCalcMode,
            activeColor = SolidPrimary,
            onClick = onToggleCalcMode
        )
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
        isActive -> activeColor.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val borderColor = when {
        isEmphasized -> activeColor
        isActive -> activeColor.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }
    val contentColor = when {
        isEmphasized -> Color.White
        isActive -> activeColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(7.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor),
        modifier = modifier
            .height(25.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(11.5.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive || isEmphasized) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hasDropdown) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
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
        shape = RoundedCornerShape(7.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .height(25.dp)
            .clip(RoundedCornerShape(7.dp))
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 3.dp),
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
                fontSize = 11.5.sp,
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
