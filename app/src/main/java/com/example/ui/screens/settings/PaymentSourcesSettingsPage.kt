package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AccountLink
import com.example.util.IconHelper
import com.example.util.LanguageHelper

private enum class PaymentSourceSettingsTab {
    SOURCE_ACCOUNTS,
    LINKED_OTHER_ACCOUNTS
}

@Composable
fun PaymentSourcesSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val paymentSourceConfig by viewModel.paymentSourceConfig.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(PaymentSourceSettingsTab.SOURCE_ACCOUNTS) }
    var showLinkDialogForAccount by remember { mutableStateOf<Account?>(null) }

    // Active leaf accounts
    val activeLeafAccounts = remember(accounts) {
        val parentIds = accounts.mapNotNull { it.parentId }.toSet()
        accounts.filter { it.isActive && it.id !in parentIds }
    }

    val selectedSourceIds = remember(paymentSourceConfig, activeLeafAccounts) {
        if (paymentSourceConfig.hasCustomizedSelection) {
            paymentSourceConfig.selectedSourceAccountIds
        } else {
            activeLeafAccounts.filter { it.type == AccountType.ASSET }.map { it.id }.toSet()
        }
    }

    val otherAccounts = remember(activeLeafAccounts, selectedSourceIds) {
        activeLeafAccounts.filter { it.id !in selectedSourceIds }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_sources_settings_page"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স ও অ্যাকাউন্ট লিঙ্কিং" else "Payment Sources & Account Linking",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোন অ্যাকাউন্টগুলো পেমেন্ট সোর্স হবে তা নির্ধারণ করুন" else "Designate payment sources & link counterparty accounts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolidPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = SolidPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == PaymentSourceSettingsTab.SOURCE_ACCOUNTS,
                    onClick = { selectedTab = PaymentSourceSettingsTab.SOURCE_ACCOUNTS },
                    text = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স (${selectedSourceIds.size})" else "Payment Sources (${selectedSourceIds.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == PaymentSourceSettingsTab.LINKED_OTHER_ACCOUNTS,
                    onClick = { selectedTab = PaymentSourceSettingsTab.LINKED_OTHER_ACCOUNTS },
                    text = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "অন্যান্য অ্যাকাউন্ট (${otherAccounts.size})" else "Other Accounts (${otherAccounts.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            when (selectedTab) {
                PaymentSourceSettingsTab.SOURCE_ACCOUNTS -> {
                    SourceAccountsTabContent(
                        accounts = activeLeafAccounts,
                        selectedSourceIds = selectedSourceIds,
                        languageMode = languageMode,
                        onToggleSource = { accountId, isSource ->
                            viewModel.togglePaymentSourceAccount(accountId, isSource, selectedSourceIds)
                        },
                        onSelectAllAssets = {
                            val assetIds = activeLeafAccounts.filter { it.type == AccountType.ASSET }.map { it.id }.toSet()
                            viewModel.setPaymentSourceAccountIds(assetIds)
                        }
                    )
                }
                PaymentSourceSettingsTab.LINKED_OTHER_ACCOUNTS -> {
                    OtherAccountsTabContent(
                        otherAccounts = otherAccounts,
                        allAccounts = accounts,
                        sourceAccountIds = selectedSourceIds,
                        accountLinks = paymentSourceConfig.accountLinks,
                        languageMode = languageMode,
                        onOpenLinkDialog = { account ->
                            showLinkDialogForAccount = account
                        },
                        onDeleteLink = { linkId ->
                            viewModel.deleteAccountLink(linkId)
                        }
                    )
                }
            }
        }
    }

    if (showLinkDialogForAccount != null) {
        val targetAcc = showLinkDialogForAccount!!
        val existingLinks = remember(targetAcc, paymentSourceConfig.accountLinks) {
            paymentSourceConfig.accountLinks.filter { it.otherAccountId == targetAcc.id }
        }
        val sourceAccounts = remember(activeLeafAccounts, selectedSourceIds) {
            activeLeafAccounts.filter { it.id in selectedSourceIds }
        }

        EditAccountLinksDialog(
            otherAccount = targetAcc,
            availableSourceAccounts = sourceAccounts,
            currentLinkedSourceIds = existingLinks.map { it.paymentSourceAccountId }.toSet(),
            existingNote = existingLinks.firstOrNull()?.relationNote ?: "",
            languageMode = languageMode,
            onDismiss = { showLinkDialogForAccount = null },
            onSaveLinks = { selectedIds, note ->
                viewModel.saveLinksForOtherAccount(targetAcc.id, selectedIds.toList(), note)
                showLinkDialogForAccount = null
            }
        )
    }
}

