package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.IconPickerModal
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper

private val categoryPresetColors = listOf(
    "#EF4444", "#F97316", "#F59E0B", "#10B981", "#06B6D4",
    "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899", "#64748B"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    categories: List<Category>,
    languageMode: LanguageMode,
    initialTab: Int = 0,
    onAddCategoryClick: (CategoryType) -> Unit,
    onAddSubCategoryClick: (Category) -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onUpdateCategories: ((List<Category>) -> Unit)? = null,
    onDeleteCategories: ((List<Category>) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedCategoryIds = remember { mutableStateListOf<Long>() }

    var showBatchEditDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val currentType = if (selectedTab == 0) CategoryType.EXPENSE else CategoryType.INCOME

    val parentCategories = remember(categories, currentType) {
        categories.filter { it.type == currentType && it.parentId == null }
    }

    val subCategoriesMap = remember(categories, currentType) {
        categories.filter { it.type == currentType && it.parentId != null }.groupBy { it.parentId!! }
    }

    val currentTypeCategories = remember(categories, currentType) {
        categories.filter { it.type == currentType }
    }

    val selectedCategories = remember(categories, selectedCategoryIds.toList()) {
        categories.filter { it.id in selectedCategoryIds }
    }

    fun toggleSelection(id: Long) {
        if (selectedCategoryIds.contains(id)) {
            selectedCategoryIds.remove(id)
            if (selectedCategoryIds.isEmpty()) {
                // Keep in selection mode or leave
            }
        } else {
            selectedCategoryIds.add(id)
        }
    }

    fun selectAll() {
        selectedCategoryIds.clear()
        selectedCategoryIds.addAll(currentTypeCategories.map { it.id })
    }

    fun deselectAll() {
        selectedCategoryIds.clear()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("categories_screen"),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = if (isSelectionMode) 88.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageHelper.getString("categories", languageMode),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${parentCategories.size} ${if (selectedTab == 0) LanguageHelper.getString("expenses", languageMode) else LanguageHelper.getString("incomes", languageMode)} ${LanguageHelper.getString("categories", languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Multi-select toggle button
                        FilterChip(
                            selected = isSelectionMode,
                            onClick = {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) {
                                    selectedCategoryIds.clear()
                                }
                            },
                            label = {
                                Text(
                                    text = if (isSelectionMode) "Done" else "Select",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )

                        if (!isSelectionMode) {
                            Button(
                                onClick = { onAddCategoryClick(currentType) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == 0) SolidExpense else SolidIncome
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageHelper.getString("add_category", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Tabs (Expenses / Incomes)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 0) SolidExpense else SolidIncome
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            selectedCategoryIds.clear()
                        },
                        text = {
                            Text(
                                text = LanguageHelper.getString("expenses", languageMode),
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            selectedCategoryIds.clear()
                        },
                        text = {
                            Text(
                                text = LanguageHelper.getString("incomes", languageMode),
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Selection Controls bar when in selection mode
            if (isSelectionMode) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedCategoryIds.size} / ${currentTypeCategories.size} selected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { selectAll() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Select All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                TextButton(
                                    onClick = { deselectAll() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Deselect", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            if (parentCategories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LanguageHelper.getString("no_categories", languageMode),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(parentCategories) { parent ->
                    val subs = subCategoriesMap[parent.id] ?: emptyList()
                    val isExpanded = expandedMap[parent.id] ?: true
                    val isParentSelected = selectedCategoryIds.contains(parent.id)

                    val parentColor = remember(parent.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(parent.colorHex))
                        } catch (_: Exception) {
                            if (currentType == CategoryType.EXPENSE) SolidExpense else SolidIncome
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        toggleSelection(parent.id)
                                    } else {
                                        if (subs.isNotEmpty()) {
                                            expandedMap[parent.id] = !isExpanded
                                        } else {
                                            onEditCategoryClick(parent)
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                    }
                                    toggleSelection(parent.id)
                                }
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isParentSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isParentSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SolidPrimary) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Parent Category Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSelectionMode) {
                                        Checkbox(
                                            checked = isParentSelected,
                                            onCheckedChange = { toggleSelection(parent.id) },
                                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(parentColor.copy(alpha = 0.16f))
                                            .border(1.dp, parentColor.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            tint = parentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = parent.localizedName(languageMode),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = parentColor.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "Group",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = parentColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        if (subs.isNotEmpty()) {
                                            Text(
                                                text = "${subs.size} ${LanguageHelper.getString("sub_categories", languageMode)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (parent.budgetLimit > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = "Limit: ${LanguageHelper.formatCurrency(parent.budgetLimit, languageMode)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (!isSelectionMode) {
                                        if (subs.isNotEmpty()) {
                                            IconButton(
                                                onClick = { expandedMap[parent.id] = !isExpanded },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = "Expand",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { onEditCategoryClick(parent) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Sub-categories list
                            AnimatedVisibility(visible = (isExpanded || isSelectionMode) && subs.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, start = if (isSelectionMode) 20.dp else 12.dp)
                                ) {
                                    subs.forEach { subCat ->
                                        val isSubSelected = selectedCategoryIds.contains(subCat.id)
                                        val subColor = remember(subCat.colorHex) {
                                            try {
                                                Color(android.graphics.Color.parseColor(subCat.colorHex))
                                            } catch (_: Exception) {
                                                parentColor
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            toggleSelection(subCat.id)
                                                        } else {
                                                            onEditCategoryClick(subCat)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isSelectionMode) {
                                                            isSelectionMode = true
                                                        }
                                                        toggleSelection(subCat.id)
                                                    }
                                                ),
                                            color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isSubSelected) androidx.compose.foundation.BorderStroke(1.dp, SolidPrimary) else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    if (isSelectionMode) {
                                                        Checkbox(
                                                            checked = isSubSelected,
                                                            onCheckedChange = { toggleSelection(subCat.id) },
                                                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }

                                                    // Category icon
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(subColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = IconHelper.getIconByName(subCat.iconName),
                                                            contentDescription = null,
                                                            tint = subColor,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = subCat.localizedName(languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                if (!isSelectionMode) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable { onEditCategoryClick(subCat) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (!isSelectionMode) {
                                        // Quick Add Sub-Category button
                                        Text(
                                            text = "+ ${LanguageHelper.getString("add_sub_category", languageMode)}",
                                            fontSize = 11.sp,
                                            color = SolidPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { onAddSubCategoryClick(parent) }
                                                .padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Batch Action Toolbar at Bottom when items are selected
        if (isSelectionMode && selectedCategoryIds.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedCategoryIds.size} Selected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Batch Actions",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showBatchEditDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Edit / Icons", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showBatchDeleteDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Batch Edit Dialog
    if (showBatchEditDialog && selectedCategories.isNotEmpty() && onUpdateCategories != null) {
        CategoryBatchEditDialog(
            selectedCategories = selectedCategories,
            allParentCategories = parentCategories,
            languageMode = languageMode,
            onDismiss = { showBatchEditDialog = false },
            onApply = { updatedCategories ->
                onUpdateCategories(updatedCategories)
                showBatchEditDialog = false
                selectedCategoryIds.clear()
                isSelectionMode = false
            }
        )
    }

    // Batch Delete Confirmation
    if (showBatchDeleteDialog && selectedCategories.isNotEmpty() && onDeleteCategories != null) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Delete ${selectedCategories.size} Categories?") },
            text = {
                Text(
                    "Are you sure you want to delete these ${selectedCategories.size} selected categories? Any sub-categories or assigned transactions will be affected."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategories(selectedCategories)
                        showBatchDeleteDialog = false
                        selectedCategoryIds.clear()
                        isSelectionMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All Selected")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBatchEditDialog(
    selectedCategories: List<Category>,
    allParentCategories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (List<Category>) -> Unit
) {
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var selectedColorHex by remember { mutableStateOf<String?>(null) }
    var targetParentId by remember { mutableStateOf<Long?>(null) }
    var changeParentGroup by remember { mutableStateOf(false) }
    var makeTopLevel by remember { mutableStateOf(false) }
    var targetType by remember { mutableStateOf<CategoryType?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    // Single item edit fields
    val isSingle = selectedCategories.size == 1
    val singleCat = selectedCategories.firstOrNull()
    var singleNameEn by remember { mutableStateOf(singleCat?.nameEn ?: "") }
    var singleNameBn by remember { mutableStateOf(singleCat?.nameBn ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSingle) "Edit Category" else "Batch Edit (${selectedCategories.size} items)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apply modifications to selected categories",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If single item, show name fields
                if (isSingle) {
                    OutlinedTextField(
                        value = singleNameEn,
                        onValueChange = { singleNameEn = it },
                        label = { Text("Name (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = singleNameBn,
                        onValueChange = { singleNameBn = it },
                        label = { Text("Name (Bangla)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Change Icon section
                Text(
                    text = "Set Icon",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SolidPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(selectedIcon ?: selectedCategories.first().iconName),
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedIcon?.let { "Icon: $it" } ?: "Keep current icons (tap to change)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text("Change", fontSize = 11.sp, color = SolidPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Change Color section
                Text(
                    text = "Set Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryPresetColors.take(8).forEach { hex ->
                        val itemColor = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Change Parent Group Section
                Text(
                    text = "Move / Group Assignment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !changeParentGroup && !makeTopLevel,
                        onClick = {
                            changeParentGroup = false
                            makeTopLevel = false
                        },
                        label = { Text("No Change", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = makeTopLevel,
                        onClick = {
                            makeTopLevel = true
                            changeParentGroup = false
                            targetParentId = null
                        },
                        label = { Text("Make Group", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = changeParentGroup,
                        onClick = {
                            changeParentGroup = true
                            makeTopLevel = false
                            if (targetParentId == null && allParentCategories.isNotEmpty()) {
                                targetParentId = allParentCategories.first().id
                            }
                        },
                        label = { Text("Move to Group", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (changeParentGroup) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val selectedTarget = allParentCategories.firstOrNull { it.id == targetParentId }

                    ExposedDropdownMenuBox(
                        expanded = parentDropdownExpanded,
                        onExpandedChange = { parentDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTarget?.localizedName(languageMode) ?: "Select Target Group",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = parentDropdownExpanded,
                            onDismissRequest = { parentDropdownExpanded = false }
                        ) {
                            allParentCategories.forEach { parent ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(parent.localizedName(languageMode)) },
                                    onClick = {
                                        targetParentId = parent.id
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Apply button
                Button(
                    onClick = {
                        val modifiedList = selectedCategories.map { cat ->
                            var updated = cat
                            if (isSingle) {
                                updated = updated.copy(
                                    nameEn = singleNameEn.ifBlank { cat.nameEn },
                                    nameBn = singleNameBn.ifBlank { cat.nameBn }
                                )
                            }
                            if (selectedIcon != null) {
                                updated = updated.copy(iconName = selectedIcon!!)
                            }
                            if (selectedColorHex != null) {
                                updated = updated.copy(colorHex = selectedColorHex!!)
                            }
                            if (makeTopLevel) {
                                updated = updated.copy(parentId = null)
                            } else if (changeParentGroup && targetParentId != null) {
                                updated = updated.copy(parentId = targetParentId)
                            }
                            if (targetType != null) {
                                updated = updated.copy(type = targetType!!)
                            }
                            updated
                        }
                        onApply(modifiedList)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Changes to Selected", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerModal(
            selectedIconName = selectedIcon ?: selectedCategories.first().iconName,
            onIconSelected = { icon ->
                selectedIcon = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
