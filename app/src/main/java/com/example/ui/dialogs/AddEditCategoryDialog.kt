package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.util.LanguageHelper

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
    var isSubCategory by remember { mutableStateOf(parentId != null) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("add_category_dialog")
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
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                        onClick = { categoryType = CategoryType.EXPENSE },
                        label = { Text(LanguageHelper.getString("expense", languageMode)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = categoryType == CategoryType.INCOME,
                        onClick = { categoryType = CategoryType.INCOME },
                        label = { Text(LanguageHelper.getString("income", languageMode)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Category vs Sub-Category
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
                        shape = RoundedCornerShape(8.dp)
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
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                if (isSubCategory) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = parentDropdownExpanded,
                            onDismissRequest = { parentDropdownExpanded = false }
                        ) {
                            filteredParents.forEach { parent ->
                                DropdownMenuItem(
                                    text = { Text(parent.localizedName(languageMode)) },
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name Bangla
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(LanguageHelper.getString("name_bn", languageMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (categoryType == CategoryType.EXPENSE && !isSubCategory) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetLimitText,
                        onValueChange = { budgetLimitText = it },
                        label = { Text(LanguageHelper.getString("budget_limit", languageMode)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                            onClick = {
                                onDelete(existingCategory)
                                onDismiss()
                            },
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
                                iconName = if (categoryType == CategoryType.INCOME) "Payments" else "ShoppingCart",
                                colorHex = if (categoryType == CategoryType.INCOME) "#10B981" else "#EF4444"
                            )
                            onSave(category)
                            onDismiss()
                        },
                        enabled = nameEn.isNotBlank() || nameBn.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_category_btn")
                    ) {
                        Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
