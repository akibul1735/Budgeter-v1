package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.remote.OnlineIconResult
import com.example.data.remote.OnlineIconSearchService
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IconPickerModal(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val customIcons = remember {
        mutableStateListOf<String>().apply {
            addAll(IconHelper.getAllCustomIcons(context))
        }
    }
    var iconToDelete by remember { mutableStateOf<String?>(null) }

    // Online search state
    var onlineResults by remember { mutableStateOf<List<OnlineIconResult>>(emptyList()) }
    val failedImageUrls = remember { mutableStateListOf<String>() }
    val visibleOnlineResults = remember(onlineResults, failedImageUrls.size) {
        onlineResults.filter { it.imageUrl !in failedImageUrls }
    }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentOnlinePage by remember { mutableStateOf(1) }
    var hasMoreOnline by remember { mutableStateOf(true) }
    var downloadingUrl by remember { mutableStateOf<String?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Trigger online search whenever in "Online Search" tab or user is actively searching
    LaunchedEffect(searchQuery, selectedCategory) {
        val query = searchQuery.trim()
        if (selectedCategory == "Online Search") {
            if (query.length >= 2) {
                isSearchingOnline = true
                failedImageUrls.clear()
                currentOnlinePage = 1
                hasMoreOnline = true
                searchJob?.cancel()
                searchJob = coroutineScope.launch {
                    delay(350)
                    val results = OnlineIconSearchService.searchIcons(context, query, page = 1)
                    onlineResults = results
                    hasMoreOnline = results.size >= 4
                    isSearchingOnline = false
                }
            } else {
                onlineResults = emptyList()
                failedImageUrls.clear()
                isSearchingOnline = false
                currentOnlinePage = 1
                hasMoreOnline = false
            }
        }
    }

    // Media picker launcher for custom PNG/images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedIconKey = IconHelper.saveCustomIconFromUri(context, uri)
            if (savedIconKey != null) {
                if (!customIcons.contains(savedIconKey)) {
                    customIcons.add(0, savedIconKey)
                }
                selectedCategory = "Custom"
                onIconSelected(savedIconKey)
                Toast.makeText(context, "Custom icon added!", Toast.LENGTH_SHORT).show()
                onDismiss()
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.67f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SolidPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconHelper.AppIcon(
                                iconName = selectedIconName,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = SolidPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Choose Icon",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (IconHelper.isCustomIcon(selectedIconName)) "Custom Icon" else selectedIconName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (selectedCategory == "Online Search") "Search brands, logos & icons online..." else "Search icons (e.g. food, car, bank...)",
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        if (selectedCategory == "Online Search") {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp), tint = SolidPrimary)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(IconHelper.CATEGORIES) { cat ->
                        val isSelected = cat == selectedCategory && searchQuery.isBlank()
                        val countLabel = if (cat == "Custom") " (${customIcons.size})" else ""
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = cat
                                searchQuery = ""
                            },
                            leadingIcon = if (cat == "Online Search") {
                                {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else null,
                            label = {
                                Text(
                                    text = "$cat$countLabel",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = SolidPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar for Custom Image or Quick Online Search Switch
                if (selectedCategory == "Custom" || selectedCategory == "All") {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Icon from PNG / Image", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // If in Online Search mode
                if (selectedCategory == "Online Search") {
                    if (isSearchingOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = SolidPrimary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Searching online icons...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (searchQuery.trim().length < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = SolidPrimary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Search Any Logo or Icon Online",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Type any company, bank, or keyword (e.g. 'Netflix', 'bKash', 'Coffee', 'Gym', 'Groceries') to search and download high-quality icons directly.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (visibleOnlineResults.isEmpty() && !isSearchingOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No online icons found for \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(visibleOnlineResults) { item ->
                                val isDownloading = downloadingUrl == item.imageUrl
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !isDownloading) {
                                            downloadingUrl = item.imageUrl
                                            coroutineScope.launch {
                                                val savedKey = OnlineIconSearchService.downloadAndSaveIcon(
                                                    context = context,
                                                    imageUrl = item.imageUrl
                                                )
                                                downloadingUrl = null
                                                if (savedKey != null) {
                                                    if (!customIcons.contains(savedKey)) {
                                                        customIcons.add(0, savedKey)
                                                    }
                                                    onIconSelected(savedKey)
                                                    Toast.makeText(context, "Icon saved & selected!", Toast.LENGTH_SHORT).show()
                                                    onDismiss()
                                                } else {
                                                    Toast.makeText(context, "Failed to download icon", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        if (isDownloading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = SolidPrimary,
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            AsyncImage(
                                                model = item.imageUrl,
                                                contentDescription = item.title,
                                                onError = {
                                                    if (!failedImageUrls.contains(item.imageUrl)) {
                                                        failedImageUrls.add(item.imageUrl)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.title,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            if (visibleOnlineResults.isNotEmpty()) {
                                item(span = { GridItemSpan(4) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoadingMore) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = SolidPrimary,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Loading more icons...",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        } else if (hasMoreOnline) {
                                            OutlinedButton(
                                                onClick = {
                                                    val query = searchQuery.trim()
                                                    if (query.isNotBlank() && !isLoadingMore) {
                                                        isLoadingMore = true
                                                        coroutineScope.launch {
                                                            val nextPage = currentOnlinePage + 1
                                                            val moreResults = OnlineIconSearchService.searchIcons(context, query, page = nextPage)
                                                            if (moreResults.isNotEmpty()) {
                                                                val currentUrls = onlineResults.map { it.imageUrl }.toSet()
                                                                val filteredNew = moreResults.filter { it.imageUrl !in currentUrls }
                                                                if (filteredNew.isNotEmpty()) {
                                                                    onlineResults = onlineResults + filteredNew
                                                                    currentOnlinePage = nextPage
                                                                } else {
                                                                    hasMoreOnline = false
                                                                }
                                                            } else {
                                                                hasMoreOnline = false
                                                            }
                                                            isLoadingMore = false
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Load More Icons",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SolidPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Filtered icons for built-in categories
                    val filteredBuiltins = remember(searchQuery, selectedCategory) {
                        val query = searchQuery.trim().lowercase()
                        IconHelper.BUILTIN_ICONS.filter { item ->
                            val matchesSearch = query.isEmpty() ||
                                    item.name.lowercase().contains(query) ||
                                    item.category.lowercase().contains(query) ||
                                    item.tags.any { it.contains(query) }
                            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
                            matchesSearch && matchesCategory
                        }
                    }

                    val showCustomGrid = (selectedCategory == "Custom" || selectedCategory == "All") && searchQuery.isBlank()

                    // If user is searching and no local match, suggest switching to Online Search
                    if (searchQuery.isNotBlank() && filteredBuiltins.isEmpty() && (!showCustomGrid || customIcons.isEmpty())) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No local icons found for \"$searchQuery\"",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    selectedCategory = "Online Search"
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Search \"$searchQuery\" Online", fontSize = 12.5.sp)
                            }
                        }
                    } else {
                        // Icons Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Show custom icons
                            if (showCustomGrid && customIcons.isNotEmpty()) {
                                items(customIcons) { customIconKey ->
                                    val isSelected = customIconKey == selectedIconName
                                    val file = IconHelper.getCustomIconFile(context, customIconKey)

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) SolidPrimary.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onIconSelected(customIconKey)
                                                onDismiss()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (file.exists()) {
                                            AsyncImage(
                                                model = file,
                                                contentDescription = "Custom Icon",
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Show built-in icons
                            if (selectedCategory != "Custom") {
                                items(filteredBuiltins) { item ->
                                    val isSelected = item.name == selectedIconName
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) SolidPrimary.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) SolidPrimary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onIconSelected(item.name)
                                                onDismiss()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (IconHelper.isDrawableIcon(item.name)) {
                                            IconHelper.AppIcon(
                                                iconName = item.name,
                                                contentDescription = item.name,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(item.name),
                                                contentDescription = item.name,
                                                tint = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(24.dp)
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

    // Delete custom icon dialog confirmation
    if (iconToDelete != null) {
        AlertDialog(
            onDismissRequest = { iconToDelete = null },
            title = { Text("Delete Custom Icon") },
            text = { Text("Are you sure you want to remove this custom icon?") },
            confirmButton = {
                Button(
                    onClick = {
                        val key = iconToDelete!!
                        IconHelper.deleteCustomIcon(context, key)
                        customIcons.remove(key)
                        iconToDelete = null
                        if (selectedIconName == key) {
                            onIconSelected("Category")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { iconToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
