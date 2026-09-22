package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.WishlistItem
import com.example.data.model.WishlistItemWithCategory
import com.example.data.model.WishlistPriority
import com.example.data.model.WishlistTargetType
import com.example.ui.dialogs.AddEditWishlistDialog
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch
import java.util.Calendar

enum class WishlistFilterTab {
    ACTIVE,
    NEXT_MONTH,
    PLANNED_MONTHS,
    SAVINGS_GOALS,
    PURCHASED,
    ALL
}

enum class WishlistSort {
    PRIORITY,
    AMOUNT_DESC,
    AMOUNT_ASC,
    RECENT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WishlistScreen(
    wishlistItemsWithDetails: List<WishlistItemWithCategory>,
    categories: List<Category>,
    languageMode: LanguageMode,
    onSaveWishlistItem: (WishlistItem) -> Unit,
    onDeleteWishlistItem: (Long) -> Unit,
    onTogglePurchased: (Long, Boolean) -> Unit,
    onAddToBudget: (WishlistItem, Int, Int) -> Unit,
    onConvertToGoal: (WishlistItem, Long) -> Unit,
    onRecordPurchase: (WishlistItem) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(WishlistFilterTab.ACTIVE) }
    var selectedSort by remember { mutableStateOf(WishlistSort.PRIORITY) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<WishlistItem?>(null) }
    var itemToDelete by remember { mutableStateOf<WishlistItem?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()
    val nextCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    val nextMonth = nextCal.get(Calendar.MONTH) + 1
    val nextYear = nextCal.get(Calendar.YEAR)

    // Calculate Summary Stats
    val activeItems = wishlistItemsWithDetails.filter { !it.item.isPurchased }
    val totalActiveAmount = activeItems.sumOf { it.item.estimatedAmount }
    val nextMonthItems = activeItems.filter {
        it.item.targetType == WishlistTargetType.NEXT_MONTH ||
                (it.item.targetType == WishlistTargetType.SPECIFIC_MONTH && it.item.targetYear == nextYear && it.item.targetMonth == nextMonth)
    }
    val nextMonthAmount = nextMonthItems.sumOf { it.item.estimatedAmount }
    val goalItems = activeItems.filter { it.item.targetType == WishlistTargetType.SAVINGS_GOAL || it.item.linkedGoalId != null }
    val goalAmount = goalItems.sumOf { it.item.estimatedAmount }

    // Filter Items
    val filteredItems = remember(wishlistItemsWithDetails, selectedTab, searchQuery, selectedSort) {
        var list = when (selectedTab) {
            WishlistFilterTab.ACTIVE -> wishlistItemsWithDetails.filter { !it.item.isPurchased }
            WishlistFilterTab.NEXT_MONTH -> wishlistItemsWithDetails.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.NEXT_MONTH ||
                        (it.item.targetType == WishlistTargetType.SPECIFIC_MONTH && it.item.targetYear == nextYear && it.item.targetMonth == nextMonth))
            }
            WishlistFilterTab.PLANNED_MONTHS -> wishlistItemsWithDetails.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.SPECIFIC_MONTH || it.item.targetType == WishlistTargetType.NEXT_MONTH)
            }
            WishlistFilterTab.SAVINGS_GOALS -> wishlistItemsWithDetails.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.SAVINGS_GOAL || it.item.linkedGoalId != null)
            }
            WishlistFilterTab.PURCHASED -> wishlistItemsWithDetails.filter { it.item.isPurchased }
            WishlistFilterTab.ALL -> wishlistItemsWithDetails
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.item.title.lowercase().contains(q) ||
                        it.item.notes.lowercase().contains(q) ||
                        (it.category?.nameEn?.lowercase()?.contains(q) == true) ||
                        (it.category?.nameBn?.lowercase()?.contains(q) == true)
            }
        }

        when (selectedSort) {
            WishlistSort.PRIORITY -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenBy {
                        when (it.item.priority) {
                            WishlistPriority.HIGH -> 1
                            WishlistPriority.MEDIUM -> 2
                            WishlistPriority.LOW -> 3
                        }
                    }
                    .thenByDescending { it.item.createdAt }
            )
            WishlistSort.AMOUNT_DESC -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenByDescending { it.item.estimatedAmount }
            )
            WishlistSort.AMOUNT_ASC -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenBy { it.item.estimatedAmount }
            )
            WishlistSort.RECENT -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenByDescending { it.item.createdAt }
            )
        }
    }

    if (showAddEditDialog) {
        AddEditWishlistDialog(
            item = itemToEdit,
            categories = categories,
            languageMode = languageMode,
            onDismiss = {
                showAddEditDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                onSaveWishlistItem(savedItem)
                showAddEditDialog = false
                itemToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Wishlist item saved!")
                }
            }
        )
    }

    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(LanguageHelper.getString("delete", languageMode)) },
            text = { Text("Are you sure you want to delete \"${target.title}\" from your wishlist?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWishlistItem(target.id)
                        itemToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Wishlist item deleted")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageHelper.getString("delete", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_wishlist_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = LanguageHelper.getString("add_to_wishlist", languageMode))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = LanguageHelper.getString("wishlist", languageMode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = LanguageHelper.getString("search", languageMode),
                                    tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Sort",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Priority (High to Low)") },
                                        onClick = {
                                            selectedSort = WishlistSort.PRIORITY
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Amount (High to Low)") },
                                        onClick = {
                                            selectedSort = WishlistSort.AMOUNT_DESC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Amount (Low to High)") },
                                        onClick = {
                                            selectedSort = WishlistSort.AMOUNT_ASC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Recently Added") },
                                        onClick = {
                                            selectedSort = WishlistSort.RECENT
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    itemToEdit = null
                                    showAddEditDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Search input bar
                    AnimatedVisibility(visible = isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(LanguageHelper.getString("search", languageMode) + " wishlist...") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        divider = {}
                    ) {
                        WishlistFilterTab.values().forEach { tab ->
                            val count = when (tab) {
                                WishlistFilterTab.ACTIVE -> activeItems.size
                                WishlistFilterTab.NEXT_MONTH -> nextMonthItems.size
                                WishlistFilterTab.PLANNED_MONTHS -> activeItems.count { it.item.targetType == WishlistTargetType.SPECIFIC_MONTH || it.item.targetType == WishlistTargetType.NEXT_MONTH }
                                WishlistFilterTab.SAVINGS_GOALS -> goalItems.size
                                WishlistFilterTab.PURCHASED -> wishlistItemsWithDetails.count { it.item.isPurchased }
                                WishlistFilterTab.ALL -> wishlistItemsWithDetails.size
                            }
                            val label = when (tab) {
                                WishlistFilterTab.ACTIVE -> "Active"
                                WishlistFilterTab.NEXT_MONTH -> "Next Month"
                                WishlistFilterTab.PLANNED_MONTHS -> "Planned"
                                WishlistFilterTab.SAVINGS_GOALS -> "Goals"
                                WishlistFilterTab.PURCHASED -> "Purchased"
                                WishlistFilterTab.ALL -> LanguageHelper.getString("all", languageMode)
                            }

                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (count > 0) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selectedTab == tab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Metric Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Value Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = LanguageHelper.getString("wishlist_total_value", languageMode),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LanguageHelper.activeCurrencyConfig.activeSymbol + LanguageHelper.formatNumber(totalActiveAmount, languageMode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${activeItems.size} items active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Next Month Planned Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = LanguageHelper.getString("planned_next_month_val", languageMode),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LanguageHelper.activeCurrencyConfig.activeSymbol + LanguageHelper.formatNumber(nextMonthAmount, languageMode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${nextMonthItems.size} items in budget plan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = LanguageHelper.getString("no_wishlist_items", languageMode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        itemToEdit = null
                                        showAddEditDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(LanguageHelper.getString("add_to_wishlist", languageMode))
                                }
                            }
                        }
                    }
                } else {
                    items(filteredItems, key = { it.item.id }) { itemWithCat ->
                        val wishItem = itemWithCat.item
                        val category = itemWithCat.category
                        val subCategory = itemWithCat.subCategory

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .testTag("wishlist_item_${wishItem.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (wishItem.isPurchased) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (wishItem.priority == WishlistPriority.HIGH && !wishItem.isPurchased) {
                                    Color(0xFFEF4444).copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Top Row: Title, Amount, Checkbox
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Checkbox(
                                            checked = wishItem.isPurchased,
                                            onCheckedChange = { isChecked ->
                                                onTogglePurchased(wishItem.id, isChecked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        Column {
                                            Text(
                                                text = wishItem.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (wishItem.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (wishItem.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            )

                                            // Category tag
                                            if (category != null) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    val catIcon = IconHelper.getIconByName(category.iconName)
                                                    Icon(
                                                        imageVector = catIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
                                                            .getOrDefault(MaterialTheme.colorScheme.primary)
                                                    )
                                                    val catName = LanguageHelper.getLocalizedName(category.nameEn, category.nameBn, languageMode)
                                                    val subName = subCategory?.let { " > " + LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode) } ?: ""
                                                    Text(
                                                        text = "$catName$subName",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = LanguageHelper.activeCurrencyConfig.activeSymbol + LanguageHelper.formatNumber(wishItem.estimatedAmount, languageMode),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (wishItem.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                        )

                                        // Priority Chip
                                        val (prioText, prioBg, prioFg) = when (wishItem.priority) {
                                            WishlistPriority.HIGH -> Triple("🔥 High", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626))
                                            WishlistPriority.MEDIUM -> Triple("⭐ Medium", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706))
                                            WishlistPriority.LOW -> Triple("💤 Low", Color(0xFF6B7280).copy(alpha = 0.15f), Color(0xFF4B5563))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = prioBg,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = prioText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = prioFg,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Timeline and Meta Tags
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Target Timeline Badge
                                    when (wishItem.targetType) {
                                        WishlistTargetType.NEXT_MONTH -> {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                            ) {
                                                Text(
                                                    text = "⚡ " + LanguageHelper.getString("next_month", languageMode),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        WishlistTargetType.SPECIFIC_MONTH -> {
                                            val mStr = wishItem.targetMonth?.let { "Month $it" } ?: ""
                                            val yStr = wishItem.targetYear?.toString() ?: ""
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                            ) {
                                                Text(
                                                    text = "📅 $mStr $yStr",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        WishlistTargetType.SAVINGS_GOAL -> {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "🎯 " + LanguageHelper.getString("save_up_goal", languageMode),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        WishlistTargetType.SOMEDAY -> {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "💭 " + LanguageHelper.getString("someday_maybe", languageMode),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Added to Budget Indicator
                                    if (wishItem.isAddedToBudget) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = "✓ " + LanguageHelper.getString("added_to_budget", languageMode),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Notes
                                if (wishItem.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = wishItem.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // URL link preview if available
                                if (wishItem.url.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wishItem.url))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open Link",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = wishItem.url,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Primary Quick Actions (Add to Budget / Buy / Convert to Goal)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!wishItem.isPurchased) {
                                            // Buy / Record Purchase
                                            Button(
                                                onClick = { onRecordPurchase(wishItem) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ShoppingCart,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(LanguageHelper.getString("buy", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Add to Month Budget button if category is set
                                            if (category != null && !wishItem.isAddedToBudget) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val targetY = wishItem.targetYear ?: nextYear
                                                        val targetM = wishItem.targetMonth ?: nextMonth
                                                        onAddToBudget(wishItem, targetY, targetM)
                                                        coroutineScope.launch {
                                                            val formattedAmt = LanguageHelper.formatCurrency(wishItem.estimatedAmount, languageMode)
                                                            val catName = LanguageHelper.getLocalizedName(category.nameEn, category.nameBn, languageMode)
                                                            snackbarHostState.showSnackbar("Added $formattedAmt to $catName budget!")
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarMonth,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(LanguageHelper.getString("add_to_budget", languageMode), fontSize = 11.sp)
                                                }
                                            }

                                            // Convert to Savings Goal
                                            if (wishItem.linkedGoalId == null && wishItem.targetType != WishlistTargetType.SAVINGS_GOAL) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val targetDeadline = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000L // 3 months default
                                                        onConvertToGoal(wishItem, targetDeadline)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Created Savings Goal for \"${wishItem.title}\"!")
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Savings,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(LanguageHelper.getString("goal", languageMode), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Secondary Actions: Edit, Delete
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                itemToEdit = wishItem
                                                showAddEditDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = LanguageHelper.getString("edit", languageMode),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { itemToDelete = wishItem },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = LanguageHelper.getString("delete", languageMode),
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
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
