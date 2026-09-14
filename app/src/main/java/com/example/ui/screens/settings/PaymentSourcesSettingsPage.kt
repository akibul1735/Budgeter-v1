package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.IconHelper
import com.example.util.PaymentSourcePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSourcesSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val paymentSourcePrefs = remember { PaymentSourcePreferences.getInstance(context) }
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val paymentSourceConfig by viewModel.paymentSourceConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var accountToLink by remember { mutableStateOf<Account?>(null) }
    var searchQuery by remember { mutableStateOf("") }

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

    val sourceAccounts = remember(activeLeafAccounts, selectedSourceIds) {
        activeLeafAccounts.filter { it.id in selectedSourceIds }
    }

    val otherAccounts = remember(activeLeafAccounts, selectedSourceIds) {
        activeLeafAccounts.filter { it.id !in selectedSourceIds }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_sources_settings_page")
    ) {
        // App Header
        AppTabHeader(
            title = if (isBangla) "পেমেন্ট সোর্স ও লিঙ্কিং" else "Payment Sources & Linking",
            tabIcon = Icons.Default.Payments,
            onBack = onBack,
            autoHideOnScroll = false
        )

        // Tabs
        val tabs = listOf(
            if (isBangla) "পেমেন্ট সোর্স (${sourceAccounts.size})" else "Source Accounts (${sourceAccounts.size})",
            if (isBangla) "অন্যান্য ও লিঙ্কড (${otherAccounts.size})" else "Other & Linked (${otherAccounts.size})"
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SolidPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Search Bar for Accounts
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    if (isBangla) "অ্যাকাউন্ট খুঁজুন..." else "Search accounts...",
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.outline
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedTabIndex == 0) {
                // Info Summary Card
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "পেমেন্ট সোর্স নির্বাচন" else "Designate Payment Sources",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBangla)
                                        "যেসকল অ্যাকাউন্ট থেকে সরাসরি টাকা লেনদেন বা খরচ হয় সেগুলোকে পেমেন্ট সোর্স হিসেবে চালু রাখুন।"
                                    else
                                        "Toggle accounts that can be used directly as funds sources (Cash, Bank, Mobile Wallet).",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                val filteredAccounts = activeLeafAccounts.filter {
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.nameBn.contains(searchQuery, ignoreCase = true)
                }

                if (filteredAccounts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBangla) "কোনো অ্যাকাউন্ট পাওয়া যায়নি" else "No matching accounts found",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(filteredAccounts, key = { it.id }) { acc ->
                        val isSource = acc.id in selectedSourceIds
                        val parentAccount = acc.parentId?.let { pId -> accounts.firstOrNull { it.id == pId } }

                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSource)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSource) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSource) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(acc.iconName),
                                                contentDescription = null,
                                                tint = if (isSource) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = acc.localizedName(languageMode),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (parentAccount != null) {
                                                Text(
                                                    text = "${parentAccount.localizedName(languageMode)} • ",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (acc.type) {
                                                    AccountType.ASSET -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    AccountType.LIABILITY -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            ) {
                                                Text(
                                                    text = when (acc.type) {
                                                        AccountType.ASSET -> if (isBangla) "সম্পদ" else "Asset"
                                                        AccountType.LIABILITY -> if (isBangla) "দায়" else "Liability"
                                                        else -> acc.type.name
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Switch(
                                    checked = isSource,
                                    onCheckedChange = { checked ->
                                        paymentSourcePrefs.toggleSourceAccount(acc.id, checked, selectedSourceIds)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Tab 2: Linked Other Accounts
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "কাউন্টারপার্টি অ্যাকাউন্ট লিঙ্কিং" else "Counterparty Account Linking",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBangla)
                                        "ক্রেডিট কার্ড, লোন বা পাওনাদার অ্যাকাউন্টের সাথে ডিফল্ট সোর্স ব্যাংক বা ক্যাশ অ্যাকাউন্ট লিঙ্ক করুন।"
                                    else
                                        "Link credit cards or loans to their source payment accounts for automatic tracking.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                val filteredOtherAccounts = otherAccounts.filter {
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.nameBn.contains(searchQuery, ignoreCase = true)
                }

                if (filteredOtherAccounts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBangla) "কোনো অতিরিক্ত অ্যাকাউন্ট নেই" else "No other accounts found",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(filteredOtherAccounts, key = { it.id }) { acc ->
                        val matchingLinks = paymentSourceConfig.accountLinks.filter { it.otherAccountId == acc.id }
                        val linkedSources = remember(matchingLinks, accounts) {
                            matchingLinks.mapNotNull { link -> accounts.firstOrNull { it.id == link.paymentSourceAccountId } }
                        }
                        val firstNote = matchingLinks.firstOrNull()?.relationNote.orEmpty()

                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(acc.iconName),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = acc.localizedName(languageMode),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = when (acc.type) {
                                                    AccountType.ASSET -> if (isBangla) "সম্পদ অ্যাকাউন্ট" else "Asset Account"
                                                    AccountType.LIABILITY -> if (isBangla) "দায় / লোন অ্যাকাউন্ট" else "Liability Account"
                                                    else -> acc.type.name
                                                },
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { accountToLink = acc },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (linkedSources.isEmpty())
                                                (if (isBangla) "লিঙ্ক করুন" else "Link")
                                            else
                                                (if (isBangla) "এডিট লিঙ্ক" else "Edit Links"),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (linkedSources.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = if (isBangla) "লিঙ্কড পেমেন্ট সোর্স:" else "Linked Payment Sources:",
                                        fontSize = 11.sp,
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
                                                        Icons.Default.CheckCircle,
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

                                    if (firstNote.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "“$firstNote”",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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

    // Modal Bottom Sheet for Account Linking
    accountToLink?.let { targetAccount ->
        val matchingLinks = paymentSourceConfig.accountLinks.filter { it.otherAccountId == targetAccount.id }
        val currentLinkedIds = matchingLinks.map { it.paymentSourceAccountId }.toSet()
        val currentNote = matchingLinks.firstOrNull()?.relationNote.orEmpty()

        EditAccountLinksBottomSheet(
            otherAccount = targetAccount,
            availableSourceAccounts = sourceAccounts,
            currentLinkedSourceIds = currentLinkedIds,
            existingNote = currentNote,
            languageMode = languageMode,
            onDismiss = { accountToLink = null },
            onSaveLinks = { selectedIds, note ->
                paymentSourcePrefs.saveLinksForOtherAccount(
                    otherAccountId = targetAccount.id,
                    sourceAccountIds = selectedIds.toList(),
                    note = note
                )
                accountToLink = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountLinksBottomSheet(
    otherAccount: Account,
    availableSourceAccounts: List<Account>,
    currentLinkedSourceIds: Set<Long>,
    existingNote: String,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSaveLinks: (Set<Long>, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedIds = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            currentLinkedSourceIds.forEach { put(it, true) }
        }
    }
    var noteText by remember { mutableStateOf(existingNote) }
    var searchSourceQuery by remember { mutableStateOf("") }
    val isBangla = languageMode == LanguageMode.BANGLA

    val filteredSources = remember(availableSourceAccounts, searchSourceQuery) {
        if (searchSourceQuery.isBlank()) {
            availableSourceAccounts
        } else {
            availableSourceAccounts.filter {
                it.nameEn.contains(searchSourceQuery, ignoreCase = true) ||
                it.nameBn.contains(searchSourceQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isBangla) "পেমেন্ট সোর্স লিঙ্ক করুন" else "Link Payment Sources",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = otherAccount.localizedName(languageMode),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search box inside picker
            OutlinedTextField(
                value = searchSourceQuery,
                onValueChange = { searchSourceQuery = it },
                placeholder = { Text(if (isBangla) "সোর্স অ্যাকাউন্ট খুঁজুন..." else "Search source accounts...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBangla) "যেসকল সোর্স থেকে পেমেন্ট হবে (একাধিক সম্ভব):" else "Select linked sources (multiple allowed):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSources, key = { it.id }) { src ->
                    val isChecked = selectedIds[src.id] == true
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(src.iconName),
                                    contentDescription = null,
                                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = src.localizedName(languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
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
                label = { Text(if (isBangla) "নোট / সম্পর্কের বিবরণ" else "Link Note (Optional)", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val picked = selectedIds.filter { it.value }.keys.toSet()
                        onSaveLinks(picked, noteText.trim())
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save Links")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
