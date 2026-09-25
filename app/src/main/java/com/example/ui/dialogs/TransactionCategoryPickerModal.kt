package com.example.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.DisplayFormatPreferences
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun CategoryPickerModalDialog(
    categories: List<Category>,
    txType: TransactionType,
    selectedCategoryId: Long?,
    selectedSubCategoryId: Long?,
    languageMode: LanguageMode,
    onCategorySelected: (Long, Long?) -> Unit,
    onAddNewCategory: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    val targetType = when (txType) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.TRANSFER -> CategoryType.EXPENSE
    }
    val activeCategories = remember(categories, targetType) {
        categories.filter { it.type == targetType && it.isActive }
    }
    val parentCategories = remember(activeCategories) {
        activeCategories.filter { it.parentId == null }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showInlineCreateCategory by remember { mutableStateOf(false) }

    if (showInlineCreateCategory) {
        QuickCreateCategoryDialog(
            parentCategories = parentCategories,
            targetType = targetType,
            languageMode = languageMode,
            onDismiss = { showInlineCreateCategory = false },
            onCategoryCreated = {
                onAddNewCategory(it)
                showInlineCreateCategory = false
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Drag Handle
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Top Search Bar with Green "New" Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = LanguageHelper.getString("search", languageMode).ifEmpty { "Search" },
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Green "New" Pill Button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showInlineCreateCategory = true }
                            ) {
                                Text(
                                    text = "New",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Category Groups Grid
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                    ) {
                        val query = searchQuery.trim().lowercase()

                        if (query.isNotEmpty()) {
                            // Search Results: Only search subcategories under groups
                            val matchingCategories = activeCategories.filter { cat ->
                                cat.parentId != null && (
                                    cat.nameEn.lowercase().contains(query) ||
                                    cat.nameBn.lowercase().contains(query)
                                )
                            }
                            if (matchingCategories.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = LanguageHelper.getString("no_results_found", languageMode).ifEmpty { "No categories found" },
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "Search Results",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                                Category3ColumnGrid(
                                    items = matchingCategories,
                                    selectedCategoryId = selectedCategoryId,
                                    selectedSubCategoryId = selectedSubCategoryId,
                                    languageMode = languageMode,
                                    onItemClick = { cat ->
                                        if (cat.parentId != null) {
                                            onCategorySelected(cat.parentId, cat.id)
                                        }
                                    }
                                )
                            }
                        } else {
                            // Display by Groups (Group name appears only once at middle as group name; no duplicate group name in categories)
                            parentCategories.forEach { parent ->
                                val subCats = activeCategories
                                    .filter { it.parentId == parent.id }
                                    .filter { sub ->
                                        // Remove duplicate if identical to group name unless it's the sole subcategory
                                        !sub.nameEn.equals(parent.nameEn, ignoreCase = true) ||
                                        activeCategories.count { it.parentId == parent.id } == 1
                                    }

                                if (subCats.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = parent.iconName,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = parent.localizedName(languageMode),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Category3ColumnGrid(
                                        items = subCats,
                                        selectedCategoryId = selectedCategoryId,
                                        selectedSubCategoryId = selectedSubCategoryId,
                                        languageMode = languageMode,
                                        onItemClick = { cat ->
                                            onCategorySelected(parent.id, cat.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
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
private fun Category3ColumnGrid(
    items: List<Category>,
    selectedCategoryId: Long?,
    selectedSubCategoryId: Long?,
    languageMode: LanguageMode,
    onItemClick: (Category) -> Unit
) {
    val rows = items.chunked(3)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0 until 3) {
                    if (i < rowItems.size) {
                        val cat = rowItems[i]
                        val isSelected = if (cat.parentId != null) {
                            selectedSubCategoryId == cat.id
                        } else {
                            selectedCategoryId == cat.id && selectedSubCategoryId == null
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onItemClick(cat) }
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(parseItemColor(cat.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconHelper.AppIcon(
                                        iconName = cat.iconName,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickCreateCategoryDialog(
    parentCategories: List<Category>,
    targetType: CategoryType,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onCategoryCreated: (Category) -> Unit
) {
    var createMode by remember { mutableStateOf(if (parentCategories.isNotEmpty()) 0 else 1) } // 0 = Category under Group, 1 = New Group
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf<Long?>(parentCategories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (createMode == 0) "Create Category" else "Create Category Group",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = createMode == 0,
                        onClick = { createMode = 0 },
                        label = { Text("Sub-Category", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = createMode == 1,
                        onClick = { createMode = 1 },
                        label = { Text("New Group", fontSize = 12.sp) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (createMode == 0) "Category Name (English)" else "Group Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Name (Bangla - Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (createMode == 0) {
                    if (parentCategories.isEmpty()) {
                        Text(
                            text = "No category groups exist yet. Please switch to 'New Group' first.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    } else {
                        Text("Connect to Group (Required):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parentCategories.forEach { parent ->
                                FilterChip(
                                    selected = selectedParentId == parent.id,
                                    onClick = { selectedParentId = parent.id },
                                    label = { Text(parent.localizedName(languageMode), fontSize = 12.sp) },
                                    leadingIcon = {
                                        IconHelper.AppIcon(
                                            iconName = parent.iconName,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canSave = nameEn.isNotBlank() && (createMode == 1 || (createMode == 0 && selectedParentId != null))
            Button(
                onClick = {
                    if (canSave) {
                        val parentCat = if (createMode == 0) parentCategories.firstOrNull { it.id == selectedParentId } else null
                        val newCat = Category(
                            nameEn = nameEn.trim(),
                            nameBn = nameBn.trim().ifEmpty { nameEn.trim() },
                            type = targetType,
                            parentId = if (createMode == 0) selectedParentId else null,
                            iconName = parentCat?.iconName ?: "Category",
                            colorHex = parentCat?.colorHex ?: "#2563EB"
                        )
                        onCategoryCreated(newCat)
                    }
                },
                enabled = canSave
            ) {
                Text(LanguageHelper.getString("save", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

