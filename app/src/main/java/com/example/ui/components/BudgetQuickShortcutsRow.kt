package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary

/**
 * Compact quick shortcut toolbar placed directly below the summary card in the Budget tab.
 * Includes view selector (All, Only Groups, Only Categories), sort menu with comprehensive options,
 * expand/collapse all toggle, and fast filter shortcuts.
 */
@Composable
fun BudgetQuickShortcutsRow(
    filterState: BudgetFilterState,
    onFilterChange: (BudgetFilterState) -> Unit,
    allGroupsExpanded: Boolean,
    onToggleExpandAll: (Boolean) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val isBn = languageMode == LanguageMode.BANGLA
    var showViewMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

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
        // 1. Hierarchy View Selector (All / Only Groups / Only Categories)
        // ---------------------------------------------------------------------
        val isViewCustom = filterState.showOnlyCategoriesWithoutGroups || filterState.showOnlyGroups
        val (viewIcon, viewLabel) = when {
            filterState.showOnlyCategoriesWithoutGroups -> Icons.Default.FormatListBulleted to (if (isBn) "শুধু ক্যাটাগরি" else "Only Cats")
            filterState.showOnlyGroups -> Icons.Default.Folder to (if (isBn) "শুধু গ্রুপ" else "Only Groups")
            else -> Icons.Default.AccountTree to (if (isBn) "দৃশ্য: সকল" else "View: All")
        }

        Box {
            CompactToolbarPill(
                icon = viewIcon,
                label = viewLabel,
                hasDropdown = true,
                isActive = isViewCustom,
                onClick = { showViewMenu = true }
            )

            DropdownMenu(
                expanded = showViewMenu,
                onDismissRequest = { showViewMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = if (!isViewCustom) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "সকল (গ্রুপ ও ক্যাটাগরি)" else "All (Groups & Categories)",
                            fontSize = 12.sp,
                            fontWeight = if (!isViewCustom) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (!isViewCustom) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showViewMenu = false
                        onFilterChange(
                            filterState.copy(
                                showOnlyCategoriesWithoutGroups = false,
                                showOnlyGroups = false
                            )
                        )
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (filterState.showOnlyGroups) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু গ্রুপ (সংক্ষিপ্ত)" else "Only Groups (Collapsed)",
                            fontSize = 12.sp,
                            fontWeight = if (filterState.showOnlyGroups) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (filterState.showOnlyGroups) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showViewMenu = false
                        onFilterChange(
                            filterState.copy(
                                showOnlyGroups = true,
                                showOnlyCategoriesWithoutGroups = false
                            )
                        )
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            tint = if (filterState.showOnlyCategoriesWithoutGroups) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু ক্যাটাগরি (গ্রুপ ছাড়া)" else "Only Categories (Flat List)",
                            fontSize = 12.sp,
                            fontWeight = if (filterState.showOnlyCategoriesWithoutGroups) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (filterState.showOnlyCategoriesWithoutGroups) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showViewMenu = false
                        onFilterChange(
                            filterState.copy(
                                showOnlyCategoriesWithoutGroups = true,
                                showOnlyGroups = false
                            )
                        )
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 2. Sort Button (Dropdown menu with all options)
        // ---------------------------------------------------------------------
        val sortLabel = when (filterState.sortOrder) {
            BudgetSortOrder.SPENT_DESC, BudgetSortOrder.AMOUNT_DESC -> if (isBn) "ব্যয় ↓" else "Spent ↓"
            BudgetSortOrder.AMOUNT_ASC -> if (isBn) "ব্যয় ↑" else "Spent ↑"
            BudgetSortOrder.REMAINING_DESC -> if (isBn) "অবশিষ্ট ↓" else "Remaining ↓"
            BudgetSortOrder.REMAINING_ASC -> if (isBn) "অবশিষ্ট ↑" else "Remaining ↑"
            BudgetSortOrder.BUDGET_DESC -> if (isBn) "বাজেট ↓" else "Budget ↓"
            BudgetSortOrder.BUDGET_ASC -> if (isBn) "বাজেট ↑" else "Budget ↑"
            BudgetSortOrder.UTILIZATION_DESC -> if (isBn) "ব্যবহার % ↓" else "Used % ↓"
            BudgetSortOrder.NAME_ASC -> "A → Z"
            BudgetSortOrder.NAME_DESC -> "Z → A"
            BudgetSortOrder.DEFAULT -> if (isBn) "ডিফল্ট" else "Default"
        }
        val isSortCustom = filterState.sortOrder != BudgetSortOrder.DEFAULT &&
                filterState.sortOrder != BudgetSortOrder.AMOUNT_DESC &&
                filterState.sortOrder != BudgetSortOrder.SPENT_DESC

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
                // Actual Spent
                SortMenuItem(
                    title = if (isBn) "ব্যয়: বেশি থেকে কম" else "Actual Spent: High → Low",
                    isSelected = filterState.sortOrder == BudgetSortOrder.SPENT_DESC || filterState.sortOrder == BudgetSortOrder.AMOUNT_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.SPENT_DESC, sortByAmount = true))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "ব্যয়: কম থেকে বেশি" else "Actual Spent: Low → High",
                    isSelected = filterState.sortOrder == BudgetSortOrder.AMOUNT_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.AMOUNT_ASC, sortByAmount = false))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Remaining
                SortMenuItem(
                    title = if (isBn) "অবশিষ্ট: বেশি থেকে কম" else "Remaining: Most → Least",
                    isSelected = filterState.sortOrder == BudgetSortOrder.REMAINING_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.REMAINING_DESC, sortByAmount = false))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "অবশিষ্ট: কম থেকে বেশি" else "Remaining: Least → Most",
                    isSelected = filterState.sortOrder == BudgetSortOrder.REMAINING_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.REMAINING_ASC, sortByAmount = false))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Budget Limit
                SortMenuItem(
                    title = if (isBn) "বাজেট সীমা: বেশি থেকে কম" else "Budget Limit: High → Low",
                    isSelected = filterState.sortOrder == BudgetSortOrder.BUDGET_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.BUDGET_DESC, sortByAmount = false))
                    }
                )

                // Utilization
                SortMenuItem(
                    title = if (isBn) "ব্যবহারের হার %: বেশি থেকে কম" else "Utilization %: High → Low",
                    isSelected = filterState.sortOrder == BudgetSortOrder.UTILIZATION_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.UTILIZATION_DESC, sortByAmount = false))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Alphabetical
                SortMenuItem(
                    title = if (isBn) "নাম: A থেকে Z" else "Name: A → Z",
                    isSelected = filterState.sortOrder == BudgetSortOrder.NAME_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(filterState.copy(sortOrder = BudgetSortOrder.NAME_ASC, sortByAmount = false))
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 3. Expand / Collapse All (Visible in hierarchical view)
        // ---------------------------------------------------------------------
        if (!filterState.showOnlyCategoriesWithoutGroups) {
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
        // 4. Quick Filter: Remaining Only
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = Icons.Default.Savings,
            label = if (isBn) "শুধু অবশিষ্ট" else "Remaining",
            hasDropdown = false,
            isActive = filterState.showOnlyRemainingBalance,
            activeColor = SolidIncome,
            onClick = {
                onFilterChange(filterState.copy(showOnlyRemainingBalance = !filterState.showOnlyRemainingBalance))
            }
        )

        // ---------------------------------------------------------------------
        // 5. Quick Filter: Over Budget
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = Icons.Default.PriorityHigh,
            label = if (isBn) "অতিরিক্ত" else "Over Budget",
            hasDropdown = false,
            isActive = filterState.filterOnlyOverBudget,
            activeColor = SolidExpense,
            onClick = {
                onFilterChange(filterState.copy(filterOnlyOverBudget = !filterState.filterOnlyOverBudget))
            }
        )
    }
}

@Composable
private fun CompactToolbarPill(
    icon: ImageVector,
    label: String,
    hasDropdown: Boolean = false,
    isActive: Boolean = false,
    activeColor: Color = SolidPrimary,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor = if (isActive) activeColor.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val contentColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(7.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor),
        modifier = Modifier
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
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
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
