package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.AccountObligation
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

@Composable
internal fun CategoryAccountSplitDialog(
    categoryAllocation: CategoryAllocationAnalysis,
    paymentSourceAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Map<Long, Double>) -> Unit
) {
    val totalBudget = categoryAllocation.totalBudgeted
    val isExpense = categoryAllocation.isExpense

    // Mode: 0 = Single Source (100%), 1 = Multi-Source Split
    var assignmentMode by remember {
        mutableStateOf(if (categoryAllocation.isMultiAccount || categoryAllocation.accountSplits.size > 1) 1 else 0)
    }

    // Single source selection state
    var selectedSingleAccountId by remember {
        mutableStateOf(
            categoryAllocation.accountSplits.firstOrNull()?.account?.id
                ?: paymentSourceAccounts.firstOrNull()?.id
                ?: 0L
        )
    }

    // Editable budget amount
    var budgetInputText by remember {
        mutableStateOf(if (totalBudget > 0.0) String.format("%.2f", totalBudget) else "")
    }

    // Multi-split state
    var isPercentageMode by remember { mutableStateOf(false) }

    val allocMap = remember {
        val map = mutableStateMapOf<Long, String>()
        categoryAllocation.accountSplits.forEach { split ->
            map[split.account.id] = if (split.allocatedAmount > 0) String.format("%.2f", split.allocatedAmount) else "0.00"
        }
        if (map.isEmpty() && paymentSourceAccounts.isNotEmpty()) {
            map[paymentSourceAccounts.first().id] = if (totalBudget > 0) String.format("%.2f", totalBudget) else "0.00"
        }
        map
    }

    val pctMap = remember {
        val map = mutableStateMapOf<Long, String>()
        allocMap.forEach { (accId, amtStr) ->
            val amt = amtStr.toDoubleOrNull() ?: 0.0
            val pct = if (totalBudget > 0) (amt / totalBudget) * 100.0 else 0.0
            map[accId] = if (pct > 0) String.format("%.1f", pct) else "0.0"
        }
        map
    }

    fun getEffectiveBudget(): Double {
        return budgetInputText.toDoubleOrNull() ?: totalBudget
    }

    fun recalculateAmountsFromPercentages() {
        val currBudget = getEffectiveBudget()
        pctMap.forEach { (accId, pctStr) ->
            val pct = pctStr.toDoubleOrNull() ?: 0.0
            val amt = (pct / 100.0) * currBudget
            allocMap[accId] = if (amt > 0) String.format("%.2f", amt) else "0.00"
        }
    }

    fun recalculatePercentagesFromAmounts() {
        val currBudget = getEffectiveBudget()
        allocMap.forEach { (accId, amtStr) ->
            val amt = amtStr.toDoubleOrNull() ?: 0.0
            val pct = if (currBudget > 0) (amt / currBudget) * 100.0 else 0.0
            pctMap[accId] = if (pct > 0) String.format("%.1f", pct) else "0.0"
        }
    }

    fun splitEqually() {
        val count = allocMap.size
        val currBudget = getEffectiveBudget()
        if (count > 0) {
            val shareAmt = if (currBudget > 0) currBudget / count else 0.0
            val sharePct = 100.0 / count
            allocMap.keys.toList().forEach { accId ->
                allocMap[accId] = if (shareAmt > 0) String.format("%.2f", shareAmt) else "0.00"
                pctMap[accId] = String.format("%.1f", sharePct)
            }
        }
    }

    val catColor = remember(categoryAllocation.category.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(categoryAllocation.category.colorHex))
        } catch (e: Exception) {
            if (isExpense) SolidExpense else SolidIncome
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(8.dp)
                .testTag("dialog_category_split"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header: Category Icon, Name, and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(categoryAllocation.category.iconName),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = categoryAllocation.category.localizedName(languageMode),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isExpense) (if (languageMode == LanguageMode.BANGLA) "ব্যয় ক্যাটাগরি" else "Expense Category")
                                else (if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি" else "Income Category"),
                                fontSize = 11.sp,
                                color = if (isExpense) SolidExpense else SolidIncome,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Budget Input Field
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "মাসিক বাজেট / পরিমাণ (৳):" else "Monthly Budget / Target (৳):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = budgetInputText,
                            onValueChange = { input ->
                                budgetInputText = input
                                if (assignmentMode == 1) {
                                    if (isPercentageMode) recalculateAmountsFromPercentages()
                                    else recalculatePercentagesFromAmounts()
                                }
                            },
                            placeholder = { Text("0.00", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 12.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs: Single Source vs Multi-Source Split
                TabRow(
                    selectedTabIndex = assignmentMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[assignmentMode]),
                            color = SolidPrimary
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = assignmentMode == 0,
                        onClick = {
                            assignmentMode = 0
                            if (selectedSingleAccountId == 0L && paymentSourceAccounts.isNotEmpty()) {
                                selectedSingleAccountId = paymentSourceAccounts.first().id
                            }
                        },
                        text = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "একটি সোর্স (১০০%)" else "Single Source (100%)",
                                fontSize = 11.sp,
                                fontWeight = if (assignmentMode == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = assignmentMode == 1,
                        onClick = {
                            assignmentMode = 1
                            if (allocMap.isEmpty() && selectedSingleAccountId > 0) {
                                val currBudget = getEffectiveBudget()
                                allocMap[selectedSingleAccountId] = if (currBudget > 0) String.format("%.2f", currBudget) else "0.00"
                                pctMap[selectedSingleAccountId] = "100.0"
                            }
                        },
                        text = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "একাধিক সোর্সে বিভক্ত" else "Split Across Sources",
                                fontSize = 11.sp,
                                fontWeight = if (assignmentMode == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                if (assignmentMode == 0) {
                    // MODE 0: Single Source (100%) selection
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "এই ক্যাটাগরির জন্য মূল পেমেন্ট সোর্স নির্বাচন করুন:" else "Select the primary payment source for this category:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Option for No Source Account (Unassigned)
                        item {
                            val isNoneSelected = selectedSingleAccountId == 0L
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSingleAccountId = 0L },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNoneSelected) SolidExpense.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isNoneSelected) SolidExpense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                tint = if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "কোনো সোর্স নেই (আনঅ্যাসাইন)" else "No Source Account (Unassigned)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "এই ক্যাটাগরিতে কোনো সোর্স অ্যাকাউন্ট নির্ধারিত থাকবে না" else "No payment source account will be assigned",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    if (isNoneSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SolidExpense,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(paymentSourceAccounts, key = { it.id }) { acc ->
                            val isSelected = acc.id == selectedSingleAccountId
                            val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSingleAccountId = acc.id },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = acc.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(if (isImg) 32.dp else 18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = acc.localizedName(languageMode),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (acc.accountNumber.isNotBlank()) {
                                                Text(
                                                    text = acc.accountNumber,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SolidPrimary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // MODE 1: Multi-Source Split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (!isPercentageMode) SolidPrimary else Color.Transparent)
                                        .clickable {
                                            if (isPercentageMode) {
                                                isPercentageMode = false
                                                recalculatePercentagesFromAmounts()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "৳ পরিমাণ" else "৳ Amount",
                                        fontSize = 11.sp,
                                        fontWeight = if (!isPercentageMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isPercentageMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPercentageMode) SolidPrimary else Color.Transparent)
                                        .clickable {
                                            if (!isPercentageMode) {
                                                isPercentageMode = true
                                                recalculatePercentagesFromAmounts()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "% শতাংশ" else "% Percent",
                                        fontSize = 11.sp,
                                        fontWeight = if (isPercentageMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPercentageMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { splitEqually() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সমান ভাগ" else "Split Equally",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(paymentSourceAccounts, key = { it.id }) { acc ->
                            val isIncluded = allocMap.containsKey(acc.id)
                            val currentAmtStr = allocMap[acc.id] ?: ""
                            val currentPctStr = pctMap[acc.id] ?: ""
                            val currBudget = getEffectiveBudget()

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isIncluded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Checkbox(
                                            checked = isIncluded,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    val existingCount = allocMap.size + 1
                                                    val share = if (currBudget > 0) (currBudget / existingCount) else 0.0
                                                    val sharePct = 100.0 / existingCount
                                                    allocMap[acc.id] = if (share > 0) String.format("%.2f", share) else "0.00"
                                                    pctMap[acc.id] = String.format("%.1f", sharePct)
                                                } else {
                                                    allocMap.remove(acc.id)
                                                    pctMap.remove(acc.id)
                                                }
                                            }
                                        )
                                        val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = acc.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(if (isImg) 28.dp else 16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = acc.localizedName(languageMode),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isIncluded) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isPercentageMode) {
                                                OutlinedTextField(
                                                    value = currentPctStr,
                                                    onValueChange = { input ->
                                                        pctMap[acc.id] = input
                                                        val pct = input.toDoubleOrNull() ?: 0.0
                                                        val amt = (pct / 100.0) * currBudget
                                                        allocMap[acc.id] = if (amt > 0) String.format("%.2f", amt) else "0.00"
                                                    },
                                                    modifier = Modifier.width(72.dp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(6.dp),
                                                    trailingIcon = { Text("%", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline) },
                                                    textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
                                                )
                                            } else {
                                                OutlinedTextField(
                                                    value = currentAmtStr,
                                                    onValueChange = { input ->
                                                        allocMap[acc.id] = input
                                                        val amt = input.toDoubleOrNull() ?: 0.0
                                                        val pct = if (currBudget > 0) (amt / currBudget) * 100.0 else 0.0
                                                        pctMap[acc.id] = if (pct > 0) String.format("%.1f", pct) else "0.0"
                                                    },
                                                    modifier = Modifier.width(88.dp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(6.dp),
                                                    textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val totalAllocated = allocMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
                    val currBudget = getEffectiveBudget()
                    val totalPct = if (currBudget > 0) (totalAllocated / currBudget) * 100.0 else 0.0
                    val isBalanced = currBudget <= 0 || kotlin.math.abs(totalAllocated - currBudget) < 0.01

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (isBalanced) SolidIncome else SolidExpense).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, (if (isBalanced) SolidIncome else SolidExpense).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট বরাদ্দ" else "Total Allocated",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${LanguageHelper.formatCurrency(totalAllocated, languageMode)} (${String.format("%.0f", totalPct)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBalanced) SolidIncome else SolidExpense
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons (Cancel & Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onSave(mapOf(0L to 0.0))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সোর্স সরান" else "Unassign",
                            fontSize = 12.sp,
                            color = SolidExpense,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val currBudget = getEffectiveBudget()
                                if (assignmentMode == 0) {
                                    // Single Source mode: 100% to selected account or unassigned
                                    val targetAccId = selectedSingleAccountId
                                    if (targetAccId == 0L) {
                                        onSave(mapOf(0L to 0.0))
                                    } else {
                                        val amount = if (currBudget > 0.0) currBudget else 0.001
                                        onSave(mapOf(targetAccId to amount))
                                    }
                                } else {
                                    // Multi-split mode
                                    val resultMap = mutableMapOf<Long, Double>()
                                    allocMap.forEach { (accId, amtStr) ->
                                        val amt = amtStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0.0) resultMap[accId] = amt
                                    }
                                    if (resultMap.isEmpty()) {
                                        onSave(mapOf(0L to 0.0))
                                    } else {
                                        onSave(resultMap)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বরাদ্দ সংরক্ষণ" else "Assign & Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: OTHER ACCOUNT MULTI-SOURCE SPLIT & SINGLE-SOURCE ASSIGNMENT
// -----------------------------------------------------------------------------

@Composable
internal fun OtherAccountSplitDialog(
    otherAccountAllocation: OtherAccountAllocationAnalysis,
    paymentSourceAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Map<Long, Double>) -> Unit
) {
    val totalBudget = otherAccountAllocation.totalBudgeted
    val isExpense = otherAccountAllocation.isExpense

    var assignmentMode by remember {
        mutableStateOf(if (otherAccountAllocation.isMultiAccount || otherAccountAllocation.accountSplits.size > 1) 1 else 0)
    }

    var selectedSingleAccountId by remember {
        mutableStateOf(
            otherAccountAllocation.accountSplits.firstOrNull()?.account?.id
                ?: paymentSourceAccounts.firstOrNull()?.id
                ?: 0L
        )
    }

    var budgetInputText by remember {
        mutableStateOf(if (totalBudget > 0.0) String.format("%.2f", totalBudget) else "")
    }

    var isPercentageMode by remember { mutableStateOf(false) }

    val allocMap = remember {
        val map = mutableStateMapOf<Long, String>()
        otherAccountAllocation.accountSplits.forEach { split ->
            map[split.account.id] = if (split.allocatedAmount > 0) String.format("%.2f", split.allocatedAmount) else "0.00"
        }
        if (map.isEmpty() && paymentSourceAccounts.isNotEmpty()) {
            map[paymentSourceAccounts.first().id] = if (totalBudget > 0) String.format("%.2f", totalBudget) else "0.00"
        }
        map
    }

    val pctMap = remember {
        val map = mutableStateMapOf<Long, String>()
        allocMap.forEach { (accId, amtStr) ->
            val amt = amtStr.toDoubleOrNull() ?: 0.0
            val pct = if (totalBudget > 0) (amt / totalBudget) * 100.0 else 0.0
            map[accId] = if (pct > 0) String.format("%.1f", pct) else "0.0"
        }
        map
    }

    fun getEffectiveBudget(): Double {
        return budgetInputText.toDoubleOrNull() ?: totalBudget
    }

    fun recalculateAmountsFromPercentages() {
        val currBudget = getEffectiveBudget()
        pctMap.forEach { (accId, pctStr) ->
            val pct = pctStr.toDoubleOrNull() ?: 0.0
            val amt = (pct / 100.0) * currBudget
            allocMap[accId] = if (amt > 0) String.format("%.2f", amt) else "0.00"
        }
    }

    fun recalculatePercentagesFromAmounts() {
        val currBudget = getEffectiveBudget()
        allocMap.forEach { (accId, amtStr) ->
            val amt = amtStr.toDoubleOrNull() ?: 0.0
            val pct = if (currBudget > 0) (amt / currBudget) * 100.0 else 0.0
            pctMap[accId] = if (pct > 0) String.format("%.1f", pct) else "0.0"
        }
    }

    fun splitEqually() {
        val count = allocMap.size
        val currBudget = getEffectiveBudget()
        if (count > 0) {
            val shareAmt = if (currBudget > 0) currBudget / count else 0.0
            val sharePct = 100.0 / count
            allocMap.keys.toList().forEach { accId ->
                allocMap[accId] = if (shareAmt > 0) String.format("%.2f", shareAmt) else "0.00"
                pctMap[accId] = String.format("%.1f", sharePct)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(8.dp)
                .testTag("dialog_other_account_split"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${if (languageMode == LanguageMode.BANGLA) "সোর্স নির্ধারণ:" else "Assign Sources:"} ${otherAccountAllocation.account.localizedName(languageMode)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${if (isExpense) "Due / Required" else "Expected Inflow"}: ${LanguageHelper.formatCurrency(totalBudget, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Tabs: Single Source vs Multi-Source Split
                TabRow(
                    selectedTabIndex = assignmentMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[assignmentMode]),
                            color = SolidPrimary
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = assignmentMode == 0,
                        onClick = {
                            assignmentMode = 0
                            if (selectedSingleAccountId == 0L && paymentSourceAccounts.isNotEmpty()) {
                                selectedSingleAccountId = paymentSourceAccounts.first().id
                            }
                        },
                        text = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "একটি সোর্স (১০০%)" else "Single Source (100%)",
                                fontSize = 11.sp,
                                fontWeight = if (assignmentMode == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = assignmentMode == 1,
                        onClick = {
                            assignmentMode = 1
                            if (allocMap.isEmpty() && selectedSingleAccountId > 0) {
                                val currBudget = getEffectiveBudget()
                                allocMap[selectedSingleAccountId] = if (currBudget > 0) String.format("%.2f", currBudget) else "0.00"
                                pctMap[selectedSingleAccountId] = "100.0"
                            }
                        },
                        text = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "একাধিক সোর্সে বিভক্ত" else "Split Across Sources",
                                fontSize = 11.sp,
                                fontWeight = if (assignmentMode == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (assignmentMode == 0) {
                    // MODE 0: Single Source (100%)
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "এই একাউন্টের জন্য মূল পেমেন্ট সোর্স নির্বাচন করুন:" else "Select the primary payment source for this account:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Option for No Source Account (Unassigned)
                        item {
                            val isNoneSelected = selectedSingleAccountId == 0L
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSingleAccountId = 0L },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNoneSelected) SolidExpense.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isNoneSelected) SolidExpense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                tint = if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "কোনো সোর্স নেই (আনঅ্যাসাইন)" else "No Source Account (Unassigned)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isNoneSelected) SolidExpense else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "এই একাউন্টে কোনো সোর্স অ্যাকাউন্ট নির্ধারিত থাকবে না" else "No payment source account will be assigned",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    if (isNoneSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SolidExpense,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(paymentSourceAccounts, key = { it.id }) { acc ->
                            val isSelected = acc.id == selectedSingleAccountId
                            val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedSingleAccountId = acc.id },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = acc.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(if (isImg) 32.dp else 18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = acc.localizedName(languageMode),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (acc.accountNumber.isNotBlank()) {
                                                Text(
                                                    text = acc.accountNumber,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SolidPrimary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // MODE 1: Multi-Split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (!isPercentageMode) SolidPrimary else Color.Transparent)
                                        .clickable {
                                            if (isPercentageMode) {
                                                isPercentageMode = false
                                                recalculatePercentagesFromAmounts()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "৳ পরিমাণ" else "৳ Amount",
                                        fontSize = 11.sp,
                                        fontWeight = if (!isPercentageMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isPercentageMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPercentageMode) SolidPrimary else Color.Transparent)
                                        .clickable {
                                            if (!isPercentageMode) {
                                                isPercentageMode = true
                                                recalculatePercentagesFromAmounts()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "% শতাংশ" else "% Percent",
                                        fontSize = 11.sp,
                                        fontWeight = if (isPercentageMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPercentageMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { splitEqually() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সমান ভাগ" else "Split Equally",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(paymentSourceAccounts, key = { it.id }) { acc ->
                            val isIncluded = allocMap.containsKey(acc.id)
                            val currentAmtStr = allocMap[acc.id] ?: ""
                            val currentPctStr = pctMap[acc.id] ?: ""
                            val currBudget = getEffectiveBudget()

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isIncluded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Checkbox(
                                            checked = isIncluded,
                                            onCheckedChange = { checked ->
                                                if (checked) {
                                                    val existingCount = allocMap.size + 1
                                                    val share = if (currBudget > 0) (currBudget / existingCount) else 0.0
                                                    val sharePct = 100.0 / existingCount
                                                    allocMap[acc.id] = if (share > 0) String.format("%.2f", share) else "0.00"
                                                    pctMap[acc.id] = String.format("%.1f", sharePct)
                                                } else {
                                                    allocMap.remove(acc.id)
                                                    pctMap.remove(acc.id)
                                                }
                                            }
                                        )
                                        val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = acc.iconName,
                                                contentDescription = null,
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(if (isImg) 28.dp else 16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = acc.localizedName(languageMode),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isIncluded) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isPercentageMode) {
                                                OutlinedTextField(
                                                    value = currentPctStr,
                                                    onValueChange = { input ->
                                                        pctMap[acc.id] = input
                                                        val pct = input.toDoubleOrNull() ?: 0.0
                                                        val amt = (pct / 100.0) * currBudget
                                                        allocMap[acc.id] = if (amt > 0) String.format("%.2f", amt) else "0.00"
                                                    },
                                                    modifier = Modifier.width(72.dp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(6.dp),
                                                    trailingIcon = { Text("%", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline) },
                                                    textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
                                                )
                                            } else {
                                                OutlinedTextField(
                                                    value = currentAmtStr,
                                                    onValueChange = { input ->
                                                        allocMap[acc.id] = input
                                                        val amt = input.toDoubleOrNull() ?: 0.0
                                                        val pct = if (currBudget > 0) (amt / currBudget) * 100.0 else 0.0
                                                        pctMap[acc.id] = if (pct > 0) String.format("%.1f", pct) else "0.0"
                                                    },
                                                    modifier = Modifier.width(88.dp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(6.dp),
                                                    textStyle = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val totalAllocated = allocMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
                    val currBudget = getEffectiveBudget()
                    val totalPct = if (currBudget > 0) (totalAllocated / currBudget) * 100.0 else 0.0
                    val isBalanced = currBudget <= 0 || kotlin.math.abs(totalAllocated - currBudget) < 0.01

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (isBalanced) SolidIncome else SolidExpense).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, (if (isBalanced) SolidIncome else SolidExpense).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট বরাদ্দ" else "Total Allocated",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${LanguageHelper.formatCurrency(totalAllocated, languageMode)} (${String.format("%.0f", totalPct)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBalanced) SolidIncome else SolidExpense
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons (Cancel & Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onSave(mapOf(0L to 0.0))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সোর্স সরান" else "Unassign",
                            fontSize = 12.sp,
                            color = SolidExpense,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val currBudget = getEffectiveBudget()
                                if (assignmentMode == 0) {
                                    // Single Source mode: 100% to selected account or unassigned
                                    val targetAccId = selectedSingleAccountId
                                    if (targetAccId == 0L) {
                                        onSave(mapOf(0L to 0.0))
                                    } else {
                                        val amount = if (currBudget > 0.0) currBudget else 0.001
                                        onSave(mapOf(targetAccId to amount))
                                    }
                                } else {
                                    // Multi-split mode
                                    val resultMap = mutableMapOf<Long, Double>()
                                    allocMap.forEach { (accId, amtStr) ->
                                        val amt = amtStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0.0) resultMap[accId] = amt
                                    }
                                    if (resultMap.isEmpty()) {
                                        onSave(mapOf(0L to 0.0))
                                    } else {
                                        onSave(resultMap)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বরাদ্দ সংরক্ষণ" else "Assign & Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
