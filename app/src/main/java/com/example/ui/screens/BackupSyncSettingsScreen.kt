package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.CsvExportDialog
import com.example.ui.components.CsvImportPreviewDialog
import com.example.ui.dialogs.FolderPickerDialog
import com.example.ui.dialogs.SecurityAuthDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BackupUiState
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.BackupManager
import com.example.util.CsvExportConfig
import com.example.util.DriveBackupLocation
import com.example.util.GoogleDriveBackupFile
import com.example.util.GoogleDriveService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DataManagementTab(val titleEn: String, val titleBn: String, val icon: ImageVector) {
    ONLINE("Online", "অনলাইন", Icons.Default.CloudUpload),
    LOCAL("Local", "লোকাল", Icons.Default.Storage),
    EXPORT_IMPORT("Export or Import", "এক্সপোর্ট / ইমপোর্ট", Icons.Default.SwapVert)
}

@Composable
fun BackupSyncSettingsScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    backupUiState: BackupUiState,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val config by viewModel.backupSettingsConfig.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    val signedInAccount by viewModel.signedInGoogleAccount.collectAsStateWithLifecycle()
    val driveBackups by viewModel.driveBackups.collectAsStateWithLifecycle()

    val csvImportPreview by viewModel.csvImportPreview.collectAsStateWithLifecycle()
    val isImportingCsv by viewModel.isImportingCsv.collectAsStateWithLifecycle()
    val transactionsWithDetails by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var localBackups by remember { mutableStateOf<List<File>>(emptyList()) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var showFolderPickerDialog by remember { mutableStateOf(false) }
    var showDriveRestoreDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var pendingCsvExportConfig by remember { mutableStateOf<CsvExportConfig?>(null) }
    var restoreConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var deleteConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var restoreConfirmLocalFile by remember { mutableStateOf<File?>(null) }
    var deleteConfirmLocalFile by remember { mutableStateOf<File?>(null) }

    fun refreshLocalBackups() {
        localBackups = BackupManager.listLocalBackups(context, config.localBackupDirectory)
    }

    LaunchedEffect(config.localBackupDirectory) {
        refreshLocalBackups()
        val existingAccount = GoogleDriveService.getSignedInAccount(context)
        viewModel.updateSignedInAccount(existingAccount)
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.updateSignedInAccount(account)
            viewModel.setAccountLinked(account != null)
        } catch (e: Exception) {
            e.printStackTrace()
            val current = GoogleDriveService.getSignedInAccount(context)
            viewModel.updateSignedInAccount(current)
            viewModel.setAccountLinked(current != null)
        }
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

    // CSV and QIF File Pickers
    val csvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromCsv(it) }
    }

    val qifFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromQif(it) }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            pendingCsvExportConfig?.let { cfg ->
                viewModel.exportCsvToUri(destUri, cfg)
            }
        }
    }

    val isLoading = backupUiState is BackupUiState.Loading

    // Spinning animation for QuickSync
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_data_management")
    ) {
        // TOP APP BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডাটা ব্যবস্থাপনা" else "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ক্লাউড সিঙ্ক, লোকাল ব্যাকআপ ও ফাইল ট্রান্সফার" else "Cloud Sync, Local Backups & File Transfer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // THREE OPTION TABS: Online | Local | Export or Import
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            DataManagementTab.values().forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("tab_data_${tab.name.lowercase()}"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) tab.titleBn else tab.titleEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                )
            }
        }

        // MAIN CONTENT AREA
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status Feedback Card
            when (val state = backupUiState) {
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
                                Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(state.message, fontSize = 12.sp, color = SolidExpense, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {}
            }

            // -------------------------------------------------------------
            // OPTION 1: ONLINE (Cloud Sync, Google Drive)
            // -------------------------------------------------------------
            if (selectedTabIndex == 0) {
                // Online Sync Engine Card
                item {
                    val isLinked = signedInAccount != null
                    val userName = signedInAccount?.displayName ?: if (isLinked) "Google Account" else if (languageMode == LanguageMode.BANGLA) "কোনো একাউন্ট যুক্ত নেই" else "No account linked"
                    val userEmail = signedInAccount?.email ?: if (languageMode == LanguageMode.BANGLA) "ক্লাউড সিঙ্ক সক্রিয় করতে সাইন-ইন করুন" else "Sign in to enable automated cloud backup & sync"

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isLinked) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(userEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    }
                                }

                                Switch(
                                    checked = isLinked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            val client = GoogleDriveService.getGoogleSignInClient(context)
                                            googleSignInLauncher.launch(client.signInIntent)
                                        } else {
                                            val client = GoogleDriveService.getGoogleSignInClient(context)
                                            client.signOut().addOnCompleteListener {
                                                viewModel.updateSignedInAccount(null)
                                                viewModel.setAccountLinked(false)
                                            }
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val lastSyncText = if (config.lastSyncTimestamp > 0L) {
                                        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(config.lastSyncTimestamp))
                                    } else if (languageMode == LanguageMode.BANGLA) "এখনো সিঙ্ক হয়নি" else "Not synced yet"
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "সর্বশেষ সিঙ্ক: $lastSyncText" else "Last sync: $lastSyncText",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.triggerQuickSync() },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp).then(if (isLoading) Modifier.rotate(spinAngle) else Modifier))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (languageMode == LanguageMode.BANGLA) "কুইক সিঙ্ক" else "Quick Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cloud Provider & Automated Preferences
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsListTile(
                                icon = Icons.Default.CloudQueue,
                                iconTint = SolidPrimary,
                                title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড ব্যাকআপ প্রোভাইডার" else "Cloud Backup Provider",
                                subtitle = config.cloudProvider,
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(config.cloudProvider, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                                    }
                                },
                                onClick = { showProviderDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            SettingsListTile(
                                icon = Icons.Default.Sync,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় সিঙ্ক (অটো-সিঙ্ক)" else "Automatic sync on change",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "প্রতিটি লেনদেন পরিবর্তনের সাথে সাথে ক্লাউডে সিঙ্ক করুন" else "Instantly upload changes to Google Drive in background",
                                trailingContent = {
                                    Switch(
                                        checked = config.autoSyncData,
                                        onCheckedChange = { viewModel.setAutoSyncData(it) }
                                    )
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            SettingsListTile(
                                icon = Icons.Default.AttachFile,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "সংযুক্তি আপলোড করুন" else "Upload receipt attachments",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "ভাউচার ও রসিদের ছবি ক্লাউডে সংরক্ষণ করুন" else "Include image attachments in cloud backup snapshots",
                                trailingContent = {
                                    Switch(
                                        checked = config.uploadAttachments,
                                        onCheckedChange = { viewModel.setUploadAttachments(it) }
                                    )
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            SettingsListTile(
                                icon = Icons.Default.Wifi,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "কেবল ওয়াই-ফাই (Wi-Fi Only)" else "Wi-Fi only sync",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "মোবাইল ডাটা খরচ রোধ করতে কেবল ওয়াই-ফাই ব্যবহার করুন" else "Restrict cloud uploads to Wi-Fi connection",
                                trailingContent = {
                                    Switch(
                                        checked = config.wifiOnly,
                                        onCheckedChange = { viewModel.setWifiOnly(it) }
                                    )
                                }
                            )
                        }
                    }
                }

                // Cloud Actions & Snapshots List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (signedInAccount != null) {
                                    viewModel.backupToGoogleDrive(signedInAccount!!)
                                } else {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    googleSignInLauncher.launch(client.signInIntent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভে ব্যাকআপ নিন" else "Backup to Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (signedInAccount != null) {
                                    viewModel.fetchDriveBackups(signedInAccount!!)
                                    showDriveRestoreDialog = true
                                } else {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    googleSignInLauncher.launch(client.signInIntent)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভ স্ন্যাপশটস" else "Drive Snapshots", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // OPTION 2: LOCAL (Storage Backup, Custom Directory)
            // -------------------------------------------------------------
            if (selectedTabIndex == 1) {
                // Storage Folder Configuration Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SolidIncome.copy(alpha = 0.15f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "লোকাল ব্যাকআপ ফোল্ডার" else "Local Backup Storage Directory",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = config.localBackupDirectory,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showFolderPickerDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_select_backup_folder")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (languageMode == LanguageMode.BANGLA) "নির্বাচন" else "Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Automatic Phone Backup Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় লোকাল ব্যাকআপ" else "Automatic Phone Backup",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "প্রতিদিন নির্দিষ্ট সময়ে স্বয়ংক্রিয় ব্যাকআপ সংরক্ষণ করুন" else "Automatically save daily local snapshots at scheduled time",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Switch(
                                    checked = config.isAutoPhoneBackupEnabled,
                                    onCheckedChange = { viewModel.setAutoPhoneBackupEnabled(it) }
                                )
                            }

                            // Scheduled Time Picker
                            if (config.isAutoPhoneBackupEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    viewModel.setScheduledTime(hourOfDay, minute)
                                                },
                                                config.scheduledBackupHour,
                                                config.scheduledBackupMinute,
                                                false
                                            ).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (languageMode == LanguageMode.BANGLA) "প্রতিদিনের সময়" else "Scheduled Daily Time", fontSize = 12.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = SolidIncome.copy(alpha = 0.12f)) {
                                        Text(
                                            text = config.formattedScheduledTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidIncome,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Create Local Backup & Restore file from storage
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.createLocalBackup {
                                    refreshLocalBackups()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "লোকাল ব্যাকআপ তৈরি করুন" else "Create Local Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                importFileLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "ফাইল নির্বাচন করে রিস্টোর" else "Restore From File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Local Backups List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত লোকাল ব্যাকআপসমূহ (${localBackups.size})" else "Local Snapshots (${localBackups.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { refreshLocalBackups() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Local Backup Files List
                if (localBackups.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো লোকাল ব্যাকআপ পাওয়া যায়নি" else "No local backups found in this folder",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(localBackups) { file ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))
                        val sizeKb = (file.length() / 1024).coerceAtLeast(1)

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                    Text("$dateStr • $sizeKb KB", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Share
                                    IconButton(
                                        onClick = {
                                            try {
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/json"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { deleteConfirmLocalFile = file },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SolidExpense, modifier = Modifier.size(16.dp))
                                    }

                                    // Restore
                                    Button(
                                        onClick = { restoreConfirmLocalFile = file },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "রিস্টোর" else "Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // OPTION 3: EXPORT OR IMPORT (CSV, QIF, JSON, Demo Sandbox)
            // -------------------------------------------------------------
            if (selectedTabIndex == 2) {
                // Export Section Header
                item {
                    SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "ডেটা এক্সপোর্ট (রপ্তানি)" else "DATA EXPORT")
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Export to CSV
                            SettingsListTile(
                                icon = Icons.Default.TableChart,
                                iconTint = SolidPrimary,
                                title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ফাইল এক্সপোর্ট" else "Export to CSV / Excel",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "তারিখ, লেনদেন ধরন, হিসাব এবং কলাম ফিল্টার করে ফাইল সেভ বা শেয়ার করুন" else "Custom date filter, transaction types, accounts & columns selector",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = { showCsvExportDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Export JSON Database
                            SettingsListTile(
                                icon = Icons.Default.Description,
                                iconTint = SolidPrimary,
                                title = if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ ডাটাবেজ ব্যাকআপ (JSON)" else "Full Database JSON Export",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "যেকোনো স্টোরেজ লোকেশনে সম্পূর্ণ অ্যাপ ব্যাকআপ ফাইল তৈরি করুন" else "Save complete database backup file to any chosen folder",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                    exportFileLauncher.launch("Budgeter_Backup_$dateStr.json")
                                }
                            )
                        }
                    }
                }

                // Import Section Header
                item {
                    SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "ডেটা ইম্পোর্ট (আমদানি)" else "DATA IMPORT")
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Import from CSV
                            SettingsListTile(
                                icon = Icons.Default.UploadFile,
                                iconTint = SolidIncome,
                                title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ফাইল ইম্পোর্ট" else "Import from CSV / Excel",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় প্রিভিউ, কলাম ম্যাপিং ও ডুপ্লিকেট শনাক্তকরণ" else "Smart CSV parser with auto group creation & preview dialog",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    csvFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "*/*"))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Import QIF format
                            SettingsListTile(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "QIF ফরম্যাট থেকে ইম্পোর্ট" else "Import from QIF format",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যাংক স্টেটমেন্ট ও Quicken ফরম্যাট (.qif)" else "Quicken Interchange Format (.qif) for bank exports",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    qifFileLauncher.launch(arrayOf("text/plain", "application/qif", "*/*"))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Restore JSON Database
                            SettingsListTile(
                                icon = Icons.Default.FileDownload,
                                iconTint = SolidIncome,
                                title = if (languageMode == LanguageMode.BANGLA) "JSON ব্যাকআপ থেকে রিস্টোর" else "Restore from JSON File",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "পূর্বে ব্যাকআপ নেওয়া JSON ফাইল থেকে সম্পূর্ণ ডাটাবেজ প্রতিস্থাপন" else "Restore and replace database from existing JSON backup file",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    importFileLauncher.launch(arrayOf("application/json", "*/*"))
                                }
                            )
                        }
                    }
                }

                // Demo Sandbox Section Header
                item {
                    SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "ডেমো স্যান্ডবক্স মোড" else "DEMO SANDBOX MODE")
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDemoMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isDemoMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(if (languageMode == LanguageMode.BANGLA) "ডেমো স্যান্ডবক্স" else "Demo Sandbox Environment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = if (isDemoMode) "Sample fixtures active • Real data is isolated & safe" else "Real database active",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Switch(
                                    checked = isDemoMode,
                                    onCheckedChange = { viewModel.setDemoMode(it) }
                                )
                            }

                            if (isDemoMode) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Reset 30+ sample transactions & budgets:",
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(
                                        onClick = { viewModel.resetDemoData() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset Sample Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & PICKERS
    // ==========================================

    // 1. Folder Picker Modal Bottom Sheet Dialog
    if (showFolderPickerDialog) {
        FolderPickerDialog(
            currentPath = config.localBackupDirectory,
            languageMode = languageMode,
            onFolderSelected = { selectedPath ->
                viewModel.setLocalBackupDirectory(selectedPath)
                refreshLocalBackups()
                showFolderPickerDialog = false
            },
            onDismiss = { showFolderPickerDialog = false }
        )
    }

    // 2. Cloud Provider Selection Dialog
    if (showProviderDialog) {
        val providers = listOf("Google Drive", "Dropbox", "Microsoft OneDrive", "Nextcloud / WebDAV")
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
            title = { Text("Select Cloud Provider", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    providers.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setCloudProvider(provider)
                                    showProviderDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.cloudProvider == provider,
                                onClick = {
                                    viewModel.setCloudProvider(provider)
                                    showProviderDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(provider, fontSize = 14.sp, fontWeight = if (config.cloudProvider == provider) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProviderDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // 3. Google Drive Snapshots / Restore Sheet Dialog
    if (showDriveRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showDriveRestoreDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Google Drive Snapshots", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(
                        onClick = {
                            signedInAccount?.let { viewModel.fetchDriveBackups(it) }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (driveBackups.isEmpty()) {
                        Text(
                            "No Google Drive backups found for this account. Tap 'Backup to Drive' to create one.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(driveBackups) { backupFile ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (backupFile.location == DriveBackupLocation.VISIBLE_APP_FOLDER) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = SolidPrimary.copy(alpha = 0.15f)) {
                                                        Text("Visible 'Budgeter'", fontSize = 9.sp, color = SolidPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                } else {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = SolidIncome.copy(alpha = 0.15f)) {
                                                        Text("Hidden AppData", fontSize = 9.sp, color = SolidIncome, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(backupFile.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                            Text(
                                                text = if (backupFile.modifiedTime.isNotEmpty()) "${backupFile.modifiedTime.take(10)} • ${if (backupFile.size > 0) "${backupFile.size / 1024} KB" else "Synced"}" else "Cloud Snapshot",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    deleteConfirmDriveFile = backupFile
                                                    showDriveRestoreDialog = false
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                            }

                                            Button(
                                                onClick = {
                                                    restoreConfirmDriveFile = backupFile
                                                    showDriveRestoreDialog = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
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
            },
            confirmButton = {
                TextButton(onClick = { showDriveRestoreDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 4. Drive Restore Confirmation Dialog
    restoreConfirmDriveFile?.let { backupFile ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ থেকে পুনরুদ্ধার?" else "Restore From Google Drive?",
            message = if (languageMode == LanguageMode.BANGLA)
                "'${backupFile.name}' বাজেটারে পুনরুদ্ধার করা হবে। বর্তমান স্থানীয় ডেটা এই স্ন্যাপশট দ্বারা প্রতিস্থাপিত হবে।"
            else
                "This will restore '${backupFile.name}' into Budgeter. Current local data will be replaced with this snapshot.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "পুনরুদ্ধার নিশ্চিত করুন" else "Confirm Restore",
            isDestructive = false,
            requiresAuth = securityConfig.requireAuthForBackupRestore,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                signedInAccount?.let { acc ->
                    viewModel.restoreFromGoogleDrive(acc, backupFile)
                }
                restoreConfirmDriveFile = null
            },
            onDismiss = { restoreConfirmDriveFile = null }
        )
    }

    // 5. Drive Delete Confirmation Dialog
    deleteConfirmDriveFile?.let { backupFile ->
        AlertDialog(
            onDismissRequest = { deleteConfirmDriveFile = null },
            title = { Text("Delete Drive Backup?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete '${backupFile.name}' from your Google Drive storage?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        signedInAccount?.let { acc ->
                            viewModel.deleteDriveBackup(acc, backupFile)
                        }
                        deleteConfirmDriveFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmDriveFile = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Local Restore Confirmation Dialog
    restoreConfirmLocalFile?.let { file ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "লোকাল ব্যাকআপ পুনরুদ্ধার?" else "Restore Local Backup?",
            message = if (languageMode == LanguageMode.BANGLA)
                "'${file.name}' ফাইল থেকে ডাটা পুনরুদ্ধার করা হবে। বর্তমান ডেটা প্রতিস্থাপিত হবে।"
            else
                "This will restore '${file.name}' into Budgeter. Current local database will be replaced with this snapshot.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "রিস্টোর করুন" else "Confirm Restore",
            isDestructive = false,
            requiresAuth = securityConfig.requireAuthForBackupRestore,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                val uri = Uri.fromFile(file)
                viewModel.restoreBackupFromUri(uri)
                restoreConfirmLocalFile = null
            },
            onDismiss = { restoreConfirmLocalFile = null }
        )
    }

    // 7. Local Delete Confirmation Dialog
    deleteConfirmLocalFile?.let { file ->
        AlertDialog(
            onDismissRequest = { deleteConfirmLocalFile = null },
            title = { Text("Delete Local Backup?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${file.name}' from device storage?", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            file.delete()
                            refreshLocalBackups()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        deleteConfirmLocalFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmLocalFile = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 8. CSV Import Preview Dialog
    csvImportPreview?.let { preview ->
        CsvImportPreviewDialog(
            preview = preview,
            languageMode = languageMode,
            isImporting = isImportingCsv,
            onConfirmImport = { skipDuplicates, autoCreateEntities, customHeaderMap ->
                viewModel.confirmCsvImport(
                    skipDuplicates = skipDuplicates,
                    autoCreateEntities = autoCreateEntities,
                    customHeaderMap = customHeaderMap
                )
            },
            onRemapRequested = { customHeaderMap ->
                viewModel.updateCsvCustomMapping(customHeaderMap)
            },
            onDismiss = {
                viewModel.clearCsvImportPreview()
            }
        )
    }

    // 9. CSV Export Configuration Dialog
    if (showCsvExportDialog) {
        CsvExportDialog(
            allTransactions = transactionsWithDetails,
            allAccounts = allAccounts,
            allCategories = allCategories,
            languageMode = languageMode,
            onSaveToFile = { cfg ->
                pendingCsvExportConfig = cfg
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                csvExportLauncher.launch("Budgeter_Export_$dateStr.csv")
                showCsvExportDialog = false
            },
            onShareCsv = { cfg ->
                viewModel.exportAndShareCsv(cfg) { shareUri ->
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share CSV Export"))
                }
                showCsvExportDialog = false
            },
            onDismiss = { showCsvExportDialog = false }
        )
    }
}

// Subcomponents for Clean Settings Styling

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsListTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 15.sp
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            trailingContent()
        }
    }
}
