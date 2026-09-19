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

private val GOAL_ICONS = listOf(
    "Savings" to Icons.Default.Savings,
    "EmojiEvents" to Icons.Default.EmojiEvents,
    "Flight" to Icons.Default.Flight,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Home" to Icons.Default.Home,
    "Laptop" to Icons.Default.Laptop,
    "School" to Icons.Default.School,
    "BeachAccess" to Icons.Default.BeachAccess,
    "Diamond" to Icons.Default.Diamond,
    "Favorite" to Icons.Default.Favorite,
    "HealthAndSafety" to Icons.Default.HealthAndSafety,
    "ShoppingCart" to Icons.Default.ShoppingCart
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_savings_goal_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isEditing) {
                                    LanguageHelper.getString("edit_savings_goal", languageMode)
                                } else {
                                    LanguageHelper.getString("dialog_add_goal_title", languageMode)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.5.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_goal_dialog_btn")) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {
                                    if (name.isBlank() && nameBn.isBlank()) {
                                        errorMessage = if (languageMode == LanguageMode.BANGLA) "লক্ষ্যের নাম লিখুন" else "Please enter a goal name"
                                        return@Button
                                    }
                                    if (targetAmountVal <= 0.0) {
                                        errorMessage = if (languageMode == LanguageMode.BANGLA) "সঠিক লক্ষ্যমাত্রা লিখুন" else "Please enter a valid target amount"
                                        return@Button
                                    }

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
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("save_goal_submit_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    errorMessage = null
                                },
                                label = { Text(LanguageHelper.getString("name_en", languageMode)) },
                                placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "যেমন: জরুরি তহবিল, ল্যাপটপ" else "e.g., Emergency Fund, New Laptop") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("goal_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = nameBn,
                                onValueChange = { nameBn = it },
                                label = { Text(LanguageHelper.getString("name_bn", languageMode)) },
                                placeholder = { Text("যেমন: জরুরি তহবিল") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("goal_name_bn_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Target Amount & Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Target Amount (56dp height & 1f weight)
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
                                placeholder = { Text("50000") },
                                leadingIcon = {
                                    Text(
                                        LanguageHelper.activeCurrencyConfig.activeSymbol,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("goal_target_amount_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Target Date Picker Button (56dp height & 1f weight)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clickable { showDatePicker = true }
                                    .testTag("goal_date_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f, fill = false),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = LanguageHelper.getString("target_date", languageMode),
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (targetDateMillis > 0L) {
                                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(targetDateMillis))
                                            } else {
                                                if (languageMode == LanguageMode.BANGLA) "নির্ধারিত নয়" else "No deadline"
                                            },
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (targetDateMillis > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    if (targetDateMillis > 0L) {
                                        IconButton(
                                            onClick = { targetDateMillis = 0L },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear date", modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Visual Styling: Color & Icon Selector
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আইকন ও থিম কালার" else "Icon & Theme Color",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Color picker row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(parsedColor)
                                            .clickable { selectedColorHex = colorHex }
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            // Icon picker row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(GOAL_ICONS) { (iconKey, iconVector) ->
                                    val isSelected = selectedIconName == iconKey
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clickable { selectedIconName = iconKey }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = iconVector,
                                                contentDescription = iconKey,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = LanguageHelper.getString("linked_accounts", languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) {
                                                "হিসাব থেকে অর্থ বরাদ্দ করুন"
                                            } else {
                                                "Assign portions from accounts"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (totalAllocatedCalculated >= targetAmountVal && targetAmountVal > 0) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = "${LanguageHelper.formatCurrency(totalAllocatedCalculated, languageMode)} / ${LanguageHelper.formatCurrency(targetAmountVal, languageMode)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (totalAllocatedCalculated >= targetAmountVal && targetAmountVal > 0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            },
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (allAvailableAccounts.isEmpty()) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় হিসাব পাওয়া যায়নি।" else "No active accounts found in the app.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    val linkedAccounts = allAvailableAccounts.filter { it.first.id in allocationsMap.keys }
                                    val unlinkedAccounts = allAvailableAccounts.filter { it.first.id !in allocationsMap.keys }

                                    if (linkedAccounts.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো হিসাব এখনো সংযুক্ত করা হয়নি।" else "No accounts linked yet.",
                                                    fontSize = 12.5.sp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "নিচের বোতাম চেপে বিদ্যমান যেকোনো হিসাব নির্বাচন করে তহবিল বরাদ্দ করুন।" else "Select an existing account below to allocate funds to this goal.",
                                                    fontSize = 11.5.sp,
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
                                                shape = RoundedCornerShape(14.dp),
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
                                                        .padding(12.dp),
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
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(
                                                                imageVector = IconHelper.getIconByName(acc.iconName),
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode),
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    fontSize = 13.5.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = "${if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স:" else "Live Bal:"} ${LanguageHelper.formatCurrency(currentBal, languageMode)}",
                                                                    fontSize = 11.sp,
                                                                    color = if (isOverBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }

                                                        // Unlink/Remove Button
                                                        IconButton(
                                                            onClick = { allocationsMap.remove(acc.id) },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove account",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
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
                                                            label = { Text(if (languageMode == LanguageMode.BANGLA) "বরাদ্দ পরিমাণ" else "Allocated Amount", fontSize = 11.sp) },
                                                            placeholder = { Text("0") },
                                                            leadingIcon = {
                                                                Text(
                                                                    LanguageHelper.activeCurrencyConfig.activeSymbol,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.padding(start = 8.dp)
                                                                )
                                                            },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            singleLine = true,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(52.dp),
                                                            shape = RoundedCornerShape(10.dp),
                                                            textStyle = LocalTextStyle.current.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
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
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(48.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (languageMode == LanguageMode.BANGLA) "পূর্ণ" else "Fill",
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (isOverBalance) {
                                                        Text(
                                                            text = if (languageMode == LanguageMode.BANGLA) "সতর্কতা: বরাদ্দ পরিমাণ বর্তমান ব্যালেন্সের চেয়ে বেশি!" else "Warning: Allocated amount exceeds account balance!",
                                                            fontSize = 10.5.sp,
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
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (linkedAccounts.isEmpty()) {
                                                    if (languageMode == LanguageMode.BANGLA) "হিসাব খুঁজুন ও যুক্ত করুন" else "Search & Link Account"
                                                } else {
                                                    if (languageMode == LanguageMode.BANGLA) "+ আরও হিসাব খুঁজুন ও যুক্ত করুন" else "+ Search & Link Another Account"
                                                },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    } else if (allAvailableAccounts.isNotEmpty()) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সমস্ত বিদ্যমান হিসাব সংযুক্ত রয়েছে।" else "All existing accounts are currently linked.",
                                            fontSize = 11.sp,
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
                            label = { Text(LanguageHelper.getString("notes", languageMode)) },
                            placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত বিবরণ বা মন্তব্য..." else "Additional notes...") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_notes_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
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
        val unlinkedAccounts = allAvailableAccounts.filter { it.first.id !in allocationsMap.keys }
        SearchableAccountPickerDialog(
            accountsWithBalances = unlinkedAccounts,
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
}
