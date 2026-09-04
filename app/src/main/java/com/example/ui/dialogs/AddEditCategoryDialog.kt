package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    parentCategories: List<Category>,
    languageMode: LanguageMode,
    existingCategory: Category? = null,
    defaultType: CategoryType = CategoryType.EXPENSE,
    defaultParentId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
    onDelete: ((Category) -> Unit)? = null
) {
    var nameEn by remember { mutableStateOf(existingCategory?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(existingCategory?.nameBn ?: "") }
    var categoryType by remember { mutableStateOf(existingCategory?.type ?: defaultType) }
    var parentId by remember { mutableStateOf(existingCategory?.parentId ?: defaultParentId) }
    var budgetLimitText by remember {
        mutableStateOf(existingCategory?.budgetLimit?.toString() ?: "0.0")
    }
    var isActive by remember {
        mutableStateOf(existingCategory?.isActive ?: true)
    }
    var isSubCategory by remember { mutableStateOf(parentId != null) }
    var iconName by remember {
        mutableStateOf(
            existingCategory?.iconName ?: if (categoryType == CategoryType.INCOME) "Payments" else "ShoppingCart"
        )
    }
    var colorHex by remember {
        mutableStateOf(
            existingCategory?.colorHex ?: if (categoryType == CategoryType.INCOME) "#10B981" else "#EF4444"
        )
    }
    var showIconPicker by remember { mutableStateOf(false) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val parsedColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            if (categoryType == CategoryType.INCOME) SolidIncome else SolidExpense
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_category_dialog")
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
                            text = if (existingCategory == null) {
                                if (isSubCategory) LanguageHelper.getString("add_sub_category", languageMode)
                                else LanguageHelper.getString("add_category", languageMode)
                            } else {
                                LanguageHelper.getString("edit", languageMode)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSubCategory) "Under a parent group" else "Top-level group/category",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon and Color Preview Header Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
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
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor.copy(alpha = 0.18f))
                                    .border(2.dp, parsedColor, CircleShape)
                                    .clickable { showIconPicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(iconName),
                                    contentDescription = "Category Icon",
                                    tint = parsedColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (nameEn.isNotBlank() || nameBn.isNotBlank()) {
                                        if (languageMode == LanguageMode.BANGLA && nameBn.isNotBlank()) nameBn else nameEn.ifBlank { nameBn }
                                    } else "Select Icon & Color",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Icon: $iconName",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Button(
                            onClick = { showIconPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Icon", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color Preset Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryPresetColors.take(8).forEach { hex ->
                        val itemColor = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Type Chips (Expense / Income)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoryType == CategoryType.EXPENSE,
                        onClick = {
                            categoryType = CategoryType.EXPENSE
                            if (existingCategory == null) {
                                colorHex = "#EF4444"
                                iconName = "ShoppingCart"
                            }
                        },
                        label = { Text(LanguageHelper.getString("expense", languageMode), fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidExpense.copy(alpha = 0.15f),
                            selectedLabelColor = SolidExpense
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = categoryType == CategoryType.INCOME,
                        onClick = {
                            categoryType = CategoryType.INCOME
                            if (existingCategory == null) {
                                colorHex = "#10B981"
                                iconName = "Payments"
                            }
                        },
                        label = { Text(LanguageHelper.getString("income", languageMode), fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidIncome.copy(alpha = 0.15f),
                            selectedLabelColor = SolidIncome
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hierarchy Selector: Main Group / Category vs Sub-Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isSubCategory,
                        onClick = {
                            isSubCategory = false
                            parentId = null
                        },
                        label = { Text(LanguageHelper.getString("parent_category", languageMode)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isSubCategory,
                        onClick = {
                            isSubCategory = true
                            if (parentId == null && parentCategories.isNotEmpty()) {
                                parentId = parentCategories.firstOrNull { it.type == categoryType }?.id ?: parentCategories.first().id
                            }
                        },
                        label = { Text(LanguageHelper.getString("sub_categories", languageMode)) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isSubCategory) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val filteredParents = parentCategories.filter { it.type == categoryType && it.parentId == null }
                    val selectedParent = filteredParents.firstOrNull { it.id == parentId }

                    ExposedDropdownMenuBox(
                        expanded = parentDropdownExpanded,
                        onExpandedChange = { parentDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedParent?.localizedName(languageMode) ?: LanguageHelper.getString("select_category", languageMode),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(LanguageHelper.getString("parent_category", languageMode)) },
                            leadingIcon = {
                                if (selectedParent != null) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(selectedParent.iconName),
                                        contentDescription = null,
                                        tint = parsedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
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
                            filteredParents.forEach { parent ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            tint = parsedColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(parent.localizedName(languageMode), fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        parentId = parent.id
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name English
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(LanguageHelper.getString("name_en", languageMode)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name Bangla
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(LanguageHelper.getString("name_bn", languageMode)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (categoryType == CategoryType.EXPENSE && !isSubCategory) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetLimitText,
                        onValueChange = { budgetLimitText = it },
                        label = { Text(LanguageHelper.getString("budget_limit", languageMode)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সক্রিয় অবস্থা" else "Active Status",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (existingCategory != null && onDelete != null && !existingCategory.isSystem) {
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text(LanguageHelper.getString("delete", languageMode))
                        }
                    }

                    Button(
                        onClick = {
                            val parsedLimit = budgetLimitText.toDoubleOrNull() ?: 0.0
                            val category = Category(
                                id = existingCategory?.id ?: 0,
                                nameEn = nameEn.ifBlank { nameBn },
                                nameBn = nameBn.ifBlank { nameEn },
                                type = categoryType,
                                parentId = if (isSubCategory) parentId else null,
                                budgetLimit = parsedLimit,
                                iconName = iconName,
                                colorHex = colorHex,
                                isActive = isActive
                            )
                            onSave(category)
                            onDismiss()
                        },
                        enabled = nameEn.isNotBlank() || nameBn.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_category_btn")
                    ) {
                        Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerModal(
            selectedIconName = iconName,
            onIconSelected = { selected ->
                iconName = selected
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showDeleteConfirmDialog && existingCategory != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(LanguageHelper.getString("delete", languageMode)) },
            text = { Text("Are you sure you want to delete this category? All associated records may be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(existingCategory)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageHelper.getString("delete", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }
}
