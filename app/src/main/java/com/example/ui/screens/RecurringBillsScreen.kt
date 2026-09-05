package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.data.model.RecurringBill
import com.example.data.model.RecurringBillWithDetails
import com.example.data.model.TransactionType
import com.example.ui.components.AppTabHeader
import com.example.ui.dialogs.AddEditRecurringBillDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.LanguageHelper

@Composable
fun RecurringBillsScreen(
    viewModel: BudgetViewModel,
    bills: List<RecurringBillWithDetails>,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {}
) {
    var showAddBillDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<RecurringBill?>(null) }
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        AppTabHeader(
            title = LanguageHelper.getString("recurring_bills", languageMode),
            onOpenDrawer = onOpenDrawer,
            actions = {
                IconButton(
                    onClick = {
                        editingBill = null
                        showAddBillDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Bill",
                        tint = SolidPrimary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventRepeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো পুনরাবৃত্তিমূলক বিল নেই" else "No Recurring Bills Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বিদ্যুৎ, ইন্টারনেট, বাড়ি ভাড়া বা সাবস্ক্রিপশন যুক্ত করুন" else "Track electricity, rent, wifi, or recurring subscriptions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                editingBill = null
                                showAddBillDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "প্রথম বিল যোগ করুন" else "Add First Bill")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                items(bills, key = { it.bill.id }) { item ->
                    val bill = item.bill
                    val isExpense = bill.type == TransactionType.EXPENSE
                    val color = if (isExpense) SolidExpense else SolidIncome

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(bill.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    val nextDate = DateUtils.formatDate(bill.nextDueDateEpochMs, languageMode)
                                    Text(
                                        text = "Due: $nextDate • ${bill.recurrencePeriod.name}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Text(
                                    text = "${if (isExpense) "-" else "+"}${LanguageHelper.formatCurrency(bill.amount, languageMode)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = color
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (bill.isAutoRecord) SolidIncome.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (bill.isAutoRecord) "⚡ Auto-Record" else "Manual Pay",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bill.isAutoRecord) SolidIncome else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingBill = bill
                                            showAddBillDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = { viewModel.payRecurringBill(bill) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pay Now", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
            FloatingActionButton(
                onClick = {
                    editingBill = null
                    showAddBillDialog = true
                },
                containerColor = SolidPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    }

    if (showAddBillDialog) {
        AddEditRecurringBillDialog(
            accounts = allAccounts,
            categories = allCategories,
            languageMode = languageMode,
            existingBill = editingBill,
            onDismiss = { showAddBillDialog = false },
            onSave = { bill -> viewModel.saveRecurringBill(bill) },
            onDelete = { bill -> viewModel.deleteRecurringBill(bill) }
        )
    }
}
