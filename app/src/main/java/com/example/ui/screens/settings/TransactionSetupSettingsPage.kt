package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DisplayFormatConfig
import com.example.util.DisplayFormatPreferences
import com.example.util.IconHelper
import com.example.util.ItemDisplayFormat
import com.example.util.LabelsDisplayMode
import com.example.util.NotesDisplayMode
import com.example.util.TransactionConfig
import com.example.util.TransactionPreferences
import com.example.util.UnnamedPayeeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSetupSettingsPage(
    viewModel: BudgetViewModel,
    accounts: List<Account>,
    categories: List<Category>,
    languageMode: LanguageMode,
    onNavigateToAutofill: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA

    val txPrefs = remember { TransactionPreferences.getInstance(context) }
    val txConfig by txPrefs.config.collectAsStateWithLifecycle()

    val displayFormatPrefs = remember { DisplayFormatPreferences.getInstance(context) }
    val displayFormatConfig by displayFormatPrefs.config.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    val incomeCategories = remember(categories) {
        categories.filter { it.type == CategoryType.INCOME && it.parentId == null }
    }
    val expenseCategories = remember(categories) {
        categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transaction_setup_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "লেনদেন সেটআপ" else "Transaction Setup",
            tabIcon = Icons.Default.Tune,
            onBack = onBack,
            autoHideOnScroll = false
        )

        // 3-Tab Segmented Controls
        val tabs = listOf(
            if (isBangla) "ইনপুট ও ডেট" else "Input & Date",
            if (isBangla) "ডিফল্ট মান" else "Defaults & Names",
            if (isBangla) "+১ এবং প্রদর্শন" else "+1 & Display"
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SolidPrimary
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (selectedTab) {
                0 -> {
                    // Date & Time Controls
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Quick Date Picker Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isBangla) "দ্রুত তারিখ নির্বাচন (কুইক চিপস)" else "Quick Date Picker Chips",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isBangla) "আজ, গতকাল, গত পরশু দ্রুত বাছাইয়ের অপশন" else "Show Today, Yesterday, Day Before quick chips",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Switch(
                                    checked = txConfig.enableQuickDatePicker,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(enableQuickDatePicker = it) } },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            // Show Time Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isBangla) "সময় নির্বাচন দেখান" else "Show Transaction Time",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isBangla) "লেনদেনের নির্দিষ্ট সময় (ঘণ্টা:মিনিট) নির্ধারণ" else "Enable selecting exact time with transactions",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Switch(
                                    checked = txConfig.showTimePicker,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showTimePicker = it) } },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            // Show Keyboard Immediately Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isBangla) "কিবোর্ড অবিলম্বে দেখান" else "Show Keyboard Immediately",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isBangla) "লেনদেন যোগ করার সময় নাম ফিল্ডে সরাসরি কিবোর্ড খুলবে" else "Open keyboard immediately in Name field when adding transaction",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Switch(
                                    checked = txConfig.showKeyboardImmediately,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(showKeyboardImmediately = it) } },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                                )
                            }
                        }
                    }

                    // Smart Autofill Link Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToAutofill() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "স্মার্ট অটোফিল ও পরামর্শ সেটিংস" else "Smart Autofill & Suggestions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isBangla) "বিবরণ ও অ্যাকাউন্টের স্বয়ংক্রিয় পূরণ কনফিগারেশন" else "Configure auto-categorization & amount predictions",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                1 -> {
                    // Defaults & Names
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 1. Default Account
                            Text(
                                text = if (isBangla) "ডিফল্ট অ্যাকাউন্ট" else "Default Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SolidPrimary
                            )

                            var showAccountDropdown by remember { mutableStateOf(false) }
                            val parentAccounts = remember(accounts) { accounts.filter { it.parentId == null && it.isActive } }
                            val currentAccount = accounts.firstOrNull { it.id == txConfig.defaultAccountId }
                            val parentOfCurrentAccount = if (currentAccount?.parentId != null) accounts.firstOrNull { it.id == currentAccount.parentId } else null
                            val currentAccountDisplay = if (currentAccount != null) {
                                if (parentOfCurrentAccount != null) {
                                    "${parentOfCurrentAccount.localizedName(languageMode)} > ${currentAccount.localizedName(languageMode)}"
                                } else {
                                    currentAccount.localizedName(languageMode)
                                }
                            } else {
                                if (isBangla) "কোনো ডিফল্ট নেই (শেষ ব্যবহৃত)" else "None (Use Last Selected)"
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showAccountDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentAccountDisplay,
                                            fontSize = 13.sp,
                                            fontWeight = if (txConfig.defaultAccountId != null) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showAccountDropdown,
                                    onDismissRequest = { showAccountDropdown = false },
                                    modifier = Modifier.heightIn(max = 400.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isBangla) "কোনো ডিফল্ট নেই (শেষ ব্যবহৃত)" else "None (Use Last Selected)",
                                                fontWeight = if (txConfig.defaultAccountId == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (txConfig.defaultAccountId == null) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        trailingIcon = if (txConfig.defaultAccountId == null) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        onClick = {
                                            txPrefs.updateConfig { it.copy(defaultAccountId = null) }
                                            showAccountDropdown = false
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    parentAccounts.forEach { parent ->
                                        val childAccounts = accounts.filter { it.parentId == parent.id && it.isActive }

                                        // Group Header (Not selectable)
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(parent.iconName),
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = parent.localizedName(languageMode),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SolidPrimary
                                                )
                                            }
                                        }

                                        val itemsToDisplay = if (childAccounts.isNotEmpty()) childAccounts else listOf(parent)
                                        itemsToDisplay.forEach { acc ->
                                            val isSelected = txConfig.defaultAccountId == acc.id
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = acc.localizedName(languageMode),
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 16.dp)
                                                    )
                                                },
                                                trailingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp)) }
                                                } else null,
                                                onClick = {
                                                    txPrefs.updateConfig { it.copy(defaultAccountId = acc.id) }
                                                    showAccountDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 2. Default Income Category
                            Text(
                                text = if (isBangla) "ডিফল্ট আয়ের ক্যাটাগরি" else "Default Income Category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SolidIncome
                            )

                            var showIncomeCatDropdown by remember { mutableStateOf(false) }
                            val parentIncomeCategories = remember(categories) {
                                categories.filter { it.type == CategoryType.INCOME && it.parentId == null && it.isActive }
                            }
                            val currentIncomeCat = categories.firstOrNull { it.id == txConfig.defaultIncomeCategoryId }
                            val parentOfCurrentIncomeCat = if (currentIncomeCat?.parentId != null) categories.firstOrNull { it.id == currentIncomeCat.parentId } else null
                            val currentIncomeCatDisplay = if (currentIncomeCat != null) {
                                if (parentOfCurrentIncomeCat != null) {
                                    "${parentOfCurrentIncomeCat.localizedName(languageMode)} > ${currentIncomeCat.localizedName(languageMode)}"
                                } else {
                                    currentIncomeCat.localizedName(languageMode)
                                }
                            } else {
                                if (isBangla) "কোনো ডিফল্ট নেই" else "None (Select Manually)"
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showIncomeCatDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentIncomeCatDisplay,
                                            fontSize = 13.sp,
                                            fontWeight = if (txConfig.defaultIncomeCategoryId != null) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Icon(Icons.Default.Category, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showIncomeCatDropdown,
                                    onDismissRequest = { showIncomeCatDropdown = false },
                                    modifier = Modifier.heightIn(max = 400.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isBangla) "কোনো ডিফল্ট নেই" else "None (Select Manually)",
                                                fontWeight = if (txConfig.defaultIncomeCategoryId == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (txConfig.defaultIncomeCategoryId == null) SolidIncome else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        trailingIcon = if (txConfig.defaultIncomeCategoryId == null) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        onClick = {
                                            txPrefs.updateConfig { it.copy(defaultIncomeCategoryId = null) }
                                            showIncomeCatDropdown = false
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    parentIncomeCategories.forEach { parent ->
                                        val childCategories = categories.filter { it.type == CategoryType.INCOME && it.parentId == parent.id && it.isActive }

                                        // Group Header (Not selectable)
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(parent.iconName),
                                                    contentDescription = null,
                                                    tint = SolidIncome,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = parent.localizedName(languageMode),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SolidIncome
                                                )
                                            }
                                        }

                                        val itemsToDisplay = if (childCategories.isNotEmpty()) childCategories else listOf(parent)
                                        itemsToDisplay.forEach { cat ->
                                            val isSelected = txConfig.defaultIncomeCategoryId == cat.id
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = cat.localizedName(languageMode),
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) SolidIncome else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 16.dp)
                                                    )
                                                },
                                                trailingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp)) }
                                                } else null,
                                                onClick = {
                                                    txPrefs.updateConfig { it.copy(defaultIncomeCategoryId = cat.id) }
                                                    showIncomeCatDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Default Expense Category
                            Text(
                                text = if (isBangla) "ডিফল্ট খরচের ক্যাটাগরি" else "Default Expense Category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SolidExpense
                            )

                            var showExpenseCatDropdown by remember { mutableStateOf(false) }
                            val parentExpenseCategories = remember(categories) {
                                categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null && it.isActive }
                            }
                            val currentExpenseCat = categories.firstOrNull { it.id == txConfig.defaultExpenseCategoryId }
                            val parentOfCurrentExpenseCat = if (currentExpenseCat?.parentId != null) categories.firstOrNull { it.id == currentExpenseCat.parentId } else null
                            val currentExpenseCatDisplay = if (currentExpenseCat != null) {
                                if (parentOfCurrentExpenseCat != null) {
                                    "${parentOfCurrentExpenseCat.localizedName(languageMode)} > ${currentExpenseCat.localizedName(languageMode)}"
                                } else {
                                    currentExpenseCat.localizedName(languageMode)
                                }
                            } else {
                                if (isBangla) "কোনো ডিফল্ট নেই" else "None (Select Manually)"
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showExpenseCatDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentExpenseCatDisplay,
                                            fontSize = 13.sp,
                                            fontWeight = if (txConfig.defaultExpenseCategoryId != null) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Icon(Icons.Default.Category, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showExpenseCatDropdown,
                                    onDismissRequest = { showExpenseCatDropdown = false },
                                    modifier = Modifier.heightIn(max = 400.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isBangla) "কোনো ডিফল্ট নেই" else "None (Select Manually)",
                                                fontWeight = if (txConfig.defaultExpenseCategoryId == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (txConfig.defaultExpenseCategoryId == null) SolidExpense else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        trailingIcon = if (txConfig.defaultExpenseCategoryId == null) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        onClick = {
                                            txPrefs.updateConfig { it.copy(defaultExpenseCategoryId = null) }
                                            showExpenseCatDropdown = false
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    parentExpenseCategories.forEach { parent ->
                                        val childCategories = categories.filter { it.type == CategoryType.EXPENSE && it.parentId == parent.id && it.isActive }

                                        // Group Header (Not selectable)
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(parent.iconName),
                                                    contentDescription = null,
                                                    tint = SolidExpense,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = parent.localizedName(languageMode),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SolidExpense
                                                )
                                            }
                                        }

                                        val itemsToDisplay = if (childCategories.isNotEmpty()) childCategories else listOf(parent)
                                        itemsToDisplay.forEach { cat ->
                                            val isSelected = txConfig.defaultExpenseCategoryId == cat.id
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = cat.localizedName(languageMode),
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) SolidExpense else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 16.dp)
                                                    )
                                                },
                                                trailingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp)) }
                                                } else null,
                                                onClick = {
                                                    txPrefs.updateConfig { it.copy(defaultExpenseCategoryId = cat.id) }
                                                    showExpenseCatDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // +1 & Display Settings
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isBangla) "লেনদেন তালিকা ভিউ ফরম্যাট" else "Transaction List Item Density",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SolidPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isSingle = displayFormatConfig.itemDisplayFormat == ItemDisplayFormat.SINGLE_LINE
                                FilterChip(
                                    selected = isSingle,
                                    onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.SINGLE_LINE) },
                                    label = { Text(if (isBangla) "এক লাইন (কম্প্যাক্ট)" else "Single Line (Compact)", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = !isSingle,
                                    onClick = { displayFormatPrefs.setItemDisplayFormat(ItemDisplayFormat.TWO_LINES) },
                                    label = { Text(if (isBangla) "দুই লাইন (পূর্ণাঙ্গ)" else "Double Line (Default)", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = if (isBangla) "+১ (Save and Add Another) বোতামের আচরণ" else "(+1) Save & Add Another Behavior",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "ক্যাটাগরি মনে রাখুন" else "Keep selected Category",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepCategory,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepCategory = it) } }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "অ্যাকাউন্ট মনে রাখুন" else "Keep selected Account",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepAccount,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepAccount = it) } }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBangla) "তারিখ মনে রাখুন" else "Keep selected Date",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = txConfig.plusOneKeepDate,
                                    onCheckedChange = { txPrefs.updateConfig { cfg -> cfg.copy(plusOneKeepDate = it) } }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
