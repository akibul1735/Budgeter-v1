package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.AddPhotoAlternate
import com.example.ui.components.IconPickerModal
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.SavingsGoal
import com.example.data.model.SavingsGoalWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.*

private val GOAL_COLORS = listOf(
    "#10B981", // Emerald
    "#3B82F6", // Blue
    "#8B5CF6", // Purple
    "#EC4899", // Pink
    "#F59E0B", // Amber
    "#06B6D4", // Cyan
    "#EF4444", // Red
    "#14B8A6", // Teal
    "#6366F1", // Indigo
    "#84CC16"  // Lime
)

private val POPULAR_GOAL_ICONS = listOf(
    "Savings",
    "EmojiEvents",
    "Flight",
    "DirectionsCar",
    "Home",
    "Laptop",
    "School",
    "BeachAccess",
    "Diamond",
    "Favorite",
    "HealthAndSafety",
    "ShoppingCart",
    "Smartphone",
    "Apartment",
    "FitnessCenter",
    "CardGiftcard",
    "Celebration",
    "Pets",
    "AccountBalance",
    "Payments",
    "Star",
    "Work",
    "ChildCare",
    "TwoWheeler",
    "Build",
    "LocalGasStation",
    "FlightTakeoff",
    "CardTravel",
    "Camera",
    "Security",
    "LiveTv",
    "MenuBook"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSavingsGoalDialog(
    goalWithDetails: SavingsGoalWithDetails? = null,
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal, List<Pair<Long, Double>>) -> Unit
) {
    val isEditing = goalWithDetails != null
    val existingGoal = goalWithDetails?.goal

    var name by remember { mutableStateOf(existingGoal?.name ?: "") }
    var nameBn by remember { mutableStateOf(existingGoal?.nameBn ?: "") }
    var targetAmountStr by remember {
        mutableStateOf(if (existingGoal != null && existingGoal.targetAmount > 0) String.format(Locale.US, "%.0f", existingGoal.targetAmount) else "")
    }
    var targetDateMillis by remember { mutableStateOf(existingGoal?.targetDate ?: 0L) }
    var selectedColorHex by remember { mutableStateOf(existingGoal?.colorHex ?: GOAL_COLORS[0]) }
    var selectedIconName by remember { mutableStateOf(existingGoal?.iconName ?: "Savings") }
    var notes by remember { mutableStateOf(existingGoal?.notes ?: "") }
    var showIconPickerModal by remember { mutableStateOf(false) }

    // Map of accountId -> allocated amount
    val allocationsMap = remember {
        val map = mutableStateMapOf<Long, String>()
        goalWithDetails?.allocations?.forEach { allocWithAcc ->
            if (allocWithAcc.allocation.allocatedAmount > 0) {
                map[allocWithAcc.account.id] = String.format(Locale.US, "%.0f", allocWithAcc.allocation.allocatedAmount)
            }
        }
        map
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Flat list of ALL active accounts from the system (including Cash, Bank, Savings, Assets, etc.)
    val allAvailableAccounts = remember(accountsWithBalances) {
        val list = mutableListOf<Pair<Account, Double>>()
        accountsWithBalances.forEach { parent ->
            if (parent.account.isActive) {
                if (parent.subAccounts.isEmpty()) {
                    list.add(parent.account to parent.currentBalance)
                } else {
                    parent.subAccounts.forEach { sub ->
                        if (sub.account.isActive) {
                            list.add(sub.account to sub.currentBalance)
                        }
                    }
                }
            }
        }
        list
    }

    var showAccountPickerDialog by remember { mutableStateOf(false) }

    val totalAllocatedCalculated = allocationsMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val targetAmountVal = targetAmountStr.toDoubleOrNull() ?: 0.0

    val performSave: () -> Unit = {
        if (name.isBlank() && nameBn.isBlank()) {
            errorMessage = if (languageMode == LanguageMode.BANGLA) "লক্ষ্যের নাম লিখুন" else "Please enter a goal name"
        } else if (targetAmountVal <= 0.0) {
            errorMessage = if (languageMode == LanguageMode.BANGLA) "সঠিক লক্ষ্যমাত্রা লিখুন" else "Please enter a valid target amount"
        } else {
            val allocationsList = allocationsMap.mapNotNull { (accId, amtStr) ->
                val amt = amtStr.toDoubleOrNull()
                if (amt != null && amt > 0) accId to amt else null
            }

            val finalGoal = SavingsGoal(
                id = existingGoal?.id ?: 0L,
                name = name.ifBlank { nameBn },
                nameBn = nameBn,
                targetAmount = targetAmountVal,
                targetDate = targetDateMillis,
                colorHex = selectedColorHex,
                iconName = selectedIconName,
                notes = notes,
                isCompleted = existingGoal?.isCompleted ?: false,
                createdAt = existingGoal?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            onSave(finalGoal, allocationsList)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("add_edit_savings_goal_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        windowInsets = WindowInsets(0.dp),
                        title = {
                            Text(
                                text = if (isEditing) {
                                    LanguageHelper.getString("edit_savings_goal", languageMode)
                                } else {
                                    LanguageHelper.getString("dialog_add_goal_title", languageMode)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_goal_dialog_btn")) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("cancel_goal_dialog_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = LanguageHelper.getString("cancel", languageMode),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = performSave,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(42.dp)
                                    .testTag("save_goal_submit_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("save", languageMode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        if (errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Goal Name Fields
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    errorMessage = null
                                },
                                label = { Text(LanguageHelper.getString("name_en", languageMode), fontSize = 12.sp) },
                                placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "যেমন: জরুরি তহবিল, ল্যাপটপ" else "e.g., Emergency Fund, New Laptop", fontSize = 12.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("goal_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp)
                            )

                            OutlinedTextField(
                                value = nameBn,
                                onValueChange = { nameBn = it },
                                label = { Text(LanguageHelper.getString("name_bn", languageMode), fontSize = 12.sp) },
                                placeholder = { Text("যেমন: জরুরি তহবিল", fontSize = 12.5.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("goal_name_bn_input"),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp)
                            )
                        }
                    }

                    // Target Amount & Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Target Amount
                            OutlinedTextField(
                                value = targetAmountStr,
                                onValueChange = {
                                    targetAmountStr = it
                                    errorMessage = null
                                },
                                label = {
                                    Text(
                                        text = LanguageHelper.getString("target_amount", languageMode),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 11.5.sp
                                    )
                                },
                                placeholder = { Text("50000", fontSize = 13.sp) },
                                prefix = {
                                    Text(
                                        text = LanguageHelper.activeCurrencyConfig.activeSymbol + " ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("goal_target_amount_input"),
                                shape = RoundedCornerShape(10.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            )

                            // Target Date Picker
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = if (targetDateMillis > 0L) {
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(targetDateMillis))
                                    } else "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text(
                                            text = LanguageHelper.getString("target_date", languageMode),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.5.sp
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "তারিখ বাছাই" else "Select date",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 12.5.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (targetDateMillis > 0L) {
                                            IconButton(
                                                onClick = { targetDateMillis = 0L },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Clear date",
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("goal_date_picker_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )
                                // Clickable overlay across the field (excluding trailing clear button)
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding(end = if (targetDateMillis > 0L) 36.dp else 0.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showDatePicker = true }
                                )
                            }
                        }
                    }

                    // Visual Styling: Color & Icon Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আইকন ও থিম কালার" else "Icon & Theme Color",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Color picker row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                items(GOAL_COLORS) { colorHex ->
                                    val parsedColor = try {
                                        Color(android.graphics.Color.parseColor(colorHex))
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(parsedColor)
                                            .clickable { selectedColorHex = colorHex }
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }

                            // Icon picker row with + Custom / More options
                            val displayIcons = remember(selectedIconName) {
                                if (selectedIconName !in POPULAR_GOAL_ICONS) {
                                    listOf(selectedIconName) + POPULAR_GOAL_ICONS
                                } else {
                                    POPULAR_GOAL_ICONS
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                // Add Custom / More Icons Button
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clickable { showIconPickerModal = true }
                                            .testTag("goal_more_icons_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "+ আরও" else "+ More",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                items(displayIcons) { iconKey ->
                                    val isSelected = selectedIconName == iconKey
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable { selectedIconName = iconKey }
                                            .testTag("goal_icon_$iconKey")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            IconHelper.AppIcon(
                                                iconName = iconKey,
                                                contentDescription = iconKey,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Multi-Account Allocation Section
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = LanguageHelper.getString("linked_accounts", languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) {
                                                "হিসাব থেকে অর্থ বরাদ্দ করুন"
                                            } else {
                                                "Assign portions from accounts"
                                            },
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (totalAllocatedCalculated >= targetAmountVal && targetAmountVal > 0) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = "${LanguageHelper.formatCurrency(totalAllocatedCalculated, languageMode)} / ${LanguageHelper.formatCurrency(targetAmountVal, languageMode)}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (totalAllocatedCalculated >= targetAmountVal && targetAmountVal > 0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            },
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (allAvailableAccounts.isEmpty()) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় হিসাব পাওয়া যায়নি।" else "No active accounts found in the app.",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    val linkedAccounts = allAvailableAccounts.filter { it.first.id in allocationsMap.keys }
                                    val unlinkedAccounts = allAvailableAccounts.filter { it.first.id !in allocationsMap.keys }

                                    if (linkedAccounts.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো হিসাব এখনো সংযুক্ত করা হয়নি।" else "No accounts linked yet.",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "নিচের বোতাম চেপে বিদ্যমান যেকোনো হিসাব নির্বাচন করে তহবিল বরাদ্দ করুন।" else "Select an existing account below to allocate funds to this goal.",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        linkedAccounts.forEach { (acc, currentBal) ->
                                            val currentAllocStr = allocationsMap[acc.id] ?: ""
                                            val currentAllocVal = currentAllocStr.toDoubleOrNull() ?: 0.0
                                            val isOverBalance = currentAllocVal > currentBal

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isOverBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Account Header Row
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(
                                                                imageVector = IconHelper.getIconByName(acc.iconName),
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode),
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 13.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = "${if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স:" else "Live Bal:"} ${LanguageHelper.formatCurrency(currentBal, languageMode)}",
                                                                    fontSize = 10.5.sp,
                                                                    color = if (isOverBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }

                                                        // Unlink/Remove Button
                                                        IconButton(
                                                            onClick = { allocationsMap.remove(acc.id) },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove account",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }

                                                    // Allocation Input Row
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        OutlinedTextField(
                                                            value = currentAllocStr,
                                                            onValueChange = { newVal ->
                                                                allocationsMap[acc.id] = newVal
                                                            },
                                                            label = {
                                                                Text(
                                                                    text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দ পরিমাণ" else "Allocated Amount",
                                                                    fontSize = 10.5.sp
                                                                )
                                                            },
                                                            placeholder = { Text("0", fontSize = 13.sp) },
                                                            prefix = {
                                                                Text(
                                                                    text = LanguageHelper.activeCurrencyConfig.activeSymbol + " ",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            textStyle = LocalTextStyle.current.copy(
                                                                fontSize = 13.5.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        )

                                                        // Quick 100% / Full balance shortcut
                                                        if (currentBal > 0) {
                                                            OutlinedButton(
                                                                onClick = {
                                                                    val remaining = maxOf(0.0, targetAmountVal - (totalAllocatedCalculated - currentAllocVal))
                                                                    val fillAmt = if (remaining in 0.01..currentBal) remaining else currentBal
                                                                    allocationsMap[acc.id] = String.format(Locale.US, "%.0f", fillAmt)
                                                                },
                                                                shape = RoundedCornerShape(8.dp),
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                                modifier = Modifier.height(48.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (languageMode == LanguageMode.BANGLA) "পূর্ণ" else "Fill",
                                                                    fontSize = 11.5.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (isOverBalance) {
                                                        Text(
                                                            text = if (languageMode == LanguageMode.BANGLA) "সতর্কতা: বরাদ্দ পরিমাণ বর্তমান ব্যালেন্সের চেয়ে বেশি!" else "Warning: Allocated amount exceeds account balance!",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.error,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Account Selection Searchable Button
                                    if (unlinkedAccounts.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { showAccountPickerDialog = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (linkedAccounts.isEmpty()) {
                                                    if (languageMode == LanguageMode.BANGLA) "হিসাব খুঁজুন ও যুক্ত করুন" else "Search & Link Account"
                                                } else {
                                                    if (languageMode == LanguageMode.BANGLA) "+ আরও হিসাব খুঁজুন ও যুক্ত করুন" else "+ Search & Link Another Account"
                                                },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                    } else if (allAvailableAccounts.isNotEmpty()) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সমস্ত বিদ্যমান হিসাব সংযুক্ত রয়েছে।" else "All existing accounts are currently linked.",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Notes
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(LanguageHelper.getString("notes", languageMode), fontSize = 11.5.sp) },
                            placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত বিবরণ বা মন্তব্য..." else "Additional notes...", fontSize = 12.sp) },
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_notes_input"),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (targetDateMillis > 0L) targetDateMillis else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDateMillis = millis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(LanguageHelper.getString("done", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAccountPickerDialog) {
        SearchableAccountPickerDialog(
            accountsWithBalances = accountsWithBalances,
            excludedAccountIds = allocationsMap.keys,
            languageMode = languageMode,
            title = if (languageMode == LanguageMode.BANGLA) "হিসাব খুঁজুন ও নির্বাচন করুন" else "Search & Select Account",
            onAccountSelected = { acc, bal ->
                showAccountPickerDialog = false
                val remainingTarget = maxOf(0.0, targetAmountVal - totalAllocatedCalculated)
                val suggestedAmt = if (remainingTarget > 0) {
                    if (bal > 0) minOf(remainingTarget, bal) else remainingTarget
                } else 0.0

                allocationsMap[acc.id] = if (suggestedAmt > 0) String.format(Locale.US, "%.0f", suggestedAmt) else ""
            },
            onDismiss = { showAccountPickerDialog = false }
        )
    }

    if (showIconPickerModal) {
        IconPickerModal(
            selectedIconName = selectedIconName,
            initialQuery = name.ifBlank { nameBn },
            onIconSelected = { newIcon ->
                selectedIconName = newIcon
                showIconPickerModal = false
            },
            onDismiss = { showIconPickerModal = false }
        )
    }
}
