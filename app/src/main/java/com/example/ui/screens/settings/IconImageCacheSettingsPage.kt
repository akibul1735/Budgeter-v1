package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ImageCacheSource
import com.example.data.model.ItemImageCache
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.AppTabHeader
import com.example.ui.components.IconPickerModal
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a unique transaction item name across the system with its assigned icon
 */
data class UniqueItemIconEntry(
    val displayName: String,
    val normalizedName: String,
    val cachedItem: ItemImageCache?,
    val assignedIcon: String,
    val usageCount: Int,
    val isCustom: Boolean,
    val categoryName: String? = null
)

private enum class ItemIconFilter {
    ALL,
    CUSTOMIZED,
    DEFAULT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconImageCacheSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBangla = languageMode == LanguageMode.BANGLA

    val cachedItems by viewModel.allItemImageCaches.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ItemIconFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog & Picker states
    var itemToEditIcon by remember { mutableStateOf<UniqueItemIconEntry?>(null) }
    var itemToEditName by remember { mutableStateOf<ItemImageCache?>(null) }
    var itemToResetConfirm by remember { mutableStateOf<UniqueItemIconEntry?>(null) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showCleanUnusedConfirmDialog by remember { mutableStateOf(false) }

    var iconCacheStats by remember { mutableStateOf(IconHelper.IconCacheStats()) }
    var isCleaningCache by remember { mutableStateOf(false) }

    LaunchedEffect(cachedItems) {
        iconCacheStats = viewModel.getIconCacheStats()
    }

    // Build the complete list of all unique item names across transactions and custom cache entries
    val allUniqueItemEntries = remember(transactions, cachedItems, categories) {
        val cacheMap = cachedItems.associateBy { it.normalizedItemName }

        val nameToTxList = mutableMapOf<String, MutableList<TransactionWithDetails>>()
        val nameToDisplayName = mutableMapOf<String, String>()

        // 1. Collect all distinct names from transactions
        transactions.forEach { item ->
            val rawName = item.transaction.payeeOrPayer.trim().ifBlank { item.transaction.note.trim() }
            if (rawName.isNotBlank()) {
                val norm = ItemCacheHelper.normalizeItemName(rawName)
                if (norm.isNotBlank()) {
                    nameToTxList.getOrPut(norm) { mutableListOf() }.add(item)
                    if (!nameToDisplayName.containsKey(norm)) {
                        nameToDisplayName[norm] = rawName
                    }
                }
            }
        }

        // 2. Include all cached items (even if they don't have matching transactions)
        cachedItems.forEach { cached ->
            val norm = cached.normalizedItemName
            if (!nameToDisplayName.containsKey(norm)) {
                nameToDisplayName[norm] = cached.itemName
            }
        }

        // 3. Assemble UniqueItemIconEntry for each distinct item name
        val list = nameToDisplayName.map { (norm, display) ->
            val cached = cacheMap[norm] ?: ItemCacheHelper.findCachedIcon(display, cacheMap)
            val txs = nameToTxList[norm] ?: emptyList()
            val sampleTx = txs.firstOrNull()
            val categoryName = sampleTx?.subCategory?.nameEn ?: sampleTx?.category?.nameEn

            val assignedIcon = if (cached != null && cached.iconKey.isNotBlank()) {
                cached.iconKey
            } else if (sampleTx != null) {
                ItemCacheHelper.resolveTransactionIconName(sampleTx, cacheMap)
            } else {
                "Category"
            }

            UniqueItemIconEntry(
                displayName = display,
                normalizedName = norm,
                cachedItem = cached,
                assignedIcon = assignedIcon,
                usageCount = txs.size,
                isCustom = cached != null && cached.iconKey.isNotBlank(),
                categoryName = categoryName
            )
        }

        // Sort: customized first, then most used, then alphabetical
        list.sortedWith(
            compareByDescending<UniqueItemIconEntry> { it.isCustom }
                .thenByDescending { it.usageCount }
                .thenBy { it.displayName.lowercase(Locale.ROOT) }
        )
    }

    val customCount = remember(allUniqueItemEntries) { allUniqueItemEntries.count { it.isCustom } }
    val defaultCount = remember(allUniqueItemEntries) { allUniqueItemEntries.count { !it.isCustom } }

    // Filtered entries based on Search & Tabs
    val filteredEntries = remember(allUniqueItemEntries, searchQuery, selectedFilter) {
        var result = allUniqueItemEntries

        // Filter by tab
        result = when (selectedFilter) {
            ItemIconFilter.ALL -> result
            ItemIconFilter.CUSTOMIZED -> result.filter { it.isCustom }
            ItemIconFilter.DEFAULT -> result.filter { !it.isCustom }
        }

        // Filter by search query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            result = result.filter { entry ->
                entry.displayName.lowercase(Locale.ROOT).contains(q) ||
                        entry.normalizedName.contains(q) ||
                        (entry.categoryName?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                        (entry.cachedItem?.sourceTitle?.lowercase(Locale.ROOT)?.contains(q) == true)
            }
        }

        result
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppTabHeader(
            title = if (isBangla) "আইটেম আইকন" else "Items Icon",
            tabIcon = Icons.Default.Category,
            showCoinIcon = false,
            onBack = onBack,
            autoHideOnScroll = false,
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isBangla) "নতুন আইটেম যোগ করুন" else "Add Item"
                    )
                }
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "অব্যবহৃত ক্যাশ পরিষ্কার করুন" else "Clean Unused Icons") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showCleanUnusedConfirmDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isBangla) "সমস্ত কাস্টম ক্যাশ রিসেট করুন" else "Reset All Custom Icons",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showClearAllConfirmDialog = true
                            }
                        )
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Storage & Usage Stats Card
            item {
                StorageStatsCard(
                    stats = iconCacheStats,
                    uniqueCount = allUniqueItemEntries.size,
                    customCount = customCount,
                    isBangla = isBangla,
                    isCleaning = isCleaningCache,
                    onCleanUnused = {
                        isCleaningCache = true
                        coroutineScope.launch {
                            val (cleanedCount, freedBytes) = viewModel.clearUnusedIconCache()
                            iconCacheStats = viewModel.getIconCacheStats()
                            isCleaningCache = false
                            val freedKb = (freedBytes / 1024).coerceAtLeast(1)
                            Toast.makeText(
                                context,
                                if (isBangla) "$cleanedCount টি অব্যবহৃত ফাইল মুছে $freedKb KB খালি করা হয়েছে!"
                                else "Cleaned $cleanedCount unused files! Freed $freedKb KB",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isBangla) "আইটেমের নাম বা ক্যাটাগরি দিয়ে খুঁজুন..." else "Search item names or categories...",
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Filter Chips Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == ItemIconFilter.ALL,
                        onClick = { selectedFilter = ItemIconFilter.ALL },
                        label = {
                            Text(
                                if (isBangla) "সকল আইটেম (${allUniqueItemEntries.size})" else "All Items (${allUniqueItemEntries.size})",
                                fontSize = 12.sp
                            )
                        }
                    )
                    FilterChip(
                        selected = selectedFilter == ItemIconFilter.CUSTOMIZED,
                        onClick = { selectedFilter = ItemIconFilter.CUSTOMIZED },
                        label = {
                            Text(
                                if (isBangla) "কাস্টমাইজড ($customCount)" else "Customized ($customCount)",
                                fontSize = 12.sp
                            )
                        }
                    )
                    FilterChip(
                        selected = selectedFilter == ItemIconFilter.DEFAULT,
                        onClick = { selectedFilter = ItemIconFilter.DEFAULT },
                        label = {
                            Text(
                                if (isBangla) "ডিফল্ট ($defaultCount)" else "Default ($defaultCount)",
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // 4. Unique Items List
            if (filteredEntries.isEmpty()) {
                item {
                    EmptyCachePlaceholder(
                        hasSearch = searchQuery.isNotEmpty() || selectedFilter != ItemIconFilter.ALL,
                        isBangla = isBangla,
                        onAddClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredEntries, key = { it.normalizedName }) { entry ->
                    UniqueItemIconCard(
                        entry = entry,
                        isBangla = isBangla,
                        onEditIconClick = { itemToEditIcon = entry },
                        onAutoDiscover = {
                            viewModel.autoDiscoverAndCacheItemIcon(entry.displayName)
                            Toast.makeText(
                                context,
                                if (isBangla) "\"${entry.displayName}\" এর জন্য অনলাইনে ছবি খোঁজা হচ্ছে..."
                                else "Searching online icon for \"${entry.displayName}\"...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onResetClick = {
                            itemToResetConfirm = entry
                        }
                    )
                }
            }
        }
    }

    // Dialog: Add New Item Name to Cache
    if (showAddDialog) {
        AddItemToCacheDialog(
            existingNames = allUniqueItemEntries.map { it.displayName },
            isBangla = isBangla,
            onDismiss = { showAddDialog = false },
            onAddAndPickIcon = { name ->
                showAddDialog = false
                itemToEditIcon = UniqueItemIconEntry(
                    displayName = name,
                    normalizedName = ItemCacheHelper.normalizeItemName(name),
                    cachedItem = null,
                    assignedIcon = "",
                    usageCount = 0,
                    isCustom = false
                )
            },
            onAddAndAutoDiscover = { name ->
                showAddDialog = false
                viewModel.autoDiscoverAndCacheItemIcon(name)
                Toast.makeText(
                    context,
                    if (isBangla) "\"$name\" এর জন্য অনলাইনে ছবি খোঁজা হচ্ছে..." else "Searching online icon for \"$name\"...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Modal: Edit / Pick Icon for Item (uses comprehensive IconPickerModal)
    itemToEditIcon?.let { targetEntry ->
        IconPickerModal(
            selectedIconName = targetEntry.assignedIcon,
            initialQuery = targetEntry.displayName,
            initialCategory = if (targetEntry.isCustom) "Online Search" else "Categories",
            onIconSelected = { selectedKey ->
                viewModel.researchAndReplaceItemImage(
                    itemName = targetEntry.displayName,
                    newIconKey = selectedKey,
                    sourceTitle = if (selectedKey.startsWith("custom_icon_")) "Custom Image" else "Selected Icon"
                )
                itemToEditIcon = null
                Toast.makeText(
                    context,
                    if (isBangla) "\"${targetEntry.displayName}\" এর আইকন সফলভাবে পরিবর্তিত হয়েছে!"
                    else "Icon updated for \"${targetEntry.displayName}\"!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { itemToEditIcon = null }
        )
    }

    // Dialog: Reset Custom Icon to Default
    itemToResetConfirm?.let { targetEntry ->
        AlertDialog(
            onDismissRequest = { itemToResetConfirm = null },
            title = {
                Text(
                    text = if (isBangla) "ডিফল্ট আইকনে রিসেট করবেন?" else "Reset to Default Icon?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "\"${targetEntry.displayName}\" এর জন্য নির্ধারিত কাস্টম আইকন মুছে ফেলতে চান? লেনদেনে আবার ক্যাটাগরির স্বাভাবিক আইকন দেখানো হবে।"
                    else
                        "Revert \"${targetEntry.displayName}\" to its default category icon? The custom assigned icon will be removed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        targetEntry.cachedItem?.let { viewModel.deleteItemImageCache(it) }
                        itemToResetConfirm = null
                        Toast.makeText(
                            context,
                            if (isBangla) "আইকন রিসেট করা হয়েছে" else "Reset to default icon",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "রিসেট করুন" else "Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToResetConfirm = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Clear Unused Cache Confirm
    if (showCleanUnusedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCleanUnusedConfirmDialog = false },
            title = {
                Text(
                    text = if (isBangla) "অব্যবহৃত ক্যাশ পরিষ্কার করুন" else "Clean Unused Icons",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "বর্তমানে কোনো লেনদেন বা ক্যাটাগরিতে ব্যবহার হচ্ছে না এমন পুরানো ক্যাশ করা ইমেজ ফাইলগুলো নিরাপদে মুছে ফেলা হবে।"
                    else
                        "Safely delete orphan icon cache files that are no longer assigned to any transactions or categories."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCleanUnusedConfirmDialog = false
                        isCleaningCache = true
                        coroutineScope.launch {
                            val (cleanedCount, freedBytes) = viewModel.clearUnusedIconCache()
                            iconCacheStats = viewModel.getIconCacheStats()
                            isCleaningCache = false
                            val freedKb = (freedBytes / 1024).coerceAtLeast(1)
                            Toast.makeText(
                                context,
                                if (isBangla) "$cleanedCount টি ফাইল মুছে $freedKb KB খালি করা হয়েছে"
                                else "Cleaned $cleanedCount files ($freedKb KB freed)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text(if (isBangla) "পরিষ্কার করুন" else "Clean Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanUnusedConfirmDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Clear All Custom Cache Confirm
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = if (isBangla) "সমস্ত কাস্টম ক্যাশ মুছবেন?" else "Reset All Custom Icons?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "সমস্ত আইটেমের কাস্টম আইকন অ্যাসাইনমেন্ট মুছে যাবে। লেনদেনের আইটেমগুলো পুনরায় ডিফল্ট ক্যাটাগরি আইকন ব্যবহার করবে।"
                    else
                        "This will remove all custom item-to-image mappings. All transactions will revert to their default category icons."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirmDialog = false
                        viewModel.clearAllItemImageCache()
                        Toast.makeText(
                            context,
                            if (isBangla) "সমস্ত কাস্টম আইকন রিসেট করা হয়েছে" else "All custom icons reset",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "সব মুছুন" else "Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageStatsCard(
    stats: IconHelper.IconCacheStats,
    uniqueCount: Int,
    customCount: Int,
    isBangla: Boolean,
    isCleaning: Boolean,
    onCleanUnused: () -> Unit
) {
    val totalKb = (stats.totalBytes / 1024).coerceAtLeast(if (stats.totalCount > 0) 1 else 0)
    val unusedKb = (stats.unusedBytes / 1024).coerceAtLeast(if (stats.unusedCount > 0) 1 else 0)

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBangla) "আইটেম ও মেমোরি বিবরণ" else "Items & Storage Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (stats.unusedCount > 0) {
                    TextButton(
                        onClick = onCleanUnused,
                        enabled = !isCleaning,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isBangla) "অব্যবহৃত মুছুন" else "Clean Unused",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBangla) "মোট ইউনিক আইটেম" else "Total Unique Items",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$uniqueCount items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = if (isBangla) "কাস্টমাইজড আইকন" else "Customized",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$customCount items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Column {
                    Text(
                        text = if (isBangla) "ডিস্ক ক্যাশ" else "Disk Cache",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "~$totalKb KB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a unique item name, its assigned icon, and an edit icon option
 */
@Composable
private fun UniqueItemIconCard(
    entry: UniqueItemIconEntry,
    isBangla: Boolean,
    onEditIconClick: () -> Unit,
    onAutoDiscover: () -> Unit,
    onResetClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditIconClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Assigned Icon Avatar Thumbnail with Edit badge overlay
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (entry.isCustom) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        1.dp,
                        if (entry.isCustom) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onEditIconClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                ) {
                    IconHelper.AppIcon(
                        iconName = entry.assignedIcon,
                        contentDescription = entry.displayName,
                        modifier = Modifier.fillMaxSize(),
                        tint = if (entry.isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Small pencil icon badge at bottom-right corner
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Name & Status Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (entry.isCustom) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (entry.isCustom) {
                                entry.cachedItem?.sourceTitle?.ifBlank {
                                    if (isBangla) "কাস্টম আইকন" else "Custom Icon"
                                } ?: (if (isBangla) "কাস্টম আইকন" else "Custom Icon")
                            } else {
                                if (isBangla) "ডিফল্ট ক্যাটাগরি" else "Default Icon"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (entry.isCustom) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Usage count
                    if (entry.usageCount > 0) {
                        Text(
                            text = if (isBangla) "${entry.usageCount} টি লেনদেন" else "${entry.usageCount} txns",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else if (!entry.categoryName.isNullOrBlank()) {
                        Text(
                            text = entry.categoryName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Edit Icon Quick Action Button
            OutlinedButton(
                onClick = onEditIconClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBangla) "আইকন পরিবর্তন" else "Edit Icon",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isBangla) "আইকন পরিবর্তন করুন" else "Change / Pick Icon") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditIconClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBangla) "অনলাইনে স্বয়ংক্রিয় খুঁজুন" else "Auto Discover Online") },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onAutoDiscover()
                        }
                    )
                    if (entry.isCustom) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isBangla) "ডিফল্ট আইকনে রিসেট করুন" else "Reset to Default Icon",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onResetClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddItemToCacheDialog(
    existingNames: List<String>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onAddAndPickIcon: (String) -> Unit,
    onAddAndAutoDiscover: (String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    val trimmedName = customName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "নতুন আইটেমে আইকন দিন" else "Assign Icon to New Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBangla)
                        "যে আইটেম বা প্রতিষ্ঠানের জন্য আইকন নির্ধারণ করতে চান তার নাম লিখুন:"
                    else
                        "Enter the item or payee name you want to assign a custom icon to:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(if (isBangla) "আইটেম / পেয়ীর নাম" else "Item / Payee Name") },
                    placeholder = { Text("e.g. LED Bulb, Netflix, Uber, Groceries") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (trimmedName.isNotBlank()) {
                            onAddAndAutoDiscover(trimmedName)
                        }
                    },
                    enabled = trimmedName.isNotBlank()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "অনলাইন খুঁজুন" else "Auto Search")
                }

                Button(
                    onClick = {
                        if (trimmedName.isNotBlank()) {
                            onAddAndPickIcon(trimmedName)
                        }
                    },
                    enabled = trimmedName.isNotBlank()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "আইকন বাছুন" else "Pick Icon")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
private fun EmptyCachePlaceholder(
    hasSearch: Boolean,
    isBangla: Boolean,
    onAddClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = if (hasSearch) {
                    if (isBangla) "কোনো ফলাফল পাওয়া যায়নি" else "No matching items found"
                } else {
                    if (isBangla) "এখনো কোনো আইটেমের লেনদেন তৈরি করা হয়নি" else "No transaction items found"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = if (hasSearch) {
                    if (isBangla) "অন্য কোনো নাম বা শব্দ দিয়ে অনুসন্ধান করুন।" else "Try searching with another item name or query."
                } else {
                    if (isBangla)
                        "লেনদেনে আইটেমের নাম লিখলে এখানে সব ইউনিক আইটেম তালিকাভুক্ত হবে এবং প্রতিটি আইটেমের আইকন সহজেই পরিবর্তন করতে পারবেন।"
                    else
                        "When you create transactions with item names, they will appear here, allowing you to easily view and customize their assigned icons."
                },
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBangla) "নতুন আইটেমে আইকন দিন" else "Add Item Name")
            }
        }
    }
}
