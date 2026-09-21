package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlin.math.abs

@Composable
fun FavoriteAccountsCard(
    accountsWithBalances: List<AccountWithBalance>,
    favoriteAccountIds: List<Long>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    accountActivityTimestamps: Map<Long, Long> = emptyMap(),
    deselectedAccountTimestamps: Map<Long, Long> = emptyMap(),
    languageMode: LanguageMode,
    onOpenAccountPicker: () -> Unit,
    onAccountClick: (Account) -> Unit = {}
) {
    // 1. Extract all active individual accounts (flatten groups)
    val flatActiveAccounts = remember(accountsWithBalances) {
        val list = mutableListOf<AccountWithBalance>()
        for (item in accountsWithBalances) {
            if (item.subAccounts.isNotEmpty()) {
                list.addAll(item.subAccounts.filter { it.account.isActive })
            } else if (item.account.isActive) {
                list.add(item)
            }
        }
        list
    }

    // 2. Compute Manually Added accounts in preserved user order
    val manualAccounts = remember(flatActiveAccounts, favoriteAccountIds) {
        val accountMap = flatActiveAccounts.associateBy { it.account.id }
        favoriteAccountIds.distinct().mapNotNull { accountMap[it] }
    }

    // 3. Compute Auto Added accounts
    // Rule: Recent activity AND non-zero effective balance AND not excluded AND not manually added
    val autoAccounts = remember(flatActiveAccounts, favoriteAccountIds, accountCalcConfig, accountActivityTimestamps, deselectedAccountTimestamps) {
        val favIdSet = favoriteAccountIds.toSet()
        flatActiveAccounts.filter { item ->
            val id = item.account.id
            val isExcluded = !accountCalcConfig.isIncluded(id)
            if (isExcluded || id in favIdSet) {
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

    // 4. Combine manual and auto accounts:
    // Manual accounts strictly follow the user's manual drag order.
    // Auto accounts (if any) are appended, sorted alphabetically.
    val combinedAccounts = remember(manualAccounts, autoAccounts, languageMode) {
        val list = mutableListOf<AccountWithBalance>()
        val seenIds = mutableSetOf<Long>()
        for (acc in manualAccounts) {
            if (seenIds.add(acc.account.id)) {
                list.add(acc)
            }
        }
        val remainingAuto = autoAccounts
            .filter { seenIds.add(it.account.id) }
            .sortedBy { it.account.localizedName(languageMode).lowercase() }
        list.addAll(remainingAuto)
        list
    }

    val totalAccountsCount = combinedAccounts.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("favorite_accounts_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LanguageHelper.getString("favorite_accounts", languageMode),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (totalAccountsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "$totalAccountsCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onOpenAccountPicker,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Select Favorite Accounts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (combinedAccounts.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LanguageHelper.getString("no_favorite_accounts", languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenAccountPicker,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LanguageHelper.getString("select_accounts", languageMode),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    combinedAccounts.forEachIndexed { index, accItem ->
                        val effectiveBalance = if (accountCalcConfig.isIncluded(accItem.account.id)) {
                            accountCalcConfig.getEffectiveBalance(accItem.account.id, accItem.currentBalance)
                        } else {
                            accItem.currentBalance
                        }

                        FavoriteAccountRow(
                            accItem = accItem,
                            effectiveBalance = effectiveBalance,
                            languageMode = languageMode,
                            onAccountClick = onAccountClick
                        )

                        if (index < combinedAccounts.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteAccountRow(
    accItem: AccountWithBalance,
    effectiveBalance: Double,
    languageMode: LanguageMode,
    onAccountClick: (Account) -> Unit
) {
    val balanceColor = when {
        effectiveBalance > 0 -> Color(0xFF10B981) // Emerald Green
        effectiveBalance < 0 -> Color(0xFFEF4444) // Red
        else -> MaterialTheme.colorScheme.onSurface // Black/White for 0.00
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onAccountClick(accItem.account) }
            .padding(vertical = 7.dp, horizontal = 4.dp),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                IconHelper.AppIcon(
                    iconName = accItem.account.iconName,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = accItem.account.localizedName(languageMode),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = LanguageHelper.formatCurrency(effectiveBalance, languageMode),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = balanceColor
        )
    }
}
