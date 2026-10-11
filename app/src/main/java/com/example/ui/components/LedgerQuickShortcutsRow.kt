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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.specs.LedgerFilterSpec
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.LanguageHelper

/**
 * Fast-access toolbar for the Transactions (Ledger) tab.
 * Gives immediate 1-tap switching for Type (All/Expense/Income/Transfer),
 * Date timeline preset dropdown, Payment account filter, Sort order,
 * formatted aggregate total badge, and Filter dialog trigger.
 */
@Composable
fun LedgerQuickShortcutsRow(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    accounts: List<Account>,
    totalFormatted: String,
    totalTextColor: Color,
    totalBgColor: Color,
    totalBorderColor: Color,
    onOpenFilterDialog: () -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val isBn = languageMode == LanguageMode.BANGLA
    var showTypeMenu by remember { mutableStateOf(false) }
    var showTimelineMenu by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val activeType = LedgerFilterSpec.getTransactionType(filterState)
    val datePreset = LedgerFilterSpec.getDatePreset(filterState)
    val selectedAccountIds = LedgerFilterSpec.getSelectedAccountIds(filterState)
    val sortOrder = LedgerFilterSpec.getSortOrder(filterState)
    val rowStyle = LedgerFilterSpec.getRowStyle(filterState)

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
        // 1. Transaction Type Selector (All / Expense / Income / Transfer)
        // ---------------------------------------------------------------------
        val isTypeCustom = activeType != null
        val (typeIcon, typeLabel, typeColor) = when (activeType) {
            TransactionType.EXPENSE -> Triple(Icons.Default.TrendingDown, if (isBn) "ব্যয়" else "Expense", SolidExpense)
            TransactionType.INCOME -> Triple(Icons.Default.TrendingUp, if (isBn) "আয়" else "Income", SolidIncome)
            TransactionType.TRANSFER -> Triple(Icons.Default.SwapHoriz, if (isBn) "স্থানান্তর" else "Transfer", SolidTransfer)
            null -> Triple(Icons.Default.SwapHoriz, if (isBn) "ধরন: সব" else "Type: All", SolidPrimary)
        }

        Box {
            CompactToolbarPill(
                icon = typeIcon,
                label = typeLabel,
                hasDropdown = true,
                isActive = isTypeCustom,
                activeColor = typeColor,
                onClick = { showTypeMenu = true }
            )

            DropdownMenu(
                expanded = showTypeMenu,
                onDismissRequest = { showTypeMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                    },
                    text = {
                        Text(
                            text = if (isBn) "সকল লেনদেন (All Types)" else "All Types",
                            fontSize = 12.sp,
                            fontWeight = if (activeType == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeType == null) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showTypeMenu = false
                        onFilterChange(LedgerFilterSpec.withType(filterState, null))
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু ব্যয় (Expense Only)" else "Expense Only",
                            fontSize = 12.sp,
                            fontWeight = if (activeType == TransactionType.EXPENSE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeType == TransactionType.EXPENSE) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showTypeMenu = false
                        onFilterChange(LedgerFilterSpec.withType(filterState, TransactionType.EXPENSE))
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু আয় (Income Only)" else "Income Only",
                            fontSize = 12.sp,
                            fontWeight = if (activeType == TransactionType.INCOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeType == TransactionType.INCOME) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showTypeMenu = false
                        onFilterChange(LedgerFilterSpec.withType(filterState, TransactionType.INCOME))
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(16.dp))
                    },
                    text = {
                        Text(
                            text = if (isBn) "শুধু স্থানান্তর (Transfer Only)" else "Transfer Only",
                            fontSize = 12.sp,
                            fontWeight = if (activeType == TransactionType.TRANSFER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        if (activeType == TransactionType.TRANSFER) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        showTypeMenu = false
                        onFilterChange(LedgerFilterSpec.withType(filterState, TransactionType.TRANSFER))
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 2. Timeline Preset Dropdown (Quick 1-tap Date Window)
        // ---------------------------------------------------------------------
        val isTimelineCustom = datePreset != LedgerDatePreset.LAST_12_MONTHS
        val timelineLabel = when (datePreset) {
            LedgerDatePreset.THIS_MONTH -> if (isBn) "চলতি মাস" else "This Month"
            LedgerDatePreset.LAST_MONTH -> if (isBn) "গত মাস" else "Last Month"
            LedgerDatePreset.THIS_WEEK -> if (isBn) "চলতি সপ্তাহ" else "This Week"
            LedgerDatePreset.LAST_WEEK -> if (isBn) "গত সপ্তাহ" else "Last Week"
            LedgerDatePreset.TODAY -> if (isBn) "আজকে" else "Today"
            LedgerDatePreset.YESTERDAY -> if (isBn) "গতকাল" else "Yesterday"
            LedgerDatePreset.THIS_QUARTER -> if (isBn) "ত্রৈমাসিক" else "Quarter"
            LedgerDatePreset.THIS_YEAR -> if (isBn) "চলতি বছর" else "This Year"
            LedgerDatePreset.LAST_YEAR -> if (isBn) "গত বছর" else "Last Year"
            LedgerDatePreset.SINCE_LAST_YEAR -> if (isBn) "গত বছর থেকে" else "Since Last Year"
            LedgerDatePreset.ALL_TIME -> if (isBn) "সব সময়" else "All Time"
            LedgerDatePreset.CUSTOM -> if (isBn) "কাস্টম" else "Custom"
            LedgerDatePreset.LAST_12_MONTHS -> if (isBn) "সময়: ১২ মাস" else "Time: 12 Mos"
        }

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
                LedgerDatePreset.values().forEach { preset ->
                    val isSelected = datePreset == preset
                    val label = when (preset) {
                        LedgerDatePreset.THIS_MONTH -> if (isBn) "চলতি মাস" else "This Month"
                        LedgerDatePreset.LAST_MONTH -> if (isBn) "গত মাস" else "Last Month"
                        LedgerDatePreset.LAST_12_MONTHS -> if (isBn) "বিগত ১২ মাস" else "Last 12 Months"
                        LedgerDatePreset.THIS_WEEK -> if (isBn) "চলতি সপ্তাহ" else "This Week"
                        LedgerDatePreset.LAST_WEEK -> if (isBn) "গত সপ্তাহ" else "Last Week"
                        LedgerDatePreset.TODAY -> if (isBn) "আজকে" else "Today"
                        LedgerDatePreset.YESTERDAY -> if (isBn) "গতকাল" else "Yesterday"
                        LedgerDatePreset.THIS_QUARTER -> if (isBn) "চলতি ত্রৈমাসিক" else "This Quarter"
                        LedgerDatePreset.THIS_YEAR -> if (isBn) "চলতি বছর" else "This Year"
                        LedgerDatePreset.LAST_YEAR -> if (isBn) "গত বছর" else "Last Year"
                        LedgerDatePreset.SINCE_LAST_YEAR -> if (isBn) "গত বছর থেকে" else "Since Last Year"
                        LedgerDatePreset.ALL_TIME -> if (isBn) "সব সময়" else "All Time"
                        LedgerDatePreset.CUSTOM -> if (isBn) "কাস্টম সময়সীমা..." else "Custom Range..."
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
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
                            if (preset == LedgerDatePreset.CUSTOM) {
                                onOpenFilterDialog()
                            } else {
                                onFilterChange(LedgerFilterSpec.withDatePreset(filterState, preset))
                            }
                        }
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. Account Filter Dropdown (Quick Switch Between Top Accounts)
        // ---------------------------------------------------------------------
        if (accounts.isNotEmpty()) {
            val isAccountCustom = selectedAccountIds.isNotEmpty()
            val accountLabel = when {
                selectedAccountIds.isEmpty() -> if (isBn) "হিসাব: সব" else "Account: All"
                selectedAccountIds.size == 1 -> {
                    val acc = accounts.firstOrNull { it.id == selectedAccountIds.first() }
                    if (acc != null) (if (isBn) acc.nameBn else acc.nameEn) else (if (isBn) "১টি হিসাব" else "1 Account")
                }
                else -> if (isBn) "${selectedAccountIds.size}টি হিসাব" else "${selectedAccountIds.size} Accounts"
            }

            Box {
                CompactToolbarPill(
                    icon = Icons.Default.AccountBalance,
                    label = accountLabel,
                    hasDropdown = true,
                    isActive = isAccountCustom,
                    onClick = { showAccountMenu = true }
                )

                DropdownMenu(
                    expanded = showAccountMenu,
                    onDismissRequest = { showAccountMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBn) "সকল হিসাব (All Accounts)" else "All Accounts",
                                fontSize = 12.sp,
                                fontWeight = if (selectedAccountIds.isEmpty()) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        trailingIcon = {
                            if (selectedAccountIds.isEmpty()) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(15.dp))
                            }
                        },
                        onClick = {
                            showAccountMenu = false
                            onFilterChange(LedgerFilterSpec.withAccountIds(filterState, emptySet()))
                        }
                    )

                    // Show top accounts
                    accounts.take(8).forEach { acc ->
                        val isSelected = selectedAccountIds.contains(acc.id)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isBn) acc.nameBn else acc.nameEn,
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
                                showAccountMenu = false
                                val nextIds = if (isSelected) {
                                    selectedAccountIds - acc.id
                                } else {
                                    setOf(acc.id)
                                }
                                onFilterChange(LedgerFilterSpec.withAccountIds(filterState, nextIds))
                            }
                        )
                    }

                    if (accounts.size > 8) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.FilterAlt, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(15.dp))
                            },
                            text = {
                                Text(
                                    text = if (isBn) "আরও হিসাব নির্বাচন করুন..." else "More Accounts...",
                                    fontSize = 11.5.sp,
                                    color = SolidPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = {
                                showAccountMenu = false
                                onOpenFilterDialog()
                            }
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. Sort Order Menu (Newest, Oldest, Amount High, Amount Low)
        // ---------------------------------------------------------------------
        val isSortCustom = sortOrder != LedgerFilterSpec.SORT_NEWEST_FIRST
        val sortLabel = when (sortOrder) {
            LedgerFilterSpec.SORT_OLDEST_FIRST -> if (isBn) "পুরানো আগে" else "Oldest First"
            LedgerFilterSpec.SORT_AMOUNT_DESC -> if (isBn) "পরিমাণ ↓" else "Amount ↓"
            LedgerFilterSpec.SORT_AMOUNT_ASC -> if (isBn) "পরিমাণ ↑" else "Amount ↑"
            else -> if (isBn) "সাজানো: নতুন" else "Sort: New"
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
                    title = if (isBn) "তারিখ: নতুন থেকে পুরানো" else "Date: Newest First",
                    isSelected = sortOrder == LedgerFilterSpec.SORT_NEWEST_FIRST,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(LedgerFilterSpec.withSortOrder(filterState, LedgerFilterSpec.SORT_NEWEST_FIRST))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "তারিখ: পুরানো থেকে নতুন" else "Date: Oldest First",
                    isSelected = sortOrder == LedgerFilterSpec.SORT_OLDEST_FIRST,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(LedgerFilterSpec.withSortOrder(filterState, LedgerFilterSpec.SORT_OLDEST_FIRST))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "পরিমাণ: বেশি থেকে কম" else "Amount: High to Low",
                    isSelected = sortOrder == LedgerFilterSpec.SORT_AMOUNT_DESC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(LedgerFilterSpec.withSortOrder(filterState, LedgerFilterSpec.SORT_AMOUNT_DESC))
                    }
                )
                SortMenuItem(
                    title = if (isBn) "পরিমাণ: কম থেকে বেশি" else "Amount: Low to High",
                    isSelected = sortOrder == LedgerFilterSpec.SORT_AMOUNT_ASC,
                    onClick = {
                        showSortMenu = false
                        onFilterChange(LedgerFilterSpec.withSortOrder(filterState, LedgerFilterSpec.SORT_AMOUNT_ASC))
                    }
                )
            }
        }

        // ---------------------------------------------------------------------
        // 5. Total Aggregate Pill (Live sum based on filters)
        // ---------------------------------------------------------------------
        if (totalFormatted.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = totalBgColor,
                border = BorderStroke(0.8.dp, totalBorderColor),
                modifier = Modifier.height(25.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (isBn) "মোট: " else "Total: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = totalTextColor.copy(alpha = 0.85f)
                    )
                    Text(
                        text = totalFormatted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = totalTextColor
                    )
                }
            }
        }

        // ---------------------------------------------------------------------
        // 6. Full Filter Dialog Trigger
        // ---------------------------------------------------------------------
        CompactToolbarPill(
            icon = Icons.Default.FilterAlt,
            label = if (isBn) "ফিল্টার" else "Filter",
            hasDropdown = false,
            isActive = false,
            onClick = onOpenFilterDialog
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
