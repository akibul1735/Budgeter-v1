package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlin.math.abs

private data class ExcludedItem(
    val account: Account,
    val baseBalance: Double,
    val calculatedBalance: Double,
    val isFullyExcluded: Boolean,
    val isAdjusted: Boolean,
    val excludedAmount: Double
)

@Composable
fun ExcludedAccountsDialog(
    allAccounts: List<Account>,
    transactions: List<Transaction>,
    accountCalcConfig: AccountCalcConfig,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onToggleIncludeStatus: (Account, Boolean) -> Unit,
    onAdjustCalculation: ((Account, Double) -> Unit)? = null,
    onResetAccountCalculation: ((Account) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }

    // Map parent accounts
    val parentMap = remember(allAccounts) {
        allAccounts.associateBy { it.id }
    }

    // Helper to calculate current balance
    fun getAccountBalance(acc: Account): Double {
        var dr = 0.0
        var cr = 0.0
        for (tx in transactions) {
            if (tx.debitAccountId == acc.id) dr += tx.amount
            if (tx.creditAccountId == acc.id) cr += tx.amount
        }
        return when (acc.type) {
            AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
            AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
            AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
        }
    }

    // Map and filter excluded or adjusted accounts
    val excludedItems = remember(allAccounts, accountCalcConfig, searchQuery, transactions) {
        allAccounts.filter { acc ->
            val isExcluded = !accountCalcConfig.isIncluded(acc.id)
            val setting = accountCalcConfig.getSetting(acc.id)
            val isAdjusted = setting.adjustmentAmount != 0.0
            val matchesSearch = searchQuery.isBlank() ||
                acc.nameEn.contains(searchQuery, ignoreCase = true) ||
                acc.nameBn.contains(searchQuery, ignoreCase = true)
            (isExcluded || isAdjusted) && matchesSearch
        }.map { acc ->
            val isExcluded = !accountCalcConfig.isIncluded(acc.id)
            val setting = accountCalcConfig.getSetting(acc.id)
            val baseBal = getAccountBalance(acc)
            val isAdjusted = setting.adjustmentAmount != 0.0
            val calcBal = if (isExcluded) 0.0 else (baseBal + setting.adjustmentAmount)
            // Excluded portion: if fully excluded, the entire baseBal is excluded.
            // If adjusted, the difference between baseBal and calcBal is the excluded/adjusted portion
            val excludedAmt = if (isExcluded) baseBal else -setting.adjustmentAmount
            ExcludedItem(
                account = acc,
                baseBalance = baseBal,
                calculatedBalance = calcBal,
                isFullyExcluded = isExcluded,
                isAdjusted = isAdjusted,
                excludedAmount = excludedAmt
            )
        }
    }

    val totalExcludedCount = remember(allAccounts, accountCalcConfig) {
        allAccounts.count { !accountCalcConfig.isIncluded(it.id) || accountCalcConfig.getSetting(it.id).adjustmentAmount != 0.0 }
    }

    val totalExcludedAmount = remember(allAccounts, accountCalcConfig, transactions) {
        allAccounts.sumOf { acc ->
            val isExcluded = !accountCalcConfig.isIncluded(acc.id)
            val setting = accountCalcConfig.getSetting(acc.id)
            if (isExcluded) {
                getAccountBalance(acc)
            } else if (setting.adjustmentAmount != 0.0) {
                // If user adjusted balance downwards (e.g. from 10k to 8k, adjustment is -2k),
                // 2k is excluded/omitted from net worth
                abs(setting.adjustmentAmount)
            } else {
                0.0
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("excluded_accounts_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া / সমন্বিত অ্যাকাউন্ট" else "Excluded & Adjusted",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "$totalExcludedCount টি অ্যাকাউন্ট • বাদ দেওয়া: ${LanguageHelper.formatCurrency(totalExcludedAmount, languageMode)}"
                            else
                                "$totalExcludedCount accounts • Excluded: ${LanguageHelper.formatCurrency(totalExcludedAmount, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                if (totalExcludedCount > 0) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search accounts...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    )

                    // Action Bar: Include / Reset All
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পুনরায় অন্তর্ভুক্ত করুন:" else "Restore to calculations:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                allAccounts.forEach { acc ->
                                    val isExcluded = !accountCalcConfig.isIncluded(acc.id)
                                    val isAdjusted = accountCalcConfig.getSetting(acc.id).adjustmentAmount != 0.0
                                    if (isExcluded) {
                                        onToggleIncludeStatus(acc, true)
                                    }
                                    if (isAdjusted) {
                                        onResetAccountCalculation?.invoke(acc)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সবগুলো অন্তর্ভুক্ত করুন" else "Include / Reset All",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (excludedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SolidIncome.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো বাদ দেওয়া অ্যাকাউন্ট নেই" else "No Excluded Accounts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "সকল অ্যাকাউন্ট এবং ব্যালেন্স বর্তমানে হিসাবে অন্তর্ভুক্ত রয়েছে।"
                                else
                                    "All accounts and balances are currently included in calculations.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(excludedItems, key = { it.account.id }) { item ->
                            val acc = item.account
                            val parentAcc = acc.parentId?.let { parentMap[it] }
                            val iconVector = IconHelper.getIconByName(acc.iconName)
                            val accColor = IconHelper.parseColorHex(acc.colorHex, MaterialTheme.colorScheme.primary)

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (onAccountClick != null) {
                                            onDismiss()
                                            onAccountClick(acc)
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Account Icon
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(accColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = iconVector,
                                                contentDescription = null,
                                                tint = accColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Account Name and Group info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) acc.nameBn else acc.nameEn,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                // Exclusion / Adjustment Status Tag
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (item.isFullyExcluded) SolidExpense.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (item.isFullyExcluded) {
                                                            if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ বাদ" else "Fully Excluded"
                                                        } else {
                                                            if (languageMode == LanguageMode.BANGLA) "সমন্বিত অংশ" else "Adjusted Portion"
                                                        },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (item.isFullyExcluded) SolidExpense else Color(0xFFD97706),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            if (parentAcc != null) {
                                                Text(
                                                    text = "${if (languageMode == LanguageMode.BANGLA) "গ্রুপ" else "Group"}: ${if (languageMode == LanguageMode.BANGLA) parentAcc.nameBn else parentAcc.nameEn}",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Action Button or Switch
                                        if (item.isFullyExcluded) {
                                            Switch(
                                                checked = false, // It is currently excluded
                                                onCheckedChange = { isChecked ->
                                                    if (isChecked) {
                                                        onToggleIncludeStatus(acc, true)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = SolidIncome,
                                                    checkedTrackColor = SolidIncome.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                // Edit Adjustment button
                                                IconButton(
                                                    onClick = {
                                                        onAdjustCalculation?.invoke(acc, item.baseBalance)
                                                    },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Adjustment",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                // Reset Adjustment button
                                                IconButton(
                                                    onClick = {
                                                        onResetAccountCalculation?.invoke(acc)
                                                    },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.RestartAlt,
                                                        contentDescription = "Reset Adjustment",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Balance Details & Prominent Excluded Amount Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${if (languageMode == LanguageMode.BANGLA) "মূল ব্যালেন্স" else "Actual"}: ${LanguageHelper.formatCurrency(item.baseBalance, languageMode)}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (item.isAdjusted) {
                                                Text(
                                                    text = "${if (languageMode == LanguageMode.BANGLA) "গণনাকৃত" else "Calculated"}: ${LanguageHelper.formatCurrency(item.calculatedBalance, languageMode)}",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        // Prominently show the Excluded Portion Amount
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া পরিমাণ:" else "Excluded Amount:",
                                                fontSize = 10.sp,
                                                color = SolidExpense.copy(alpha = 0.85f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = LanguageHelper.formatCurrency(abs(item.excludedAmount), languageMode),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
