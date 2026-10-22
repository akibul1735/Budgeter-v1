package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionLabel
import com.example.data.model.TransactionWithDetails
import com.example.ui.dialogs.AddEditLabelDialog
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.rememberFluidFlingBehavior
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLabelsScreen(
    allLabels: List<TransactionLabel>,
    transactions: List<TransactionWithDetails> = emptyList(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onNavigateToCategories: (() -> Unit)? = null,
    onSaveLabel: (TransactionLabel) -> Unit,
    onUpdateLabel: (oldName: String, newName: String, iconName: String, colorHex: String) -> Unit,
    onDeleteLabel: (TransactionLabel) -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<TransactionLabel?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Calculate usage frequencies from transactions
    val labelUsageCount = remember(transactions) {
        val map = mutableMapOf<String, Int>()
        transactions.forEach { txDetail ->
            val ref = txDetail.transaction.referenceNo.trim()
            if (ref.isNotBlank()) {
                ref.split(",").forEach { raw ->
                    val clean = raw.trim().removePrefix("#").lowercase()
                    if (clean.isNotBlank()) {
                        map[clean] = (map[clean] ?: 0) + 1
                    }
                }
            }
        }
        map
    }

    // Combine database labels with any ad-hoc labels in transactions
    val combinedLabels = remember(allLabels, labelUsageCount) {
        val list = allLabels.toMutableList()
        val existingNormalized = list.map { it.normalizedName.lowercase() }.toSet()
        labelUsageCount.keys.forEach { nameKey ->
            if (nameKey !in existingNormalized) {
                // Synthesize transient label entry
                list.add(
                    TransactionLabel(
                        id = 0L,
                        name = nameKey.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        normalizedName = nameKey,
                        iconName = "Label",
                        colorHex = "#10B981"
                    )
                )
            }
        }
        list.sortedWith(
            compareByDescending<TransactionLabel> { labelUsageCount[it.normalizedName.lowercase()] ?: 0 }
                .thenBy { it.name.lowercase() }
        )
    }

    val filteredLabels = remember(combinedLabels, searchQuery) {
        if (searchQuery.isBlank()) combinedLabels
        else combinedLabels.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (onBack != null) onBack() else onOpenDrawer()
                            },
                            modifier = Modifier.testTag("manage_labels_back_or_drawer")
                        ) {
                            Icon(
                                imageVector = if (onBack != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                                contentDescription = if (onBack != null) "Back" else "Open Drawer"
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBn) "লেবেল ব্যবস্থাপনা" else "Labels",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "${filteredLabels.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isBn) "নাম ও আইকন পরিবর্তন করুন" else "Change name and icons",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = { isSearchActive = !isSearchActive },
                            modifier = Modifier.testTag("manage_labels_search_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Search Labels"
                            )
                        }

                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("manage_labels_add_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "নতুন" else "Add",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Optional Search Field
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (isBn) "লেবেল অনুসন্ধান..." else "Search labels...",
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Categories & Labels Switcher Tab Row
                    if (onNavigateToCategories != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    0.8.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clickable { onNavigateToCategories() }
                                    .testTag("manage_labels_tab_categories")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageHelper.getString("categories", languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("manage_labels_tab_labels")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = LanguageHelper.getString("labels", languageMode).ifEmpty { "Labels" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("manage_labels_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isBn) "নতুন লেবেল" else "Add Label"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredLabels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (isBn) "\"$searchQuery\" এর সাথে কোনো লেবেল মেলেনি" else "No labels match \"$searchQuery\""
                            } else {
                                if (isBn) "কোনো লেবেল যুক্ত করা হয়নি" else "No labels found yet"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "লেনদেন ফর্ম থেকে নতুন লেবেল যোগ করুন বা এখানে নতুন লেবেল তৈরি করুন"
                            else "Add labels from the transaction form or create one here to change its icon and color",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "প্রথম লেবেল যোগ করুন" else "Create First Label")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    flingBehavior = rememberFluidFlingBehavior(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLabels, key = { it.normalizedName.lowercase() }) { labelItem ->
                        val count = labelUsageCount[labelItem.normalizedName.lowercase()] ?: 0
                        ManageLabelItemCard(
                            label = labelItem,
                            usageCount = count,
                            languageMode = languageMode,
                            onEdit = { editingLabel = labelItem }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Edit Label Dialog
    editingLabel?.let { labelToEdit ->
        AddEditLabelDialog(
            label = labelToEdit,
            initialName = labelToEdit.name,
            languageMode = languageMode,
            onDismiss = { editingLabel = null },
            onSave = { name, iconName, colorHex ->
                val oldName = labelToEdit.name
                onUpdateLabel(oldName, name, iconName, colorHex)
                editingLabel = null
            },
            onDelete = if (labelToEdit.id != 0L) {
                {
                    onDeleteLabel(labelToEdit)
                    editingLabel = null
                }
            } else null
        )
    }

    // Add Label Dialog
    if (showAddDialog) {
        AddEditLabelDialog(
            label = null,
            languageMode = languageMode,
            onDismiss = { showAddDialog = false },
            onSave = { name, iconName, colorHex ->
                onSaveLabel(
                    TransactionLabel(
                        name = name,
                        iconName = iconName,
                        colorHex = colorHex
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ManageLabelItemCard(
    label: TransactionLabel,
    usageCount: Int,
    languageMode: LanguageMode,
    onEdit: () -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA
    val labelColor = remember(label.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(label.colorHex))
        } catch (_: Exception) {
            SolidPrimary
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onEdit() }
            .testTag("manage_label_card_${label.normalizedName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(labelColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                if (label.iconName.isNotBlank() && label.iconName != "Label" && label.iconName != "Tag") {
                    IconHelper.AppIcon(
                        iconName = label.iconName,
                        tint = labelColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = labelColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Label Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (usageCount > 0) {
                    if (isBn) "$usageCount টি লেনদেনে ব্যবহৃত" else "Used in $usageCount transactions"
                } else {
                    if (isBn) "কোনো লেনদেনে ব্যবহৃত হয়নি" else "Not used in transactions yet"
                }
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Edit Action Icon
            FilledTonalIconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("edit_label_${label.normalizedName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isBn) "সম্পাদনা" else "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
