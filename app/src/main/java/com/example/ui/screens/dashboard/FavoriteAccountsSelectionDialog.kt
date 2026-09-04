package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Locale

@Composable
fun FavoriteAccountsSelectionDialog(
    allAccounts: List<AccountWithBalance>,
    initialSelectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(initialSelectedIds.toMutableSet()) }

    val filteredAccounts = remember(allAccounts, searchQuery) {
        if (searchQuery.isBlank()) {
            allAccounts
        } else {
            allAccounts.filter {
                it.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.account.nameBn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("select_favorite_accounts", languageMode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = "${selectedIds.size} ${LanguageHelper.getString("selected", languageMode)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(LanguageHelper.getString("search_accounts", languageMode), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select All / Clear All Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { selectedIds = allAccounts.map { it.account.id }.toMutableSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("select_all", languageMode), fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { selectedIds = mutableSetOf() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(LanguageHelper.getString("clear_all", languageMode), fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Accounts List
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredAccounts, key = { it.account.id }) { item ->
                        val isSelected = item.account.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedIds = (selectedIds - item.account.id).toMutableSet()
                                    } else {
                                        selectedIds = (selectedIds + item.account.id).toMutableSet()
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedIds = (selectedIds + item.account.id).toMutableSet()
                                        } else {
                                            selectedIds = (selectedIds - item.account.id).toMutableSet()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(item.account.iconName),
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.account.localizedName(languageMode),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.account.type.name,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Text(
                                text = "BDT ${String.format(Locale.US, "%,.2f", item.currentBalance)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.currentBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSave(selectedIds) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageHelper.getString("save", languageMode))
                    }
                }
            }
        }
    }
}
