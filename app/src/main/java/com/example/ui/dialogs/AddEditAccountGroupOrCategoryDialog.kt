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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.components.DatePickerModal
import com.example.ui.components.IconPickerModal
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

enum class AccountEntryMode {
    NEW_GROUP,
    NEW_CATEGORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountGroupOrCategoryDialog(
    allGroups: List<Account>, // Top-level accounts (parentId == null)
    languageMode: LanguageMode,
    existingAccount: Account? = null,
    defaultGroupId: Long? = null,
    defaultType: AccountType = AccountType.ASSET,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: ((Account) -> Unit)? = null
) {
    // Determine entry mode: If editing a top-level group, NEW_GROUP. If editing child, NEW_CATEGORY.
    var entryMode by remember {
        mutableStateOf(
            if (existingAccount != null) {
                if (existingAccount.parentId == null) AccountEntryMode.NEW_GROUP else AccountEntryMode.NEW_CATEGORY
            } else {
                if (defaultGroupId != null) AccountEntryMode.NEW_CATEGORY else AccountEntryMode.NEW_CATEGORY
            }
        )
    }

    var nameEn by remember { mutableStateOf(existingAccount?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(existingAccount?.nameBn ?: "") }
    var accountType by remember { mutableStateOf(existingAccount?.type ?: defaultType) }
    var selectedGroupId by remember {
        mutableStateOf(
            existingAccount?.parentId ?: defaultGroupId ?: allGroups.firstOrNull { it.type == accountType }?.id
        )
    }
    var selectedIcon by remember {
        mutableStateOf(
            existingAccount?.iconName ?: if (accountType == AccountType.ASSET) "Wallet" else "CreditCard"
        )
    }
    var initialAmountText by remember {
        mutableStateOf(existingAccount?.initialBalance?.toString() ?: "0.0")
    }
    var creationDateEpochMs by remember {
        mutableLongStateOf(existingAccount?.createdAt ?: System.currentTimeMillis())
    }
    var isActive by remember {
        mutableStateOf(existingAccount?.isActive ?: true)
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_edit_account_window")
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
                        text = if (existingAccount == null) {
                            if (entryMode == AccountEntryMode.NEW_GROUP) "New Group" else "New Category / Account"
                        } else {
                            if (existingAccount.parentId == null) "Edit Group" else "Edit Category / Account"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selection: "New Group" or "New Categories"
                if (existingAccount == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = entryMode == AccountEntryMode.NEW_GROUP,
                            onClick = {
                                entryMode = AccountEntryMode.NEW_GROUP
                            },
                            label = { Text("New Group", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = entryMode == AccountEntryMode.NEW_CATEGORY,
                            onClick = {
                                entryMode = AccountEntryMode.NEW_CATEGORY
                                if (selectedGroupId == null && allGroups.isNotEmpty()) {
                                    selectedGroupId = allGroups.firstOrNull { it.type == accountType }?.id ?: allGroups.first().id
                                }
                            },
                            label = { Text("New Category", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Type (Assets / Liabilities) or Group (Show All groups)
                if (entryMode == AccountEntryMode.NEW_GROUP) {
                    Text(
                        text = "Group Type (Assets or Liabilities)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = accountType == AccountType.ASSET,
                            onClick = { accountType = AccountType.ASSET },
                            label = { Text("Assets", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidIncome,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = accountType == AccountType.LIABILITY,
                            onClick = { accountType = AccountType.LIABILITY },
                            label = { Text("Liabilities", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidExpense,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Category belongs to a Group (Show All groups)
                    Text(
                        text = "Parent Group",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val availableGroups = allGroups
                    val selectedGroup = availableGroups.firstOrNull { it.id == selectedGroupId }

                    ExposedDropdownMenuBox(
                        expanded = groupDropdownExpanded,
                        onExpandedChange = { groupDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGroup?.let { "${it.localizedName(languageMode)} (${if (it.type == AccountType.ASSET) "Assets" else "Liabilities"})" }
                                ?: "Select Group",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false }
                        ) {
                            availableGroups.forEach { grp ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(grp.localizedName(languageMode), fontWeight = FontWeight.Medium)
                                            Text(
                                                text = if (grp.type == AccountType.ASSET) "Assets" else "Liabilities",
                                                fontSize = 11.sp,
                                                color = if (grp.type == AccountType.ASSET) SolidIncome else SolidExpense
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedGroupId = grp.id
                                        accountType = grp.type
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name English & Bangla
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Name (Bangla - Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Icons Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SolidPrimary.copy(alpha = 0.12f))
                                .border(1.dp, SolidPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(selectedIcon),
                                contentDescription = "Icon",
                                tint = SolidPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Icon", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(selectedIcon, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Button(
                        onClick = { showIconPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Change", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Initial Amount (Only for sub-accounts, account groups do not have initial balance)
                if (entryMode == AccountEntryMode.NEW_CATEGORY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = initialAmountText,
                        onValueChange = { initialAmountText = it },
                        label = { Text("Initial Amount (৳)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Creation Date Selector
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Creation Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = DateUtils.formatDate(creationDateEpochMs, languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text("Change", fontSize = 12.sp, color = SolidPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active / Inactive Status Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isActive) "Active Account" else "Inactive / Archived",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isActive) SolidIncome else SolidExpense
                        )
                        Text(
                            text = if (isActive) "Visible in transactions and net worth" else "Hidden from active picker",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SolidIncome,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (existingAccount != null && onDelete != null && !existingAccount.isSystem) {
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
                            val parsedBal = if (entryMode == AccountEntryMode.NEW_GROUP) 0.0 else (initialAmountText.toDoubleOrNull() ?: 0.0)
                            val parent = if (entryMode == AccountEntryMode.NEW_CATEGORY) selectedGroupId else null
                            val groupType = if (entryMode == AccountEntryMode.NEW_CATEGORY) {
                                allGroups.firstOrNull { it.id == selectedGroupId }?.type ?: accountType
                            } else {
                                accountType
                            }

                            val account = Account(
                                id = existingAccount?.id ?: 0,
                                nameEn = nameEn.ifBlank { nameBn },
                                nameBn = nameBn.ifBlank { nameEn },
                                type = groupType,
                                parentId = parent,
                                initialBalance = parsedBal,
                                iconName = selectedIcon,
                                colorHex = if (groupType == AccountType.ASSET) "#10B981" else "#EF4444",
                                isActive = isActive,
                                createdAt = creationDateEpochMs
                            )
                            onSave(account)
                            onDismiss()
                        },
                        enabled = nameEn.isNotBlank() || nameBn.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_account_group_btn")
                    ) {
                        Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerModal(
            selectedIconName = selectedIcon,
            onIconSelected = { selectedIcon = it },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = creationDateEpochMs,
            languageMode = languageMode,
            onDateSelected = { selected ->
                creationDateEpochMs = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showDeleteConfirmDialog && existingAccount != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(LanguageHelper.getString("delete", languageMode)) },
            text = { Text("Are you sure you want to delete this account? All associated records may be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(existingAccount)
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
