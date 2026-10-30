package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.example.ui.components.AppTabHeader
import com.example.ui.dialogs.AddEditLabelDialog
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.rememberFluidFlingBehavior
import com.example.util.IconHelper
import com.example.util.LanguageHelper

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("manage_labels_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTabHeader(
                title = LanguageHelper.getString("labels", languageMode).ifEmpty { if (isBn) "লেবেল" else "Labels" },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (isBn) "লেবেল অনুসন্ধান..." else "Search labels...",
                showSearchButton = true,
                onOpenDrawer = onOpenDrawer,
                onBack = onBack
            )

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
                            text = if (isBn) "লেনদেন ফর্ম থেকে নতুন লেবেল যোগ করুন বা নিচের + বাটনে ট্যাপ করুন"
                            else "Add labels from the transaction form or tap + below to create one",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
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
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        // Floating Action Button for adding labels
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("manage_labels_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isBn) "নতুন লেবেল" else "Add Label"
            )
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
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
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
            // Label Icon Badge with Slim Border
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(labelColor.copy(alpha = 0.14f))
                    .border(
                        BorderStroke(0.6.dp, labelColor.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp)
                    ),
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
