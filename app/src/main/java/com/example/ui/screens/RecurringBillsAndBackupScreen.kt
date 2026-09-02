package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.data.model.RecurringBill
import com.example.data.model.RecurringBillWithDetails
import com.example.data.model.TransactionType
import com.example.ui.dialogs.AddEditRecurringBillDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BackupUiState
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.BackupManager
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecurringBillsAndBackupScreen(
    viewModel: BudgetViewModel,
    bills: List<RecurringBillWithDetails>,
    languageMode: LanguageMode,
    backupUiState: BackupUiState,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<RecurringBill?>(null) }
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    var localBackups by remember { mutableStateOf<List<File>>(emptyList()) }

    fun refreshLocalBackups() {
        localBackups = BackupManager.listLocalBackups(context)
    }

    LaunchedEffect(Unit) {
        refreshLocalBackups()
    }

    // SAF Launchers for Storage Export & Restore
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackupToUri(it) }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreBackupFromUri(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolidPrimary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SolidPrimary
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventRepeat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recurring Bills (${bills.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        refreshLocalBackups()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup & Sync", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Recurring Bills List
                if (bills.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No recurring bills yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Text("Add electricity, internet, rent, or recurring subscriptions", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    editingBill = null
                                    showAddBillDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Bill")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bills, key = { it.bill.id }) { item ->
                            val bill = item.bill
                            val isExpense = bill.type == TransactionType.EXPENSE
                            val color = if (isExpense) SolidExpense else SolidIncome

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bill.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            val nextDate = DateUtils.formatDate(bill.nextDueDateEpochMs, languageMode)
                                            Text("Due: $nextDate • ${bill.recurrencePeriod.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        }

                                        Text(
                                            text = "${if (isExpense) "-" else "+"}${LanguageHelper.formatCurrency(bill.amount, languageMode)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = color
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(6.dp))

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
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingBill = bill
                                                    showAddBillDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                            }

                                            Button(
                                                onClick = { viewModel.payRecurringBill(bill) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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
            } else {
                // Backup & Sync View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Backup status indicator if active
                    when (val state = backupUiState) {
                        is BackupUiState.Loading -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SolidPrimary.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SolidPrimary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Processing backup operation...", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        is BackupUiState.Success -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SolidIncome.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(state.message, fontSize = 12.sp, color = SolidIncome, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        is BackupUiState.Error -> {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SolidExpense.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(state.message, fontSize = 12.sp, color = SolidExpense, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    // 1. Google Drive / Cloud Sync Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = SolidPrimary.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Sync, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Google Drive & Cloud Auto-Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Automatic decentralized double-entry snapshots", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Budgeter operates with offline-first double-entry integrity. You can export complete snapshots to Google Drive or any cloud folder using standard system storage.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                            exportFileLauncher.launch("Budgeter_DoubleEntry_Backup_$timeStamp.json")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export to Drive", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { importFileLauncher.launch(arrayOf("application/json", "*/*")) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Import File", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Local Storage Quick Snapshot Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = SolidIncome.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.SdStorage, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Local Device Storage Backup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Save snapshot on device storage / share", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.createLocalBackup { file ->
                                            refreshLocalBackups()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Local Backup Now", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // 3. Saved Backups on Device
                    item {
                        Text("SAVED LOCAL BACKUPS (${localBackups.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    if (localBackups.isEmpty()) {
                        item {
                            Text("No local backups found on device yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        items(localBackups) { file ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                        val date = SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                                        Text("$date • ${file.length() / 1024} KB", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }

                                    Row {
                                        // Share backup file
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/json"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Share Budget Backup"))
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                        }

                                        // Restore from this file
                                        Button(
                                            onClick = {
                                                val uri = Uri.fromFile(file)
                                                viewModel.restoreBackupFromUri(uri)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Restore", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for Recurring Bills
        if (selectedTab == 0) {
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
