package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.ui.components.IconPickerModal
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToResearch by remember { mutableStateOf<ItemImageCache?>(null) }
    var itemToEditName by remember { mutableStateOf<ItemImageCache?>(null) }
    var itemToDelete by remember { mutableStateOf<ItemImageCache?>(null) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showCleanUnusedConfirmDialog by remember { mutableStateOf(false) }

    var iconCacheStats by remember { mutableStateOf(IconHelper.IconCacheStats()) }
    var isCleaningCache by remember { mutableStateOf(false) }

    LaunchedEffect(cachedItems) {
        iconCacheStats = viewModel.getIconCacheStats()
    }

    // Filtered cached items
    val filteredItems = remember(cachedItems, searchQuery) {
        if (searchQuery.isBlank()) {
            cachedItems
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            cachedItems.filter {
                it.itemName.lowercase(Locale.ROOT).contains(q) ||
                        it.normalizedItemName.contains(q) ||
                        it.sourceTitle.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    // Distinct transaction item names that are NOT yet cached
    val uncachedTransactionNames = remember(transactions, cachedItems) {
        val cachedNormalized = cachedItems.map { it.normalizedItemName }.toSet()
        transactions.mapNotNull { it.transaction.payeeOrPayer.trim().takeIf { p -> p.isNotBlank() } }
            .distinct()
            .filter { name ->
                val norm = ItemCacheHelper.normalizeItemName(name)
                norm.isNotBlank() && norm !in cachedNormalized
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "আইকন ও ইমেজ ক্যাশ স্টোরেজ" else "Icon & Image Cache Storage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isBangla) "পেছনে" else "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isBangla) "নতুন আইটেম যোগ করুন" else "Add Item to Cache"
                        )
                    }
                    var menuExpanded by remember { mutableStateOf(false) }
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
                                    if (isBangla) "সমস্ত ক্যাশ মুছুন" else "Clear All Cache",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Storage Stats Card
            item {
                StorageStatsCard(
                    stats = iconCacheStats,
                    cachedCount = cachedItems.size,
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
                                if (isBangla) "$cleanedCount টি অব্যবহৃত আইকন মুছে $freedKb KB মেমোরি খালি করা হয়েছে!"
                                else "Cleaned $cleanedCount unused icons! Freed $freedKb KB",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            // 2. Add New Item Action Banner
            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "আইটেম আইকন অ্যাসাইন করুন" else "Assign Item Image / Icon",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = if (isBangla) "লেনদেনের নামের সাথে ছবি যুক্ত করুন" else "Map transaction item names to icons",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "যুক্ত করুন" else "Add Item", fontSize = 13.sp)
                            }
                        }

                        // Uncached transaction suggestions chip row
                        if (uncachedTransactionNames.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBangla) "লেনদেন থেকে প্রস্তাবিত নামসমূহ:" else "Recent transaction item names to cache:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(uncachedTransactionNames.take(8)) { name ->
                                    AssistChip(
                                        onClick = {
                                            // Trigger research dialog immediately for this item
                                            itemToResearch = ItemImageCache(
                                                itemName = name,
                                                normalizedItemName = ItemCacheHelper.normalizeItemName(name),
                                                iconKey = ""
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Search Field & Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "সংরক্ষিত আইটেম তালিকা (${cachedItems.size})" else "Cached Items (${cachedItems.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (isBangla) "আইটেমের নাম দিয়ে খুঁজুন..." else "Search cached items...",
                            fontSize = 13.sp
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

            // 4. Cached Items List
            if (filteredItems.isEmpty()) {
                item {
                    EmptyCachePlaceholder(
                        hasSearch = searchQuery.isNotEmpty(),
                        isBangla = isBangla,
                        onAddClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    CachedItemCard(
                        item = item,
                        isBangla = isBangla,
                        onResearchClick = { itemToResearch = item },
                        onEditNameClick = { itemToEditName = item },
                        onDeleteClick = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Dialog: Add Item to Cache
    if (showAddDialog) {
        AddItemToCacheDialog(
            suggestedNames = uncachedTransactionNames,
            isBangla = isBangla,
            onDismiss = { showAddDialog = false },
            onAddAndPickIcon = { name ->
                showAddDialog = false
                itemToResearch = ItemImageCache(
                    itemName = name,
                    normalizedItemName = ItemCacheHelper.normalizeItemName(name),
                    iconKey = ""
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

    // Dialog: Research / Change Image (uses full IconPickerModal)
    itemToResearch?.let { targetItem ->
        IconPickerModal(
            selectedIconName = targetItem.iconKey,
            initialQuery = targetItem.itemName,
            initialCategory = "Online Search",
            onIconSelected = { selectedKey ->
                viewModel.researchAndReplaceItemImage(
                    itemName = targetItem.itemName,
                    newIconKey = selectedKey,
                    sourceTitle = if (selectedKey.startsWith("custom_icon_")) "Custom Image" else "Selected Icon"
                )
                itemToResearch = null
                Toast.makeText(
                    context,
                    if (isBangla) "\"${targetItem.itemName}\" এর আইকন সফলভাবে পরিবর্তন করা হয়েছে!"
                    else "Icon updated for \"${targetItem.itemName}\"!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { itemToResearch = null }
        )
    }

    // Dialog: Edit Item Name
    itemToEditName?.let { targetItem ->
        var editedName by remember { mutableStateOf(targetItem.itemName) }
        AlertDialog(
            onDismissRequest = { itemToEditName = null },
            title = {
                Text(
                    text = if (isBangla) "আইটেমের নাম পরিবর্তন" else "Edit Item Name",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "এই আইকনটি যে লেনদেনের নামের সাথে প্রযোজ্য হবে তা লিখুন:"
                        else "Enter the transaction item name associated with this icon:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        label = { Text(if (isBangla) "আইটেমের নাম" else "Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editedName.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.saveItemImageCache(
                                itemName = trimmed,
                                iconKey = targetItem.iconKey,
                                source = targetItem.source,
                                sourceTitle = targetItem.sourceTitle,
                                originalQuery = targetItem.originalQuery
                            )
                            if (trimmed != targetItem.itemName) {
                                viewModel.deleteItemImageCacheById(targetItem.id)
                            }
                            itemToEditName = null
                        }
                    },
                    enabled = editedName.isNotBlank()
                ) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEditName = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Delete Single Item Confirm
    itemToDelete?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = if (isBangla) "ক্যাশ থেকে মুছবেন?" else "Remove from Cache?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "\"${targetItem.itemName}\" এর ক্যাশ করা আইকনটি মুছে ফেলতে চান? লেনদেনে আবার ডিফল্ট ক্যাটাগরি আইকন দেখানো হবে।"
                    else
                        "Remove cached image for \"${targetItem.itemName}\"? Transactions with this name will fall back to their category icon."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItemImageCache(targetItem)
                        itemToDelete = null
                        Toast.makeText(
                            context,
                            if (isBangla) "আইটেম ক্যাশ থেকে মুছে ফেলা হয়েছে" else "Item removed from cache",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
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
                        "বর্তমানে কোনো লেনদেন, ক্যাটাগরি বা অ্যাকাউন্টে ব্যবহার হচ্ছে না এমন পুরানো ক্যাশ করা ইমেজ ফাইলগুলো নিরাপদে মুছে ফেলা হবে।"
                    else
                        "Safely delete orphan icon cache files that are no longer assigned to any transactions, categories, accounts, or goals."
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

    // Dialog: Clear All Cache Confirm
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = if (isBangla) "সমস্ত ক্যাশ মুছে ফেলবেন?" else "Clear All Item Cache?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "সমস্ত আইটেমের কাস্টম আইকন অ্যাসাইনমেন্ট মুছে যাবে। লেনদেনের আইটেমগুলো পুনরায় ডিফল্ট ক্যাটাগরি আইকন ব্যবহার করবে।"
                    else
                        "This will remove all item-to-image mappings. All transactions will revert to their category icons."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirmDialog = false
                        viewModel.clearAllItemImageCache()
                        Toast.makeText(
                            context,
                            if (isBangla) "সমস্ত আইটেম ক্যাশ মুছে ফেলা হয়েছে" else "All item cache cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "সব মুছুন" else "Clear All")
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
    cachedCount: Int,
    isBangla: Boolean,
    isCleaning: Boolean,
    onCleanUnused: () -> Unit
) {
    val totalKb = (stats.totalBytes / 1024).coerceAtLeast(if (stats.totalCount > 0) 1 else 0)
    val activeKb = (stats.activeBytes / 1024).coerceAtLeast(if (stats.activeCount > 0) 1 else 0)
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
                        text = if (isBangla) "স্টোরেজ ও মেমোরি বিবরণ" else "Storage & Memory Usage",
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
                        text = if (isBangla) "সংরক্ষিত আইটেম" else "Mapped Items",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$cachedCount items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = if (isBangla) "মোট ডিস্ক মেমোরি" else "Total Disk Space",
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

                Column {
                    Text(
                        text = if (isBangla) "অব্যবহৃত ক্যাশ" else "Unused Cache",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.unusedCount} (~$unusedKb KB)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (stats.unusedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CachedItemCard(
    item: ItemImageCache,
    isBangla: Boolean,
    onResearchClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val formattedDate = remember(item.lastUpdated) {
        try {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(item.lastUpdated))
        } catch (_: Exception) {
            ""
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Avatar Thumbnail with border
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onResearchClick() }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    IconHelper.AppIcon(
                        iconName = item.iconKey,
                        contentDescription = item.itemName,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Name & Source Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.source) {
                            ImageCacheSource.AUTO_SELECTED -> MaterialTheme.colorScheme.secondaryContainer
                            ImageCacheSource.USER_SELECTED -> MaterialTheme.colorScheme.primaryContainer
                            ImageCacheSource.CUSTOM -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ) {
                        Text(
                            text = item.sourceTitle.ifBlank {
                                when (item.source) {
                                    ImageCacheSource.AUTO_SELECTED -> if (isBangla) "অটো সংগৃহীত" else "Auto Found"
                                    ImageCacheSource.USER_SELECTED -> if (isBangla) "ব্যবহারকারী নির্বাচিত" else "User Selected"
                                    ImageCacheSource.CUSTOM -> if (isBangla) "কাস্টম আপলোড" else "Custom Photo"
                                }
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = when (item.source) {
                                ImageCacheSource.AUTO_SELECTED -> MaterialTheme.colorScheme.onSecondaryContainer
                                ImageCacheSource.USER_SELECTED -> MaterialTheme.colorScheme.onPrimaryContainer
                                ImageCacheSource.CUSTOM -> MaterialTheme.colorScheme.onTertiaryContainer
                            }
                        )
                    }

                    if (formattedDate.isNotBlank()) {
                        Text(
                            text = formattedDate,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Quick Actions: Research / Change Image button
            IconButton(
                onClick = onResearchClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = if (isBangla) "ছবি পরিবর্তন করুন" else "Research / Change Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Dropdown
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isBangla) "ছবি পরিবর্তন / অনুসন্ধান" else "Research / Change Image") },
                        leadingIcon = { Icon(Icons.Default.ImageSearch, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onResearchClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBangla) "নাম সম্পাদনা করুন" else "Edit Name") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditNameClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isBangla) "ক্যাশ থেকে মুছুন" else "Remove from Cache",
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
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemToCacheDialog(
    suggestedNames: List<String>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onAddAndPickIcon: (String) -> Unit,
    onAddAndAutoDiscover: (String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }

    val activeName = selectedSuggestion ?: customName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "আইটেম ক্যাশে যোগ করুন" else "Add Item to Icon Cache",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBangla)
                        "যে আইটেম বা প্রতিষ্ঠানের জন্য আইকন নির্ধারণ করতে চান তার নাম লিখুন বা লেনদেন থেকে বেছে নিন:"
                    else
                        "Enter the item or payee name to assign a persistent icon to, or pick from recent transactions:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = customName,
                    onValueChange = {
                        customName = it
                        selectedSuggestion = null
                    },
                    label = { Text(if (isBangla) "আইটেম / পেয়ীর নাম" else "Item / Payee Name") },
                    placeholder = { Text("e.g. LED Bulb, Netflix, Uber, Groceries") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (suggestedNames.isNotEmpty()) {
                    Text(
                        text = if (isBangla) "লেনদেন থেকে প্রস্তাবিত:" else "Suggestions from transactions:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(suggestedNames.take(10)) { suggestion ->
                            val isSelected = selectedSuggestion == suggestion
                            AssistChip(
                                onClick = {
                                    selectedSuggestion = suggestion
                                    customName = suggestion
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (activeName.isNotBlank()) {
                            onAddAndAutoDiscover(activeName)
                        }
                    },
                    enabled = activeName.isNotBlank()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "স্বয়ংক্রিয় খুঁজুন" else "Auto Discover")
                }

                Button(
                    onClick = {
                        if (activeName.isNotBlank()) {
                            onAddAndPickIcon(activeName)
                        }
                    },
                    enabled = activeName.isNotBlank()
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
                    if (isBangla) "কোনো ফলাফল পাওয়া যায়নি" else "No matching cached items"
                } else {
                    if (isBangla) "এখনো কোনো আইটেম ক্যাশ করা হয়নি" else "No item icons cached yet"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = if (hasSearch) {
                    if (isBangla) "অন্য কোনো নাম বা শব্দ দিয়ে অনুসন্ধান করুন।" else "Try searching with another item name or query."
                } else {
                    if (isBangla)
                        "লেনদেনে নতুন আইটেমের নাম লিখলে অ্যাপ স্বয়ংক্রিয়ভাবে অনলাইন থেকে প্রাসঙ্গিক আইকন ক্যাশ করবে, অথবা আপনি নিজেও যোগ করতে পারেন।"
                    else
                        "When you enter transaction item names, the app automatically finds and caches matching icons, or you can add items manually."
                },
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!hasSearch) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "নতুন আইটেম যোগ করুন" else "Add First Item")
                }
            }
        }
    }
}
