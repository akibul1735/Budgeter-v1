package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.AccountObligation
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun AccountsPaymentSourceTabContent(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode,
    searchQuery: String = "",
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
    onDeleteObligation: (String) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Overview Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_source_overview_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স সারসংক্ষেপ" else "Payment Sources Overview",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (overview.isOverallSurplus) {
                                    "${if (languageMode == LanguageMode.BANGLA) "মোট উদ্বৃত্ত" else "Net Surplus"}: +${LanguageHelper.formatCurrency(overview.netStatus, languageMode)}"
                                } else {
                                    "${if (languageMode == LanguageMode.BANGLA) "মোট ঘাটতি" else "Net Shortfall"}: -${LanguageHelper.formatCurrency(overview.totalShortfall, languageMode)}"
                                },
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (overview.accountsNeedingFundsCount > 0) SolidExpense else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (overview.accountsNeedingFundsCount == 0) (if (languageMode == LanguageMode.BANGLA) "সকল সোর্স প্রস্তুত" else "All Funded")
                                else "${overview.accountsNeedingFundsCount} ${if (languageMode == LanguageMode.BANGLA) "টিতে ঘাটতি" else "Need Funds"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overview.accountsNeedingFundsCount > 0) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয় ব্যয়" else "Required Expenses",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 9.sp
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalRequired, languageMode),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট মজুদ ও আয়" else "Total Available & Income",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 9.sp
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalAvailable, languageMode),
                                color = SolidIncome,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Side-by-side action buttons inside the Overview Card
                    val suggestionsCount = overview.transferSuggestions.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Select Payment Sources Button
                        Button(
                            onClick = onOpenSourceSelector,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_sources"),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সোর্স নির্বাচন" else "Select Sources",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 2. Suggested Transfers Button
                        Button(
                            onClick = onOpenSuggestedTransfers,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (suggestionsCount > 0) SolidIncome else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (suggestionsCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_suggested_transfers"),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (suggestionsCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ফান্ড ট্রান্সফার" else "Fund Transfers",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (suggestionsCount > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (suggestionsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = SolidExpense,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$suggestionsCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account Status Filter Chips & Sort
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = accountStatusFilter == AccountStatusFilter.ALL,
                        onClick = { onStatusFilterChange(AccountStatusFilter.ALL) },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সকল (${overview.accountAnalyses.size})" else "All (${overview.accountAnalyses.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = accountStatusFilter == AccountStatusFilter.SHORTFALL_ONLY,
                        onClick = { onStatusFilterChange(AccountStatusFilter.SHORTFALL_ONLY) },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "ঘাটতি (${overview.accountsNeedingFundsCount})" else "Shortfall (${overview.accountsNeedingFundsCount})", fontSize = 11.sp) },
                        leadingIcon = { if (overview.accountsNeedingFundsCount > 0) Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = accountStatusFilter == AccountStatusFilter.SURPLUS_ONLY,
                        onClick = { onStatusFilterChange(AccountStatusFilter.SURPLUS_ONLY) },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্ত (${overview.accountsWithSurplusCount})" else "Surplus (${overview.accountsWithSurplusCount})", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(14.dp)) }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Sort Button with DropdownMenu (Moved to right side)
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSortMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (sortOption != PaymentSourceSortOption.DEFAULT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (sortOption != PaymentSourceSortOption.DEFAULT) SolidPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = if (sortOption != PaymentSourceSortOption.DEFAULT) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) sortOption.titleBn else sortOption.titleEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sortOption != PaymentSourceSortOption.DEFAULT) SolidPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        PaymentSourceSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (option == sortOption) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                        } else {
                                            Spacer(modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) option.titleBn else option.titleEn,
                                            fontSize = 12.sp,
                                            fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (option == sortOption) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    onSortOptionChange(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Account List Cards
        if (filteredAccounts.isEmpty()) {
            item {
                PaymentSourceEmptyCard(
                    message = if (languageMode == LanguageMode.BANGLA) "কোনো পেমেন্ট সোর্স একাউন্ট পাওয়া যায়নি।" else "No payment source accounts found for this filter."
                )
            }
        } else {
            items(filteredAccounts, key = { it.account.id }) { accAnalysis ->
                AccountRequirementCard(
                    analysis = accAnalysis,
                    overviewTotalAssigned = overview.totalRequired,
                    languageMode = languageMode,
                    onAccountClick = onAccountClick,
                    onAssignItem = onAssignItem
                )
            }
        }
    }
}
