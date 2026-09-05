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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.CsvExportDialog
import com.example.ui.components.CsvImportPreviewDialog
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

    var localBackups by remember { mutableStateOf<List<File>>(emptyList()) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var showDirectoryDialog by remember { mutableStateOf(false) }
    var showDriveRestoreDialog by remember { mutableStateOf(false) }
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var pendingCsvExportConfig by remember { mutableStateOf<CsvExportConfig?>(null) }
    var restoreConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var deleteConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }

    fun refreshLocalBackups() {
        localBackups = BackupManager.listLocalBackups(context)
    }

    LaunchedEffect(Unit) {
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
            pendingCsvExportConfig?.let { config ->
                viewModel.exportCsvToUri(destUri, config)
            }
        }
    }

    val isLoading = backupUiState is BackupUiState.Loading
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP APP BAR / BACK NAVIGATION
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ব্যাকআপ ও ক্লাউড সিঙ্ক" else "Backup & Cloud Sync",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // TOP HERO: QUICK SYNC & STATUS BANNER
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Quick Sync",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .then(if (isLoading) Modifier.rotate(spinAngle) else Modifier)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Data Sync Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val lastSyncText = if (config.lastSyncTimestamp > 0L) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(config.lastSyncTimestamp))
                            } else "Not synced yet"
                            Text(
                                text = "Last sync: $lastSyncText",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerQuickSync() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Quick Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // DATA ENVIRONMENT: DEMO MODE / REAL DATA
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDemoMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                color = if (isDemoMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Demo Mode",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ডেমো ডাটা এনভায়রনমেন্ট" else "Demo Data Environment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDemoMode) "Sample data active • Real data is protected & separate" else "Real database active • Operating on private data",
                                    fontSize = 12.sp,
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
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reset to fresh sample fixtures (30+ transactions, accounts & budgets):",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.resetDemoData() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Operation Status Feedback Card
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

        // ==========================================
        // 1. ONLINE BACKUP SETTINGS
        // ==========================================
        item {
            SettingsSectionHeader(title = "1. ONLINE BACKUP SETTINGS")
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Provider Selection Tile
                    SettingsListTile(
                        icon = Icons.Default.CloudQueue,
                        iconTint = SolidPrimary,
                        title = "Cloud Backup Provider",
                        subtitle = config.cloudProvider,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = config.cloudProvider,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        onClick = { showProviderDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Account Integration Tile
                    val isLinked = signedInAccount != null
                    val userName = signedInAccount?.displayName ?: if (isLinked) "Google User" else "No account linked"
                    val userEmail = signedInAccount?.email ?: "Link account to enable automated Google Drive sync"

                    SettingsListTile(
                        icon = Icons.Default.AccountCircle,
                        iconTint = if (isLinked) SolidPrimary else MaterialTheme.colorScheme.outline,
                        title = userName,
                        subtitle = userEmail,
                        trailingContent = {
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
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolidPrimary,
                                    checkedTrackColor = SolidPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Manual Actions: Backup to Drive & Restore from Drive
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                            Text("Backup to Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("Restore from Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. LOCAL BACKUP SETTINGS
        // ==========================================
        item {
            SettingsSectionHeader(title = "2. LOCAL BACKUP SETTINGS")
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Directory Configuration Tile
                    SettingsListTile(
                        icon = Icons.Default.Folder,
                        iconTint = SolidIncome,
                        title = "Default Backup Directory",
                        subtitle = config.localBackupDirectory,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = config.localBackupDirectory.substringAfterLast('/'),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit Directory", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        },
                        onClick = { showDirectoryDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Scheduled Backup Toggle
                    SettingsListTile(
                        icon = Icons.Default.Backup,
                        iconTint = SolidIncome,
                        title = "Automatic phone backup",
                        subtitle = "Automatically save daily local snapshots to device storage",
                        trailingContent = {
                            Switch(
                                checked = config.isAutoPhoneBackupEnabled,
                                onCheckedChange = { viewModel.setAutoPhoneBackupEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolidIncome,
                                    checkedTrackColor = SolidIncome.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )

                    // Time Picker for Automated Local Backups
                    AnimatedVisibility(visible = config.isAutoPhoneBackupEnabled) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            SettingsListTile(
                                icon = Icons.Default.AccessTime,
                                iconTint = SolidIncome,
                                title = "Set Time",
                                subtitle = "Daily schedule: ${config.formattedScheduledTime}",
                                trailingContent = {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SolidIncome.copy(alpha = 0.12f),
                                        modifier = Modifier.clickable {
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
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = config.formattedScheduledTime,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidIncome
                                            )
                                        }
                                    }
                                },
                                onClick = {
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
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Manual Actions: Phone Storage Backup & Restore
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup to phone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                importFileLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore from phone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. ATTACHMENT & SYNC SETTINGS
        // ==========================================
        item {
            SettingsSectionHeader(title = "3. ATTACHMENT & SYNC SETTINGS")
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Upload Attachments Toggle
                    SettingsListTile(
                        icon = Icons.Default.AttachFile,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Upload attachments",
                        subtitle = "Include transaction receipts, bills, and image attachments in cloud backup",
                        trailingContent = {
                            Switch(
                                checked = config.uploadAttachments,
                                onCheckedChange = { viewModel.setUploadAttachments(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolidPrimary,
                                    checkedTrackColor = SolidPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Auto Sync Toggle
                    SettingsListTile(
                        icon = Icons.Default.Sync,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Automatically sync data",
                        subtitle = "Trigger background sync instantly on every data change",
                        trailingContent = {
                            Switch(
                                checked = config.autoSyncData,
                                onCheckedChange = { viewModel.setAutoSyncData(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolidPrimary,
                                    checkedTrackColor = SolidPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Wi-Fi Only Toggle
                    SettingsListTile(
                        icon = Icons.Default.Wifi,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Wifi only",
                        subtitle = "Restricts QuickSync and attachment uploads to Wi-Fi connection",
                        trailingContent = {
                            Switch(
                                checked = config.wifiOnly,
                                onCheckedChange = { viewModel.setWifiOnly(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SolidPrimary,
                                    checkedTrackColor = SolidPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // 4. DATA IMPORT & EXPORT
        // ==========================================
        item {
            SettingsSectionHeader(title = if (languageMode == LanguageMode.BANGLA) "৪. ডেটা ইম্পোর্ট, এক্সপোর্ট ও রিসেট" else "4. DATA IMPORT, EXPORT & RESET")
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Import from Excel (.csv)
                    SettingsListTile(
                        icon = Icons.Default.TableChart,
                        iconTint = SolidIncome,
                        title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল থেকে ইম্পোর্ট" else "Import from Excel / CSV",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন, ক্যাটাগরি ও অ্যাকাউন্ট স্বয়ংক্রিয় প্রিভিউ সহ লোড করুন" else "Smart CSV import with auto-group creation and duplicate detection",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = {
                            csvFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "*/*"))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Export to CSV
                    SettingsListTile(
                        icon = Icons.Default.FileDownload,
                        iconTint = SolidPrimary,
                        title = if (languageMode == LanguageMode.BANGLA) "CSV তে এক্সপোর্ট (কাস্টম ফিল্টার)" else "Export Transactions (Custom CSV)",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "সময়সীমা, ধরন ও কলাম নির্বাচন করে এক্সপোর্ট বা শেয়ার করুন" else "Select custom date range, transaction types, accounts, and columns",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = {
                            showCsvExportDialog = true
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Import from QIF format
                    SettingsListTile(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Import from QIF format",
                        subtitle = "Quicken Interchange Format (.qif) for bank exports",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = {
                            qifFileLauncher.launch(arrayOf("text/plain", "application/qif", "*/*"))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Reset Data Action
                    SettingsListTile(
                        icon = Icons.Default.DeleteForever,
                        iconTint = SolidExpense,
                        title = "Reset data",
                        subtitle = "Erase all transactions, custom accounts, and restore initial defaults",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SolidExpense.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        },
                        onClick = { showResetConfirmationDialog = true }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & PICKERS
    // ==========================================

    // 1. Cloud Provider Selection Dialog
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

    // 2. Directory Configuration Dialog
    if (showDirectoryDialog) {
        var dirText by remember { mutableStateOf(config.localBackupDirectory) }
        AlertDialog(
            onDismissRequest = { showDirectoryDialog = false },
            title = { Text("Set Local Backup Directory", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Specify the directory on your phone storage where automatic and manual snapshots are stored.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dirText,
                        onValueChange = { dirText = it },
                        label = { Text("Folder Path") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Documents/Budgeter", "Download/Budgeter", "Blue Coins").forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { dirText = preset }
                            ) {
                                Text(preset, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setLocalBackupDirectory(dirText.trim().ifEmpty { "Documents/Budgeter" })
                        showDirectoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectoryDialog = false }) {
                    Text("Cancel")
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
                    Text("Google Drive Snapshots", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                                        Text("Hidden Folder", fontSize = 9.sp, color = SolidIncome, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
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

    // 4. Drive Restore Confirmation Dialog (Protected by Security Auth)
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

    // 6. Reset Data Confirmation Dialog (Protected by Security Auth)
    if (showResetConfirmationDialog) {
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "সমস্ত ডেটা রিসেট করবেন?" else "Reset All Data?",
            message = if (languageMode == LanguageMode.BANGLA)
                "এটি সমস্ত লেনদেন, পুনরাবৃত্তিমূলক বিল, মাসিক বাজেট এবং কাস্টম অ্যাকাউন্ট মুছে ফেলবে এবং অ্যাপকে ডিফল্ট কাঠামোতে ফিরিয়ে আনবে। এই কাজ পুনরায় ফিরিয়ে আনা যাবে না।"
            else
                "This will completely erase all transactions, recurring bills, monthly budgets, and custom accounts, restoring the application back to its default clean structure. This action cannot be undone.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "হ্যাঁ, সব রিসেট করুন" else "Yes, Reset Everything",
            isDestructive = true,
            requiresAuth = securityConfig.requireAuthForBackupRestore || securityConfig.requireAuthForTrashClear,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                viewModel.resetAllData()
                showResetConfirmationDialog = false
            },
            onDismiss = { showResetConfirmationDialog = false }
        )
    }

    // 7. CSV Import Preview Dialog (Smart Import with duplicate check & auto-creation)
    csvImportPreview?.let { preview ->
        CsvImportPreviewDialog(
            preview = preview,
            languageMode = languageMode,
            isImporting = isImportingCsv,
            onConfirmImport = { skipDuplicates, autoCreateEntities ->
                viewModel.confirmCsvImport(
                    skipDuplicates = skipDuplicates,
                    autoCreateEntities = autoCreateEntities
                )
            },
            onDismiss = {
                viewModel.clearCsvImportPreview()
            }
        )
    }

    // 8. CSV Export Configuration Dialog (Select range, types, columns & export/share)
    if (showCsvExportDialog) {
        CsvExportDialog(
            allTransactions = transactionsWithDetails,
            allAccounts = allAccounts,
            allCategories = allCategories,
            languageMode = languageMode,
            onSaveToFile = { config ->
                pendingCsvExportConfig = config
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                csvExportLauncher.launch("Budgeter_Export_$dateStr.csv")
                showCsvExportDialog = false
            },
            onShareCsv = { config ->
                viewModel.exportAndShareCsv(config) { shareUri ->
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
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
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
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
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