@Composable
private fun SourceAccountsTabContent(
    accounts: List<Account>,
    selectedSourceIds: Set<Long>,
    languageMode: LanguageMode,
    onToggleSource: (Long, Boolean) -> Unit,
    onSelectAllAssets: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "পেমেন্ট সোর্স হলো সেই অ্যাকাউন্টগুলো (যেমন: Bank, bKash, Nagad, Cash, Credit Card) যেখান থেকে সরাসরি টাকা খরচ বা জমা হয়।"
                        else
                            "Payment Sources are active accounts (e.g. Bank, bKash, Nagad, Cash, Credit Card) where actual spending or incoming funds occur directly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "উপলব্ধ অ্যাকাউন্ট তালিকা" else "Available Accounts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = onSelectAllAssets,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সব অ্যাসেট নির্বাচন করুন" else "Select All Assets",
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(accounts, key = { it.id }) { acc ->
            val isSelected = selectedSourceIds.contains(acc.id)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSource(acc.id, !isSelected) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(acc.iconName ?: "AccountBalance"),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = acc.localizedName(languageMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${acc.type.name} • ${if (isSelected) (if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স সক্রিয়" else "Payment Source Active") else (if (languageMode == LanguageMode.BANGLA) "অন্যান্য অ্যাকাউন্ট" else "Other Account")}",
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSource(acc.id, it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherAccountsTabContent(
    otherAccounts: List<Account>,
    allAccounts: List<Account>,
    sourceAccountIds: Set<Long>,
    accountLinks: List<AccountLink>,
    languageMode: LanguageMode,
    onOpenLinkDialog: (Account) -> Unit,
    onDeleteLink: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "অন্যান্য অ্যাকাউন্ট (ব্যক্তি, ব্যবসা, লোন ইত্যাদি) এক বা একাধিক পেমেন্ট সোর্সের সাথে লিঙ্ক করা যায়। লেনদেনের সময় দ্রুত লিঙ্ক রেফারেন্স পাওয়া যাবে।"
                        else
                            "Other accounts (parties, vendors, loans) can be linked with one or multiple payment sources for quick multi-source routing & tracking.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        if (otherAccounts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো নন-সোর্স অ্যাকাউন্ট নেই। সব অ্যাকাউন্টই পেমেন্ট সোর্স হিসেবে চিহ্নিত।" else "No non-source accounts found. All accounts are currently designated as payment sources.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            items(otherAccounts, key = { it.id }) { acc ->
                val links = accountLinks.filter { it.otherAccountId == acc.id }
                val linkedSources = links.mapNotNull { lk -> allAccounts.firstOrNull { it.id == lk.paymentSourceAccountId } }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(acc.iconName ?: "Person"),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = acc.localizedName(languageMode),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = acc.type.name,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { onOpenLinkDialog(acc) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (linkedSources.isEmpty()) (if (languageMode == LanguageMode.BANGLA) "লিঙ্ক করুন" else "Link") else (if (languageMode == LanguageMode.BANGLA) "এডিট লিঙ্ক" else "Edit Links"),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (linkedSources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "লিঙ্কড পেমেন্ট সোর্স:" else "Linked Payment Sources:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                linkedSources.forEach { src ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = src.localizedName(languageMode),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
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

@Composable
private fun EditAccountLinksDialog(
    otherAccount: Account,
    availableSourceAccounts: List<Account>,
    currentLinkedSourceIds: Set<Long>,
    existingNote: String,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSaveLinks: (Set<Long>, String) -> Unit
) {
    val selectedIds = remember { mutableStateMapOf<Long, Boolean>().apply {
        currentLinkedSourceIds.forEach { put(it, true) }
    } }
    var noteText by remember { mutableStateOf(existingNote) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সোর্স লিঙ্ক করুন" else "Link Payment Sources",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = otherAccount.localizedName(languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স নির্বাচন করুন (একাধিক সম্ভব):" else "Select Payment Sources (Multiple allowed):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableSourceAccounts, key = { it.id }) { src ->
                        val isChecked = selectedIds[src.id] == true
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIds[src.id] = !isChecked }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = src.localizedName(languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { selectedIds[src.id] = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "সম্পর্কের বিবরণ / নোট" else "Relation Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val picked = selectedIds.filter { it.value }.keys.toSet()
                            onSaveLinks(picked, noteText.trim())
                        }
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ করুন" else "Save Links")
                    }
                }
            }
        }
    }
}
