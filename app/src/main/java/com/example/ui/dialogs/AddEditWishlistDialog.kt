package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
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
    onSave: (WishlistItem) -> Unit,
    onAddNewCategory: ((Category) -> Unit)? = null
) {
    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-12
    
    val nextCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
    val nextYear = nextCal.get(Calendar.YEAR)
    val nextMonth = nextCal.get(Calendar.MONTH) + 1

    var title by remember { mutableStateOf(item?.title ?: "") }
    var amountText by remember {
        mutableStateOf(item?.let { if (it.estimatedAmount > 0) {
            if (it.estimatedAmount % 1.0 == 0.0) it.estimatedAmount.toLong().toString() else it.estimatedAmount.toString()
        } else "" } ?: "")
    }
    var selectedCategoryId by remember { mutableStateOf(item?.categoryId) }
    var selectedSubCategoryId by remember { mutableStateOf(item?.subCategoryId) }
    
    var targetType by remember { mutableStateOf(item?.targetType ?: WishlistTargetType.NEXT_MONTH) }
    var targetYear by remember { mutableIntStateOf(item?.targetYear ?: nextYear) }
    var targetMonth by remember { mutableIntStateOf(item?.targetMonth ?: nextMonth) }
    
    var priority by remember { mutableStateOf(item?.priority ?: WishlistPriority.MEDIUM) }
    var notes by remember { mutableStateOf(item?.notes ?: "") }
    var url by remember { mutableStateOf(item?.url ?: "") }
    
    var showCalculator by remember { mutableStateOf(false) }
    var showCategoryPickerModal by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val monthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthNamesBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    // Resolved category information
    val parentCat = categories.firstOrNull { it.id == selectedCategoryId }
    val childCat = categories.firstOrNull { it.id == selectedSubCategoryId } ?: parentCat
    val activeCat = childCat ?: parentCat

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

    if (showCategoryPickerModal) {
        CategoryPickerModalDialog(
            categories = categories,
            txType = TransactionType.EXPENSE,
            selectedCategoryId = selectedCategoryId,
            selectedSubCategoryId = selectedSubCategoryId,
            languageMode = languageMode,
            onCategorySelected = { parentCatId, subCatId ->
                selectedCategoryId = parentCatId
                selectedSubCategoryId = subCatId
                showCategoryPickerModal = false
            },
            onAddNewCategory = { newCat ->
                onAddNewCategory?.invoke(newCat)
                if (newCat.parentId != null) {
                    selectedCategoryId = newCat.parentId
                    selectedSubCategoryId = newCat.id
                } else {
                    selectedCategoryId = newCat.id
                    selectedSubCategoryId = null
                }
                showCategoryPickerModal = false
            },
            onDismiss = { showCategoryPickerModal = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .wrapContentHeight()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (item == null) {
                                LanguageHelper.getString("add_to_wishlist", languageMode)
                            } else {
                                LanguageHelper.getString("edit_wishlist_item", languageMode)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = LanguageHelper.getString("cancel", languageMode),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item Name
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(LanguageHelper.getString("item_name", languageMode), fontSize = 12.sp) },
                    placeholder = { Text("e.g. Headphones, Office Chair", fontSize = 12.sp) },
                    isError = titleError,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wishlist_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                if (titleError) {
                    Text(
                        text = "Please enter an item title",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        label = { Text(LanguageHelper.getString("estimated_cost", languageMode), fontSize = 12.sp) },
                        placeholder = { Text("0.00", fontSize = 12.sp) },
                        prefix = {
                            Text(
                                text = LanguageHelper.activeCurrencyConfig.activeSymbol + " ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wishlist_amount_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCalculator = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                if (amountError) {
                    Text(
                        text = "Please enter a valid estimated amount",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selection Card (Matches Transaction Form style)
                Text(
                    text = LanguageHelper.getString("categories", languageMode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showCategoryPickerModal = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            if (activeCat != null) {
                                val catIcon = IconHelper.getIconByName(activeCat.iconName)
                                val parsedColor = runCatching { Color(android.graphics.Color.parseColor(activeCat.colorHex)) }
                                    .getOrDefault(MaterialTheme.colorScheme.primary)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = parsedColor
                                    )
                                }
                                val catTitle = if (parentCat != null && childCat != null && parentCat.id != childCat.id) {
                                    "${parentCat.localizedName(languageMode)} > ${childCat.localizedName(languageMode)}"
                                } else {
                                    activeCat.localizedName(languageMode)
                                }
                                Text(
                                    text = catTitle,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = LanguageHelper.getString("select_category", languageMode).ifEmpty { "Select Category" },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (activeCat != null) {
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Planning Timeline Chips
                Text(
                    text = LanguageHelper.getString("planned_timeline", languageMode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Text("⚡ " + LanguageHelper.getString("next_month", languageMode) + " ($nextMName)", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.height(30.dp)
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SPECIFIC_MONTH,
                        onClick = { targetType = WishlistTargetType.SPECIFIC_MONTH },
                        label = { Text("📅 " + LanguageHelper.getString("specific_month", languageMode), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.height(30.dp)
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SAVINGS_GOAL,
                        onClick = { targetType = WishlistTargetType.SAVINGS_GOAL },
                        label = { Text("🎯 " + LanguageHelper.getString("save_up_goal", languageMode), fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp)
                    )

                    FilterChip(
                        selected = targetType == WishlistTargetType.SOMEDAY,
                        onClick = { targetType = WishlistTargetType.SOMEDAY },
                        label = { Text("💭 " + LanguageHelper.getString("someday_maybe", languageMode), fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp)
                    )
                }

                // Month/Year pickers for Specific Month
                AnimatedVisibility(visible = targetType == WishlistTargetType.SPECIFIC_MONTH) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Dropdown
                        Box(modifier = Modifier.weight(1.3f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMonthDropdown = true }
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) monthNamesBn[targetMonth - 1] else monthNamesEn[targetMonth - 1],
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
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
                                            Text(if (languageMode == LanguageMode.BANGLA) monthNamesBn[m - 1] else monthNamesEn[m - 1], fontSize = 13.sp)
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
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showYearDropdown = true }
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = targetYear.toString(),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                for (y in currentYear..(currentYear + 5)) {
                                    DropdownMenuItem(
                                        text = { Text(y.toString(), fontSize = 13.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Priority
                Text(
                    text = LanguageHelper.getString("priority", languageMode),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = priority == WishlistPriority.HIGH,
                        onClick = { priority = WishlistPriority.HIGH },
                        label = { Text("🔥 " + LanguageHelper.getString("priority_high", languageMode), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.18f),
                            selectedLabelColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                    )
                    FilterChip(
                        selected = priority == WishlistPriority.MEDIUM,
                        onClick = { priority = WishlistPriority.MEDIUM },
                        label = { Text("⭐ " + LanguageHelper.getString("priority_medium", languageMode), fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                    )
                    FilterChip(
                        selected = priority == WishlistPriority.LOW,
                        onClick = { priority = WishlistPriority.LOW },
                        label = { Text("💤 " + LanguageHelper.getString("priority_low", languageMode), fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(LanguageHelper.getString("notes", languageMode), fontSize = 12.sp) },
                    placeholder = { Text("Optional notes or specifications...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 13.sp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(LanguageHelper.getString("product_link", languageMode), fontSize = 12.sp) },
                    placeholder = { Text("https://...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(LanguageHelper.getString("cancel", languageMode), fontSize = 13.sp)
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
                            .weight(1.4f)
                            .height(40.dp)
                            .testTag("wishlist_save_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageHelper.getString("save", languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
