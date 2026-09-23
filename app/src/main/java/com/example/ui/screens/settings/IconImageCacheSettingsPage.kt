package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ImageCacheSource
import com.example.data.model.ItemImageCache
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.data.remote.OnlineIconResult
import com.example.data.remote.OnlineImageResult
import com.example.data.remote.OnlineIconSearchService
import com.example.ui.components.AppTabHeader
import com.example.ui.components.IconCropEditorModal
import com.example.ui.components.IconPickerModal
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ItemsIconTab(val titleEn: String, val titleBn: String, val icon: ImageVector) {
    ITEMS("Items", "আইটেম তালিকা", Icons.Default.Category),
    CUSTOM("My Icons", "আমার আইকন", Icons.Default.PhotoLibrary),
    ICONS("Icons", "অনলাইন আইকন", Icons.Default.Public),
    IMAGES("Images", "ছবি ও স্টুডিও", Icons.Default.AddPhotoAlternate)
}

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

    var selectedPageTab by remember { mutableStateOf(ItemsIconTab.ITEMS) }

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

    // Online Icons tab states
    var iconSearchQuery by remember { mutableStateOf("") }
    var iconSearchResults by remember { mutableStateOf<List<OnlineIconResult>>(emptyList()) }
    var isSearchingIcons by remember { mutableStateOf(false) }
    var iconSearchError by remember { mutableStateOf<String?>(null) }
    var iconSearchPage by remember { mutableIntStateOf(1) }
    var hasMoreIcons by remember { mutableStateOf(false) }
    var isDownloadingIconUrl by remember { mutableStateOf<String?>(null) }

    // Online Images tab states
    var imageSearchQuery by remember { mutableStateOf("") }
    var imageSearchResults by remember { mutableStateOf<List<OnlineImageResult>>(emptyList()) }
    var isSearchingImages by remember { mutableStateOf(false) }
    var imageSearchError by remember { mutableStateOf<String?>(null) }
    var imageSearchPage by remember { mutableIntStateOf(1) }
    var hasMoreImages by remember { mutableStateOf(false) }

    // Crop editor states
    var cropEditorImageUrl by remember { mutableStateOf<String?>(null) }
    var cropEditorIconKey by remember { mutableStateOf<String?>(null) }
    var cropEditorInitialName by remember { mutableStateOf<String?>("Custom Image") }
    var showCropEditor by remember { mutableStateOf(false) }

    // Custom icons list & management states
    var customIconsList by remember { mutableStateOf<List<File>>(emptyList()) }
    var customIconsSearchQuery by remember { mutableStateOf("") }
    var iconToDelete by remember { mutableStateOf<String?>(null) }

    fun refreshCustomIconsAndStats() {
        customIconsList = IconHelper.getCustomIcons(context)
        coroutineScope.launch {
            iconCacheStats = viewModel.getIconCacheStats()
        }
    }

    // Gallery picker launcher
    val onlineGalleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cropEditorImageUrl = it.toString()
            cropEditorInitialName = "Custom Gallery Image"
            showCropEditor = true
        }
    }

    // Direct File Manager launcher
    val onlineDirectFileManagerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            cropEditorImageUrl = uri.toString()
            cropEditorInitialName = "Custom File Image"
            showCropEditor = true
        }
    }

    val openOnlineGallery = {
        onlineGalleryPickerLauncher.launch("image/*")
    }

    val openOnlineDirectFileManager = {
        val openDocIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "image/*",
                    "image/png",
                    "image/jpeg",
                    "image/jpg",
                    "image/webp",
                    "image/svg+xml",
                    "image/gif",
                    "image/bmp"
                )
            )
        }
        try {
            onlineDirectFileManagerLauncher.launch(openDocIntent)
        } catch (_: Exception) {
            onlineGalleryPickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(cachedItems) {
        iconCacheStats = viewModel.getIconCacheStats()
    }

    LaunchedEffect(iconSearchQuery, selectedPageTab) {
        if (selectedPageTab == ItemsIconTab.ICONS) {
            refreshCustomIconsAndStats()
            if (iconSearchQuery.trim().length >= 2) {
                isSearchingIcons = true
                iconSearchError = null
                iconSearchPage = 1
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchIcons(iconSearchQuery.trim(), page = 1)
                    }
                    iconSearchResults = res
                    hasMoreIcons = res.isNotEmpty()
                } catch (e: Exception) {
                    iconSearchError = e.message ?: "Failed to search icons"
                } finally {
                    isSearchingIcons = false
                }
            } else if (iconSearchQuery.isBlank()) {
                isSearchingIcons = true
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchIcons("bank payment shopping tech food travel", page = 1)
                    }
                    iconSearchResults = res
                    hasMoreIcons = true
                } catch (_: Exception) {
                } finally {
                    isSearchingIcons = false
                }
            }
        }
    }

    LaunchedEffect(imageSearchQuery, selectedPageTab) {
        if (selectedPageTab == ItemsIconTab.IMAGES) {
            refreshCustomIconsAndStats()
            if (imageSearchQuery.trim().length >= 2) {
                isSearchingImages = true
                imageSearchError = null
                imageSearchPage = 1
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchImages(imageSearchQuery.trim(), page = 1)
                    }
                    imageSearchResults = res
                    hasMoreImages = res.isNotEmpty()
                } catch (e: Exception) {
                    imageSearchError = e.message ?: "Failed to search images"
                } finally {
                    isSearchingImages = false
                }
            } else if (imageSearchQuery.isBlank()) {
                isSearchingImages = true
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchImages("finance grocery restaurant office", page = 1)
                    }
                    imageSearchResults = res
                    hasMoreImages = true
                } catch (_: Exception) {
                } finally {
                    isSearchingImages = false
                }
            }
        }
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

    val activeCustomIconKeys = remember(cachedItems, categories) {
        val catIcons = categories.map { it.iconName }
        val itemIcons = cachedItems.map { it.iconKey }
        (catIcons + itemIcons).filter { IconHelper.isCustomIcon(it) }.toSet()
    }

    val filteredCustomIcons = remember(customIconsList, customIconsSearchQuery) {
        if (customIconsSearchQuery.isBlank()) {
            customIconsList
        } else {
            val q = customIconsSearchQuery.trim().lowercase(Locale.ROOT)
            customIconsList.filter { it.name.lowercase(Locale.ROOT).contains(q) }
        }
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
                if (selectedPageTab == ItemsIconTab.ITEMS) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isBangla) "নতুন আইটেম যোগ করুন" else "Add Item"
                        )
                    }
                } else {
                    IconButton(onClick = { openOnlineGallery() }) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = if (isBangla) "ছবি বা আইকন যোগ করুন" else "Add Image/Icon"
                        )
                    }
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

        PrimaryTabRow(
            selectedTabIndex = selectedPageTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            ItemsIconTab.values().forEach { tab ->
                val isSelected = selectedPageTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedPageTab = tab },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) tab.titleBn else tab.titleEn,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                )
            }
        }

        when (selectedPageTab) {
            ItemsIconTab.ITEMS -> {
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
                        onCropAndAdjustClick = {
                            cropEditorIconKey = entry.assignedIcon
                            cropEditorImageUrl = null
                            cropEditorInitialName = entry.displayName
                            showCropEditor = true
                        },
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

            ItemsIconTab.CUSTOM -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header card
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "আমার কাস্টম আইকন স্টুডিও" else "My Custom Icons Studio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isBangla) "আপনার বিদ্যমান আইকনগুলো সরাসরি ক্রপ, ফিল্টার, এডিট বা পরিবর্তন করুন"
                                        else "View, crop, rotate, filter, edit or manage your existing custom icons",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons: Gallery & File Manager + Clean
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { openOnlineGallery() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "গ্যালারি থেকে" else "From Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { openOnlineDirectFileManager() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBangla) "ফাইল ম্যানেজার" else "File Manager", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Search field
                    item {
                        OutlinedTextField(
                            value = customIconsSearchQuery,
                            onValueChange = { customIconsSearchQuery = it },
                            placeholder = {
                                Text(
                                    if (isBangla) "কাস্টম আইকন খুঁজুন..." else "Search custom icons...",
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (customIconsSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { customIconsSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Custom icons count and clean stats
                    item {
                        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "মোট সংরক্ষিত আইকন: ${filteredCustomIcons.size} টি (~$totalKb KB)"
                                else "Saved Custom Icons: ${filteredCustomIcons.size} (~$totalKb KB)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (iconCacheStats.unusedCount > 0) {
                                TextButton(
                                    onClick = { showCleanUnusedConfirmDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBangla) "অব্যবহৃত মুছুন (${iconCacheStats.unusedCount})"
                                        else "Clean Unused (${iconCacheStats.unusedCount})",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Custom Icons List
                    if (filteredCustomIcons.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (customIconsSearchQuery.isNotBlank()) {
                                            if (isBangla) "কোনো কাস্টম আইকন পাওয়া যায়নি" else "No matching custom icons found"
                                        } else {
                                            if (isBangla) "এখনো কোনো কাস্টম আইকন সংরক্ষিত নেই" else "No custom icons saved yet"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = if (isBangla) "গ্যালারি, ফাইল ম্যানেজার বা অনলাইন সার্চ থেকে ছবি নিয়ে ক্রপ ও কাস্টমাইজ করে এখানে সেভ করতে পারেন।"
                                        else "Pick images from Gallery, File Manager, or Online Search, crop and customize them into custom icons.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(onClick = { openOnlineGallery() }) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBangla) "গ্যালারি থেকে আইকন তৈরি করুন" else "Create from Gallery")
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredCustomIcons, key = { it.name }) { file ->
                            val iconKey = file.nameWithoutExtension
                            val isActive = activeCustomIconKeys.contains(iconKey) || activeCustomIconKeys.contains(file.name)
                            val sizeKb = (file.length() / 1024).coerceAtLeast(1)
                            val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
                            val dateStr = remember(file) { dateFormat.format(Date(file.lastModified())) }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    // Custom Icon Preview Image
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                cropEditorIconKey = iconKey
                                                cropEditorImageUrl = null
                                                cropEditorInitialName = iconKey
                                                showCropEditor = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = file,
                                            contentDescription = "Custom Icon",
                                            modifier = Modifier.size(40.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
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
                                                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = if (isActive) (if (isBangla) "সক্রিয় ব্যবহৃত" else "In Use")
                                                    else (if (isBangla) "অব্যবহৃত" else "Unused"),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = "$sizeKb KB • $dateStr",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    // Action Buttons: Edit / Crop and Delete
                                    FilledTonalButton(
                                        onClick = {
                                            cropEditorIconKey = iconKey
                                            cropEditorImageUrl = null
                                            cropEditorInitialName = iconKey
                                            showCropEditor = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Crop,
                                            contentDescription = "Edit / Crop",
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBangla) "এডিট" else "Edit",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { iconToDelete = iconKey },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ItemsIconTab.ICONS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header info card
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "অনলাইন আইকন ও ব্র্যান্ড লাইব্রেরি" else "Online Icon & Brand Library",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isBangla) "ব্র্যান্ডের আসল লোগো ও ভেক্টর আইকন সরাসরি সার্চ ও সেভ করুন" else "Search authentic brand logos, vector symbols & save to custom cache",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Storage & Clean card
                    item {
                        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
                        val unusedKb = (iconCacheStats.unusedBytes / 1024).coerceAtLeast(if (iconCacheStats.unusedCount > 0) 1 else 0)

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isBangla) "আইকন ক্যাশ মেমোরি" else "Icon Storage & Cache",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${iconCacheStats.totalCount} icons (~$totalKb KB) • ${iconCacheStats.activeCount} in-use",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (iconCacheStats.unusedCount > 0) {
                                        Button(
                                            onClick = {
                                                isCleaningCache = true
                                                coroutineScope.launch {
                                                    val (count, _) = viewModel.clearUnusedIconCache()
                                                    refreshCustomIconsAndStats()
                                                    isCleaningCache = false
                                                    Toast.makeText(
                                                        context,
                                                        if (isBangla) "$count টি অব্যবহৃত ক্যাশ আইকন মুছে ফেলা হয়েছে" else "Cleaned $count unused cache items",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            enabled = !isCleaningCache,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text(
                                                text = if (isBangla) "ক্যাশ মুছুন ($unusedKb KB)" else "Clear Cache ($unusedKb KB)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isBangla) "ক্যাশ ক্লিন" else "Clean",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search field
                    item {
                        OutlinedTextField(
                            value = iconSearchQuery,
                            onValueChange = { iconSearchQuery = it },
                            placeholder = {
                                Text(
                                    if (isBangla) "ব্র্যান্ড বা আইকন খুঁজুন (যেমন: Bkash, Netflix, Amazon)..."
                                    else "Search logo or icon (e.g. Bkash, Netflix, Amazon)...",
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (iconSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { iconSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Preset suggestions chips
                    item {
                        val iconPresets = listOf(
                            "Bkash", "Nagad", "Rocket", "Upay", "Google", "Amazon",
                            "Netflix", "Spotify", "Uber", "Pathao", "Daraz", "Food",
                            "Medical", "Salary", "Shopping", "Gym", "Car", "Bank"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(iconPresets) { preset ->
                                val isCurrent = iconSearchQuery.equals(preset, ignoreCase = true)
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = { iconSearchQuery = preset },
                                    label = { Text(preset, fontSize = 11.5.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Results state
                    if (isSearchingIcons) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            }
                        }
                    } else if (iconSearchError != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = iconSearchError ?: "Error loading icons",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    } else if (iconSearchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Public,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBangla) "কোনো আইকন পাওয়া যায়নি" else "No icons found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        // 2 items per row
                        val chunkedIcons = iconSearchResults.chunked(2)
                        items(chunkedIcons) { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (icon in rowIcons) {
                                    val isDownloading = isDownloadingIconUrl == icon.imageUrl
                                    OutlinedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(54.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    AsyncImage(
                                                        model = icon.imageUrl,
                                                        contentDescription = icon.title,
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = icon.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = icon.sourceName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        cropEditorImageUrl = icon.imageUrl
                                                        cropEditorInitialName = icon.title
                                                        showCropEditor = true
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Crop,
                                                        contentDescription = "Crop",
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(if (isBangla) "ক্রপ" else "Crop", fontSize = 10.5.sp)
                                                }

                                                FilledTonalButton(
                                                    onClick = {
                                                        isDownloadingIconUrl = icon.imageUrl
                                                        coroutineScope.launch {
                                                            try {
                                                                val savedKey = withContext(Dispatchers.IO) {
                                                                    OnlineIconSearchService.downloadAndSaveIcon(
                                                                        context = context,
                                                                        imageUrl = icon.imageUrl,
                                                                        name = icon.title
                                                                    )
                                                                }
                                                                if (savedKey != null) {
                                                                    refreshCustomIconsAndStats()
                                                                    Toast.makeText(
                                                                        context,
                                                                        if (isBangla) "আইকন সফলভাবে সেভ করা হয়েছে!" else "Icon saved to custom icons!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                } else {
                                                                    Toast.makeText(
                                                                        context,
                                                                        if (isBangla) "আইকন সেভ ব্যর্থ হয়েছে" else "Failed to download icon",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, e.message ?: "Error saving icon", Toast.LENGTH_SHORT).show()
                                                            } finally {
                                                                isDownloadingIconUrl = null
                                                            }
                                                        }
                                                    },
                                                    enabled = !isDownloading,
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    if (isDownloading) {
                                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                                    } else {
                                                        Icon(
                                                            Icons.Default.Download,
                                                            contentDescription = "Save",
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(if (isBangla) "সেভ" else "Save", fontSize = 10.5.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (rowIcons.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (hasMoreIcons) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val nextPage = iconSearchPage + 1
                                            val query = iconSearchQuery.ifBlank { "bank payment shopping tech food travel" }
                                            try {
                                                val nextResults = withContext(Dispatchers.IO) {
                                                    OnlineIconSearchService.searchIcons(query, page = nextPage)
                                                }
                                                if (nextResults.isNotEmpty()) {
                                                    iconSearchResults = iconSearchResults + nextResults
                                                    iconSearchPage = nextPage
                                                } else {
                                                    hasMoreIcons = false
                                                }
                                            } catch (_: Exception) {
                                                hasMoreIcons = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(if (isBangla) "আরো আইকন দেখুন" else "Load More Icons", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            ItemsIconTab.IMAGES -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header info card with Gallery and File Manager buttons
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = if (isBangla) "অনলাইন ইমেজ সার্চ ও এডিটিং স্টুডিও" else "Online Image Search & Studio",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isBangla) "বাস্তবসম্মত ফটো খুঁজুন অথবা ফোন থেকে ছবি ক্রপ করে আইকন বানান" else "Search high-def photos or pick & crop images from your device",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { openOnlineGallery() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBangla) "গ্যালারি" else "Gallery", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { openOnlineDirectFileManager() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isBangla) "ফাইল ম্যানেজার" else "File Manager", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Storage & Clean card
                    item {
                        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
                        val unusedKb = (iconCacheStats.unusedBytes / 1024).coerceAtLeast(if (iconCacheStats.unusedCount > 0) 1 else 0)

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isBangla) "কাস্টম ইমেজ ও ফটো ক্যাশ" else "Custom Image & Photo Cache",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${iconCacheStats.totalCount} cached images (~$totalKb KB)",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (iconCacheStats.unusedCount > 0) {
                                        Button(
                                            onClick = {
                                                isCleaningCache = true
                                                coroutineScope.launch {
                                                    val (count, _) = viewModel.clearUnusedIconCache()
                                                    refreshCustomIconsAndStats()
                                                    isCleaningCache = false
                                                    Toast.makeText(
                                                        context,
                                                        if (isBangla) "$count টি অব্যবহৃত ফাইল মুছে ফেলা হয়েছে" else "Cleaned $count unused image files",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            enabled = !isCleaningCache,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text(
                                                text = if (isBangla) "ক্যাশ মুছুন ($unusedKb KB)" else "Clear Cache ($unusedKb KB)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isBangla) "ক্লিন" else "Clean",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search field
                    item {
                        OutlinedTextField(
                            value = imageSearchQuery,
                            onValueChange = { imageSearchQuery = it },
                            placeholder = {
                                Text(
                                    if (isBangla) "ফটো খুঁজুন (যেমন: Coffee, Travel, Shopping)..."
                                    else "Search photos (e.g. Coffee, Travel, Shopping)...",
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (imageSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { imageSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Preset suggestions chips
                    item {
                        val imagePresets = listOf(
                            "Coffee", "Burger", "Restaurant", "Groceries", "Shopping",
                            "Travel", "Flight", "Hotel", "Car", "Office", "Salary",
                            "Bonus", "Gym", "Medical", "Investment"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(imagePresets) { preset ->
                                val isCurrent = imageSearchQuery.equals(preset, ignoreCase = true)
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = { imageSearchQuery = preset },
                                    label = { Text(preset, fontSize = 11.5.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Results state
                    if (isSearchingImages) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            }
                        }
                    } else if (imageSearchError != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = imageSearchError ?: "Error loading images",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    } else if (imageSearchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isBangla) "কোনো ছবি পাওয়া যায়নি" else "No images found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        // 3 items per row
                        val chunkedImages = imageSearchResults.chunked(3)
                        items(chunkedImages) { rowImages ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (image in rowImages) {
                                    OutlinedCard(
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                cropEditorImageUrl = image.thumbUrl.ifBlank { image.imageUrl }
                                                cropEditorInitialName = image.title
                                                showCropEditor = true
                                            },
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            ) {
                                                AsyncImage(
                                                    model = image.thumbUrl.ifBlank { image.imageUrl },
                                                    contentDescription = image.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                ) {
                                                    Text(
                                                        text = image.sourceName.take(8),
                                                        fontSize = 8.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = image.title,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 10.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    cropEditorImageUrl = image.thumbUrl.ifBlank { image.imageUrl }
                                                    cropEditorInitialName = image.title
                                                    showCropEditor = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth().height(26.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Crop,
                                                    contentDescription = "Crop",
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(if (isBangla) "ক্রপ ও সেভ" else "Crop & Save", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                                if (rowImages.size < 3) {
                                    for (i in 0 until (3 - rowImages.size)) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        if (hasMoreImages) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val nextPage = imageSearchPage + 1
                                            val query = imageSearchQuery.ifBlank { "finance grocery restaurant office" }
                                            try {
                                                val nextResults = withContext(Dispatchers.IO) {
                                                    OnlineIconSearchService.searchImages(query, page = nextPage)
                                                }
                                                if (nextResults.isNotEmpty()) {
                                                    imageSearchResults = imageSearchResults + nextResults
                                                    imageSearchPage = nextPage
                                                } else {
                                                    hasMoreImages = false
                                                }
                                            } catch (_: Exception) {
                                                hasMoreImages = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(if (isBangla) "আরো ফটো দেখুন" else "Load More Photos", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
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

    // Modal: Image / Icon Crop & Edit Studio
    if (showCropEditor && (cropEditorImageUrl != null || cropEditorIconKey != null)) {
        IconCropEditorModal(
            imageUrl = cropEditorImageUrl,
            initialIconKey = cropEditorIconKey ?: cropEditorInitialName,
            onDismiss = {
                showCropEditor = false
                cropEditorImageUrl = null
                cropEditorInitialName = null
                cropEditorIconKey = null
            },
            onCroppedIconSaved = { savedKey: String ->
                showCropEditor = false
                cropEditorImageUrl = null
                cropEditorInitialName = null
                cropEditorIconKey = null
                refreshCustomIconsAndStats()
                Toast.makeText(
                    context,
                    if (isBangla) "আইকন সফলভাবে সংরক্ষিত ও আপডেট হয়েছে!" else "Saved icon to custom icons!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Dialog: Delete Single Custom Icon
    iconToDelete?.let { targetKey ->
        AlertDialog(
            onDismissRequest = { iconToDelete = null },
            title = {
                Text(
                    text = if (isBangla) "কাস্টম আইকন মুছবেন?" else "Delete Custom Icon?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla) "এই কাস্টম আইকনটি স্টোরেজ থেকে চিরতরে মুছে ফেলা হবে।"
                    else "Are you sure you want to permanently delete this custom icon from storage?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = IconHelper.getCustomIconFile(context, targetKey)
                        if (file.exists()) {
                            file.delete()
                        }
                        iconToDelete = null
                        refreshCustomIconsAndStats()
                        Toast.makeText(
                            context,
                            if (isBangla) "আইকন মুছে ফেলা হয়েছে" else "Icon deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { iconToDelete = null }) {
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
    onCropAndAdjustClick: () -> Unit,
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
                        text = { Text(if (isBangla) "আইকন এডিট ও ক্রপ করুন" else "Edit / Crop Icon Studio") },
                        leadingIcon = { Icon(Icons.Default.Crop, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            menuExpanded = false
                            onCropAndAdjustClick()
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
