package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.WishlistItem
import com.example.data.model.WishlistPriority
import com.example.data.model.WishlistTargetType
import com.example.ui.components.PopupCalculatorDialog
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditWishlistDialog(
    item: WishlistItem? = null,
    categories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (WishlistItem) -> Unit
) {
    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-12
    
    val nextCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    val nextYear = nextCal.get(Calendar.YEAR)
    val nextMonth = nextCal.get(Calendar.MONTH) + 1

    var title by remember { mutableStateOf(item?.title ?: "") }
    var amountText by remember { mutableStateOf(item?.let { if (it.estimatedAmount > 0) it.estimatedAmount.toString() else "" } ?: "") }
    var selectedCategoryId by remember { mutableStateOf(item?.categoryId) }
    var selectedSubCategoryId by remember { mutableStateOf(item?.subCategoryId) }
    
    var targetType by remember { mutableStateOf(item?.targetType ?: WishlistTargetType.NEXT_MONTH) }
    var targetYear by remember { mutableIntStateOf(item?.targetYear ?: nextYear) }
    var targetMonth by remember { mutableIntStateOf(item?.targetMonth ?: nextMonth) }
    
    var priority by remember { mutableStateOf(item?.priority ?: WishlistPriority.MEDIUM) }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    var url by remember { mutableStateOf(item?.url ?: "") }
    
    var showCalculator by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val expenseCategories = remember(categories) {
        categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null }
    }
    val allSubCategories = remember(categories) {
        categories.filter { it.parentId != null }
    }

    val selectedCat = categories.firstOrNull { it.id == selectedCategoryId }
    val subCategoriesForSelected = allSubCategories.filter { it.parentId == selectedCategoryId }
    val selectedSubCat = categories.firstOrNull { it.id == selectedSubCategoryId }

    val monthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthNamesBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    if (showCalculator) {
        PopupCalculatorDialog(
            initialValue = amountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showCalculator = false },
            onValueConfirmed = { result ->
                amountText = if (result % 1.0 == 0.0) result.toLong().toString() else String.format(java.util.Locale.US, "%.2f", result)
                showCalculator = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = if (item == null) {
                                LanguageHelper.getString("add_to_wishlist", languageMode)
                            } else {
                                LanguageHelper.getString("edit_wishlist_item", languageMode)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = LanguageHelper.getString("cancel", languageMode)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Name
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(LanguageHelper.getString("item_name", languageMode)) },
                    placeholder = { Text("e.g. Sony WH-1000XM5, Ergonomic Chair") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wishlist_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                if (titleError) {
                    Text(
                        text = "Please enter an item title",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estimated Cost & Calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            if (it.isNotBlank()) amountError = false
                        },
                        label = { Text(LanguageHelper.getString("estimated_cost", languageMode)) },
                        placeholder = { Text("0.00") },
                        prefix = {
                            Text(
                                text = LanguageHelper.activeCurrencyConfig.activeSymbol,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wishlist_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = { showCalculator = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (amountError) {
                    Text(
                        text = "Please enter a valid amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text(
                    text = LanguageHelper.getString("categories", languageMode),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = OutlinedTextFieldDefaults.colors().run {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (selectedCat != null) {
                                    val catIcon = IconHelper.getIconByName(selectedCat.iconName)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                runCatching { Color(android.graphics.Color.parseColor(selectedCat.colorHex)) }
                                                    .getOrDefault(MaterialTheme.colorScheme.primary).copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = runCatching { Color(android.graphics.Color.parseColor(selectedCat.colorHex)) }
                                                .getOrDefault(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Text(
                                        text = LanguageHelper.getLocalizedName(selectedCat.nameEn, selectedCat.nameBn, languageMode),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = LanguageHelper.getString("select_category", languageMode),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (selectedCat != null) {
                                IconButton(
                                    onClick = {
                                        selectedCategoryId = null
                                        selectedSubCategoryId = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val cIcon = IconHelper.getIconByName(cat.iconName)
                                        Icon(
                                            imageVector = cIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }
                                                .getOrDefault(MaterialTheme.colorScheme.primary)
                                        )
                                        Text(LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode))
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedSubCategoryId = null
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Subcategory selection if available
                if (subCategoriesForSelected.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subCategoriesForSelected.forEach { subCat ->
                            val isSubSelected = selectedSubCategoryId == subCat.id
                            FilterChip(
                                selected = isSubSelected,
                                onClick = {
                                    selectedSubCategoryId = if (isSubSelected) null else subCat.id
                                },
                                label = {
                                    Text(
                                        text = LanguageHelper.getLocalizedName(subCat.nameEn, subCat.nameBn, languageMode),
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Planning Timeline
                Text(
                    text = LanguageHelper.getString("planned_timeline", languageMode),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = targetType == WishlistTargetType.NEXT_MONTH,
                        onClick = {
                            targetType = WishlistTargetType.NEXT_MONTH
                            targetYear = nextYear
                            targetMonth = nextMonth
                        },
                        label = {
                            val nextMName = if (languageMode == LanguageMode.BANGLA) monthNamesBn[nextMonth - 1] else monthNamesEn[nextMonth - 1]
                            Text("⚡ " + LanguageHelper.getString("next_month", languageMode) + " ($nextMName)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SPECIFIC_MONTH,
                        onClick = { targetType = WishlistTargetType.SPECIFIC_MONTH },
                        label = { Text("📅 " + LanguageHelper.getString("specific_month", languageMode)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SAVINGS_GOAL,
                        onClick = { targetType = WishlistTargetType.SAVINGS_GOAL },
                        label = { Text("🎯 " + LanguageHelper.getString("save_up_goal", languageMode)) }
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SOMEDAY,
                        onClick = { targetType = WishlistTargetType.SOMEDAY },
                        label = { Text("💭 " + LanguageHelper.getString("someday_maybe", languageMode)) }
                    )
                }

                // Month/Year pickers for Specific Month
                AnimatedVisibility(visible = targetType == WishlistTargetType.SPECIFIC_MONTH) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Month Dropdown
                            Box(modifier = Modifier.weight(1.5f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showMonthDropdown = true }
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) monthNamesBn[targetMonth - 1] else monthNamesEn[targetMonth - 1],
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showMonthDropdown,
                                    onDismissRequest = { showMonthDropdown = false }
                                ) {
                                    for (m in 1..12) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(if (languageMode == LanguageMode.BANGLA) monthNamesBn[m - 1] else monthNamesEn[m - 1])
                                            },
                                            onClick = {
                                                targetMonth = m
                                                showMonthDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Year Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showYearDropdown = true }
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = targetYear.toString(),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showYearDropdown,
                                    onDismissRequest = { showYearDropdown = false }
                                ) {
                                    for (y in currentYear..(currentYear + 5)) {
                                        DropdownMenuItem(
                                            text = { Text(y.toString()) },
                                            onClick = {
                                                targetYear = y
                                                showYearDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority
                Text(
                    text = LanguageHelper.getString("priority", languageMode),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = priority == WishlistPriority.HIGH,
                        onClick = { priority = WishlistPriority.HIGH },
                        label = { Text("🔥 " + LanguageHelper.getString("priority_high", languageMode)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFDC2626)
                        )
                    )
                    FilterChip(
                        selected = priority == WishlistPriority.MEDIUM,
                        onClick = { priority = WishlistPriority.MEDIUM },
                        label = { Text("⭐ " + LanguageHelper.getString("priority_medium", languageMode)) }
                    )
                    FilterChip(
                        selected = priority == WishlistPriority.LOW,
                        onClick = { priority = WishlistPriority.LOW },
                        label = { Text("💤 " + LanguageHelper.getString("priority_low", languageMode)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(LanguageHelper.getString("notes", languageMode)) },
                    placeholder = { Text("Why you need it, model number, specifications...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(LanguageHelper.getString("product_link", languageMode)) },
                    placeholder = { Text("https://...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }

                    Button(
                        onClick = {
                            var hasError = false
                            if (title.isBlank()) {
                                titleError = true
                                hasError = true
                            }
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                amountError = true
                                hasError = true
                            }
                            if (!hasError && amt != null) {
                                val finalYear = if (targetType == WishlistTargetType.NEXT_MONTH) nextYear else if (targetType == WishlistTargetType.SPECIFIC_MONTH) targetYear else null
                                val finalMonth = if (targetType == WishlistTargetType.NEXT_MONTH) nextMonth else if (targetType == WishlistTargetType.SPECIFIC_MONTH) targetMonth else null
                                
                                val newItem = (item ?: WishlistItem(title = title, estimatedAmount = amt)).copy(
                                    title = title.trim(),
                                    estimatedAmount = amt,
                                    categoryId = selectedCategoryId,
                                    subCategoryId = selectedSubCategoryId,
                                    targetType = targetType,
                                    targetYear = finalYear,
                                    targetMonth = finalMonth,
                                    priority = priority,
                                    notes = notes.trim(),
                                    url = url.trim(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(newItem)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("wishlist_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageHelper.getString("save", languageMode),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
