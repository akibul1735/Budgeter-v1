package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.dialogs.SecurityAuthDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import com.example.util.TrashItemType
import com.example.util.TrashedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val trashedItems by viewModel.trashedItems.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<TrashItemType?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var itemToDeletePermanently by remember { mutableStateOf<TrashedItem?>(null) }

    val filteredItems = remember(trashedItems, selectedFilter) {
        if (selectedFilter == null) trashedItems
        else trashedItems.filter { it.type == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ ও রিসাইকেল বিন" else "Trash & Recycle Bin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "${trashedItems.size} টি মুছে ফেলা আইটেম" else "${trashedItems.size} deleted items",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (trashedItems.isNotEmpty()) {
                        TextButton(
                            onClick = { showEmptyTrashDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = SolidExpense)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সব খালি করুন" else "Empty Trash",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("trash_screen")
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${trashedItems.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                val txCount = trashedItems.count { it.type == TrashItemType.TRANSACTION }
                FilterChip(
                    selected = selectedFilter == TrashItemType.TRANSACTION,
                    onClick = { selectedFilter = TrashItemType.TRANSACTION },
                    label = { Text("Transactions ($txCount)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                val accCount = trashedItems.count { it.type == TrashItemType.ACCOUNT }
                FilterChip(
                    selected = selectedFilter == TrashItemType.ACCOUNT,
                    onClick = { selectedFilter = TrashItemType.ACCOUNT },
                    label = { Text("Accounts ($accCount)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ খালি" else "Trash is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "মুছে ফেলা লেনদেন বা অ্যাকাউন্ট এখানে জমা থাকবে এবং পুনরুদ্ধার করা যাবে।"
                            else
                                "Deleted transactions and accounts will appear here and can be restored anytime.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        TrashItemCard(
                            item = item,
                            languageMode = languageMode,
                            onRestore = { viewModel.restoreTrashedItem(item) },
                            onDeletePermanently = { itemToDeletePermanently = item }
                        )
                    }
                }
            }
        }
    }

    // Confirm Empty Trash Dialog (with Security Protection)
    if (showEmptyTrashDialog) {
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ খালি করুন" else "Empty All Trash?",
            message = if (languageMode == LanguageMode.BANGLA)
                "ট্র্যাশের সমস্ত আইটেম চিরতরে মুছে ফেলা হবে। এই কাজ পুনরায় ফিরিয়ে আনা যাবে না।"
            else
                "All items in the trash will be permanently deleted. This action cannot be undone.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "সব মুছুন" else "Empty All",
            isDestructive = true,
            requiresAuth = securityConfig.requireAuthForTrashClear,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                viewModel.emptyTrash()
                showEmptyTrashDialog = false
            },
            onDismiss = { showEmptyTrashDialog = false }
        )
    }

    // Confirm Single Delete Forever Dialog
    if (itemToDeletePermanently != null) {
        AlertDialog(
            onDismissRequest = { itemToDeletePermanently = null },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "চিরতরে মুছে ফেলবেন?" else "Delete permanently?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "'${itemToDeletePermanently?.title}' চিরতরে মুছে ফেলা হবে।"
                    else
                        "'${itemToDeletePermanently?.title}' will be permanently deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDeletePermanently?.let { viewModel.deleteTrashedItemPermanently(it) }
                        itemToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "চিরতরে মুছুন" else "Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeletePermanently = null }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrashItemCard(
    item: TrashedItem,
    languageMode: LanguageMode,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (item.type) {
                                    TrashItemType.TRANSACTION -> SolidPrimary.copy(alpha = 0.12f)
                                    TrashItemType.ACCOUNT -> SolidIncome.copy(alpha = 0.12f)
                                    TrashItemType.CATEGORY -> SolidExpense.copy(alpha = 0.12f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                TrashItemType.TRANSACTION -> Icons.AutoMirrored.Filled.ReceiptLong
                                TrashItemType.ACCOUNT -> Icons.Default.AccountBalanceWallet
                                TrashItemType.CATEGORY -> Icons.Default.Category
                            },
                            contentDescription = null,
                            tint = when (item.type) {
                                TrashItemType.TRANSACTION -> SolidPrimary
                                TrashItemType.ACCOUNT -> SolidIncome
                                TrashItemType.CATEGORY -> SolidExpense
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.subtitle.isNotBlank()) {
                            Text(
                                text = item.subtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (item.amount != null) {
                    Text(
                        text = LanguageHelper.formatCurrency(item.amount, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deleted: ${DateUtils.formatDate(item.deletedAtEpochMs, languageMode)}",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestore,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp), tint = SolidIncome)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পুনরুদ্ধার" else "Restore",
                            fontSize = 11.sp,
                            color = SolidIncome,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDeletePermanently,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Forever",
                            tint = SolidExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
