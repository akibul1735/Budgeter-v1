package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.DateUtils
import com.example.util.LanguageHelper

@Composable
internal fun AccountsPaymentSourceTabContent(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode,
    searchQuery: String = "",
    selectedMonthLabel: String = "",
    onMonthClick: () -> Unit = {},
    calculationBasis: RequirementCalculationBasis = RequirementCalculationBasis.BUDGET_AMOUNT,
    onCalculationBasisChange: (RequirementCalculationBasis) -> Unit = {},
    accountStatusFilter: AccountStatusFilter,
    onStatusFilterChange: (AccountStatusFilter) -> Unit,
    sortOption: PaymentSourceSortOption,
    onSortOptionChange: (PaymentSourceSortOption) -> Unit,
    onOpenSourceSelector: () -> Unit,
    onOpenSuggestedTransfers: () -> Unit,
    onOpenAddObligation: () -> Unit,
    onExecuteTransferSuggestion: (FundAllocationSuggestion) -> Unit,
    onAccountClick: (Long) -> Unit,
    onAssignItem: (Account) -> Unit,
    onDeleteObligation: (String) -> Unit,
    onNavigateToAssignedItems: () -> Unit = {}
) {
    val filteredAccounts = remember(overview.accountAnalyses, accountStatusFilter, searchQuery, sortOption) {
        val byStatus = when (accountStatusFilter) {
            AccountStatusFilter.ALL -> overview.accountAnalyses
            AccountStatusFilter.SHORTFALL_ONLY -> overview.accountAnalyses.filter { it.isShortfall }
            AccountStatusFilter.SURPLUS_ONLY -> overview.accountAnalyses.filter { it.isSurplus }
        }
        val q = searchQuery.trim().lowercase()
        val list = if (q.isEmpty()) {
            byStatus
        } else {
            byStatus.filter {
                it.account.nameEn.lowercase().contains(q) ||
                it.account.nameBn.lowercase().contains(q)
            }
        }
        when (sortOption) {
            PaymentSourceSortOption.DEFAULT -> list
            PaymentSourceSortOption.BALANCE_DESC -> list.sortedByDescending { it.currentBalance }
            PaymentSourceSortOption.BALANCE_ASC -> list.sortedBy { it.currentBalance }
            PaymentSourceSortOption.REQUIRED_DESC -> list.sortedByDescending { it.requiredExpenseAmount }
            PaymentSourceSortOption.SHORTFALL_DESC -> list.sortedByDescending { it.shortfall }
            PaymentSourceSortOption.NAME_ASC -> list.sortedBy { it.account.localizedName(languageMode).lowercase() }
        }
    }

    // Set of expanded account card IDs. Default: expand first account or shortfall accounts
    val expandedAccountIds = remember(filteredAccounts) {
        mutableStateMapOf<Long, Boolean>().apply {
            filteredAccounts.forEachIndexed { idx, acc ->
                this[acc.account.id] = (idx == 0 || acc.isShortfall)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_source_tab_content"),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Row: "Payment source" Title on left & "Nov 2026 ▾" Dropdown on right
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স" else "Payment source",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val monthText = if (selectedMonthLabel.isNotBlank()) selectedMonthLabel else "${DateUtils.getMonthName(overview.month, languageMode).take(3)} ${overview.year}"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMonthClick() }
                        .testTag("btn_payment_source_month")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = monthText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Month",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Segmented Pill Buttons: [ Budget | Remaining ]
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("basis_switch_container"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isBudget = calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onCalculationBasisChange(RequirementCalculationBasis.BUDGET_AMOUNT) }
                            .testTag("switch_budget_button"),
                        shape = RoundedCornerShape(9.dp),
                        color = if (isBudget) MaterialTheme.colorScheme.surface else Color.Transparent,
                        border = if (isBudget) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) else null,
                        shadowElevation = if (isBudget) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget",
                                fontSize = 13.sp,
                                fontWeight = if (isBudget) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBudget) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val isRemaining = calculationBasis == RequirementCalculationBasis.REMAINING_AMOUNT
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onCalculationBasisChange(RequirementCalculationBasis.REMAINING_AMOUNT) }
                            .testTag("switch_remaining_button"),
                        shape = RoundedCornerShape(9.dp),
                        color = if (isRemaining) MaterialTheme.colorScheme.surface else Color.Transparent,
                        border = if (isRemaining) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) else null,
                        shadowElevation = if (isRemaining) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining",
                                fontSize = 13.sp,
                                fontWeight = if (isRemaining) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRemaining) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Top KPI Card: "Net after full budget" with large amount & progress bar
        item {
            val isBudgetBasis = calculationBasis == RequirementCalculationBasis.BUDGET_AMOUNT
            val kpiSubtitle = if (isBudgetBasis) {
                if (languageMode == LanguageMode.BANGLA) "পূর্ণ বাজেট পরবর্তী নিট স্থিতি" else "Net after full budget"
            } else {
                if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট বাজেট পরবর্তী নিট স্থিতি" else "Net after remaining"
            }
            val isPositiveNet = overview.netStatus >= 0
            val netColor = if (isPositiveNet) SolidIncome else SolidExpense
            val formattedNet = buildString {
                if (isPositiveNet) append("+") else append("-")
                append(LanguageHelper.formatCurrency(kotlin.math.abs(overview.netStatus), languageMode))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_source_overview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = kpiSubtitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedNet,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = netColor,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয়" else "Required"} ${LanguageHelper.formatCurrency(overview.totalRequired, languageMode)} · ${if (languageMode == LanguageMode.BANGLA) "মজুদ" else "Available"} ${LanguageHelper.formatCurrency(overview.totalAvailable, languageMode)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Full-width Progress Bar
                    val progress = if (overview.totalRequired <= 0.0) 1.0f
                    else (overview.totalAvailable / (overview.totalRequired + overview.totalAvailable).coerceAtLeast(1.0)).toFloat().coerceIn(0.05f, 1.0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(if (isPositiveNet) 1f else progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(netColor)
                        )
                    }
                }
            }
        }

        // 4. Alert 1: Shortfall Alert Banner (if any source account is short)
        val firstShortfall = overview.accountAnalyses.firstOrNull { it.isShortfall }
        if (firstShortfall != null) {
            item {
                val suggestion = overview.transferSuggestions.firstOrNull { it.toAccount.id == firstShortfall.account.id }
                    ?: overview.transferSuggestions.firstOrNull()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_shortfall_banner"),
                    shape = RoundedCornerShape(14.dp),
                    color = SolidExpense.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ⓘ ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense
                                )
                                Text(
                                    text = "${firstShortfall.account.localizedName(languageMode)} ${if (languageMode == LanguageMode.BANGLA) "হিসাবে ঘাটতি" else "is short by"} ${LanguageHelper.formatCurrency(firstShortfall.shortfall, languageMode)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val moveText = if (suggestion != null) {
                                "${if (languageMode == LanguageMode.BANGLA) "স্থানান্তর করুন" else "Move"} ${LanguageHelper.formatCurrency(suggestion.transferAmount, languageMode)} ${if (languageMode == LanguageMode.BANGLA) "হতে" else "from"} ${suggestion.fromAccount.localizedName(languageMode)}"
                            } else {
                                if (languageMode == LanguageMode.BANGLA) "ঘাটতি মেটাতে তহবিল স্থানান্তর প্রয়োজন" else "Transfer funds to cover this shortfall"
                            }
                            Text(
                                text = moveText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = {
                                if (suggestion != null) onExecuteTransferSuggestion(suggestion)
                                else onOpenSuggestedTransfers()
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SolidExpense,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_transfer_shortfall")
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "স্থানান্তর" else "Transfer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Alert 2: Unassigned Alert Banner (if any items/categories lack assigned source)
        val unassignedCategories = overview.categoryAllocations.filter { it.accountSplits.isEmpty() }
        val unassignedOtherAccs = overview.otherAccountAllocations.filter { it.accountSplits.isEmpty() }
        val totalUnassignedCount = unassignedCategories.size + unassignedOtherAccs.size
        if (totalUnassignedCount > 0) {
            item {
                val firstUnassignedName = unassignedCategories.firstOrNull()?.category?.localizedName(languageMode)
                    ?: unassignedOtherAccs.firstOrNull()?.account?.localizedName(languageMode)
                    ?: "Utilities"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert_unassigned_banner"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚠ ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "$totalUnassignedCount ${if (totalUnassignedCount > 1) (if (languageMode == LanguageMode.BANGLA) "টি আইটেমে কোনো সোর্স নেই" else "items have no source") else (if (languageMode == LanguageMode.BANGLA) "টি আইটেমে কোনো সোর্স নেই" else "item has no source")}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$firstUnassignedName ${if (languageMode == LanguageMode.BANGLA) "ফলব্যাক একাউন্ট ব্যবহার করছে" else "uses a fallback account"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = onOpenSourceSelector,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_assign_source")
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দ" else "Assign",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 6. Sources Header: "Sources" (left) & "Tap to expand" (right)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সোর্সসমূহ" else "Sources",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে বিস্তারিত দেখুন" else "Tap to expand",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // 7. Source Accounts Cards List
        if (filteredAccounts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো পেমেন্ট সোর্স একাউন্ট পাওয়া যায়নি।" else "No payment source accounts found.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            itemsIndexed(filteredAccounts, key = { _, acc -> acc.account.id }) { _, accAnalysis ->
                val isExpanded = expandedAccountIds[accAnalysis.account.id] ?: false
                val isShortfall = accAnalysis.isShortfall
                val statusColor = if (isShortfall) SolidExpense else SolidIncome
                val amountText = if (isShortfall) "-${LanguageHelper.formatCurrency(accAnalysis.shortfall, languageMode)}"
                else "+${LanguageHelper.formatCurrency(accAnalysis.surplus, languageMode)}"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(180))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            expandedAccountIds[accAnalysis.account.id] = !isExpanded
                        }
                        .testTag("source_card_${accAnalysis.account.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Top Row: Status Dot, Account Name, Balance Amount, and Expand Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Dot (red if shortfall, green if surplus)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = accAnalysis.account.localizedName(languageMode),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = amountText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal Status Color Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(statusColor)
                        )

                        // Expanded Content: 2x2 Grid + Action Row
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // 2x2 Grid: Balance, Required, Expected Income, Projected
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            // Left Column
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স" else "Balance",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = LanguageHelper.formatCurrency(accAnalysis.currentBalance, languageMode),
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত আয়" else "Expected income",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "+${LanguageHelper.formatCurrency(accAnalysis.expectedIncomeAmount, languageMode)}",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SolidIncome
                                                )
                                            }

                                            // Right Column
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয়" else "Required",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "-${LanguageHelper.formatCurrency(accAnalysis.requiredExpenseAmount, languageMode)}",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SolidExpense
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "আনুমানিক" else "Projected",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                val projNet = accAnalysis.projectedBalance
                                                val projSign = if (projNet >= 0) "+" else "-"
                                                val projColor = if (projNet >= 0) SolidIncome else SolidExpense
                                                Text(
                                                    text = "$projSign${LanguageHelper.formatCurrency(kotlin.math.abs(projNet), languageMode)}",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = projColor
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Action Row inside Expanded Card: "View items (N) →" and "+ Assign"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val itemCount = accAnalysis.itemizedExpenses.size + accAnalysis.itemizedIncomes.size
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onAccountClick(accAnalysis.account.id) }
                                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${if (languageMode == LanguageMode.BANGLA) "আইটেম দেখুন" else "View items"} ($itemCount) →",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0284C7)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { onAssignItem(accAnalysis.account) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দ" else "Assign",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
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
        }

        // 8. Footer Row: "All assigned items (N) →" (left) & "Select sources" (right)
        item {
            val totalAssignedCount = overview.otherAccountAllocations.size + overview.categoryAllocations.size + overview.incomeAllocations.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToAssignedItems() }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .testTag("link_all_assigned_items"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "সকল বরাদ্দকৃত আইটেম" else "All assigned items"} ($totalAssignedCount) →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                }

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সোর্স নির্বাচন" else "Select sources",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenSourceSelector() }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("link_select_sources")
                )
            }
        }
    }
}
