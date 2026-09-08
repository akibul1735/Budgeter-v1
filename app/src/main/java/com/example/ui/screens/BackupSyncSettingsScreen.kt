package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.core.content.ContextCompat
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
import com.example.util.CloudAccountInfo
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
    ONLINE("Online Sync", "অনলাইন সিঙ্ক", Icons.Default.CloudUpload),
    LOCAL("Local Backup", "লোকাল ব্যাকআপ", Icons.Default.Storage),
    EXPORT_IMPORT("Export / Import", "এক্সপোর্ট / ইমপোর্ট", Icons.Default.SwapVert)
}

val SUPPORTED_CLOUD_PROVIDERS = listOf(
    "Google Drive",
    "Microsoft OneDrive",
    "Dropbox",
    "Nextcloud / WebDAV",
    "Custom Cloud Server"
)

fun getProviderIcon(provider: String): ImageVector {
    return when {
        provider.contains("Google", ignoreCase = true) -> Icons.Default.Cloud
        provider.contains("OneDrive", ignoreCase = true) -> Icons.Default.CloudQueue
        provider.contains("Dropbox", ignoreCase = true) -> Icons.Default.Folder
        provider.contains("Nextcloud", ignoreCase = true) || provider.contains("WebDAV", ignoreCase = true) -> Icons.Default.Storage
        else -> Icons.Default.SdStorage
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val secondarySignedInAccount by viewModel.secondarySignedInGoogleAccount.collectAsStateWithLifecycle()
    val secondaryDriveBackups by viewModel.secondaryDriveBackups.collectAsStateWithLifecycle()

    val csvImportPreview by viewModel.csvImportPreview.collectAsStateWithLifecycle()
    val isImportingCsv by viewModel.isImportingCsv.collectAsStateWithLifecycle()
    val transactionsWithDetails by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()

    var selectedMainTab by remember { mutableIntStateOf(0) }
    var onlineSubTabIndex by remember { mutableIntStateOf(0) }
    var activeDriveAccountTab by remember { mutableIntStateOf(0) }
    var showFolderTypeDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var showProviderDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var showAccountEditDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var localBackups by remember { mutableStateOf<List<File>>(emptyList()) }
    var showFolderPickerDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var pendingCsvExportConfig by remember { mutableStateOf<CsvExportConfig?>(null) }
    var restoreConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var deleteConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var restoreConfirmLocalFile by remember { mutableStateOf<File?>(null) }
    var deleteConfirmLocalFile by remember { mutableStateOf<File?>(null) }

    fun refreshLocalBackups() {
        localBackups = BackupManager.listLocalBackups(context, config.localBackupDirectory)
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                true
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasStoragePermission = perms.values.any { it } || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        refreshLocalBackups()
    }

    val dirPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { treeUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setLocalBackupDirectory(treeUri.toString())
            refreshLocalBackups()
        }
    }

    LaunchedEffect(config.localBackupDirectory) {
        refreshLocalBackups()
        val existingAccount = GoogleDriveService.getSignedInAccount(context)
        viewModel.updateSignedInAccount(existingAccount)
        if (existingAccount != null) {
            viewModel.fetchDriveBackups(existingAccount)
        }
        if (secondarySignedInAccount != null) {
            viewModel.fetchSecondaryDriveBackups(secondarySignedInAccount!!)
        }
    }

    // Google Sign-In Launchers
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.updateSignedInAccount(account)
            if (account != null) {
                viewModel.setPrimaryAccount(
                    email = account.email ?: "",
                    displayName = account.displayName ?: "",
                    isLinked = true
                )
                viewModel.fetchDriveBackups(account)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val current = GoogleDriveService.getSignedInAccount(context)
            viewModel.updateSignedInAccount(current)
        }
    }

    val secondaryGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.updateSecondarySignedInAccount(account)
            if (account != null) {
                viewModel.setSecondaryAccount(
                    email = account.email ?: "",
                    displayName = account.displayName ?: "",
                    isLinked = true
                )
                viewModel.fetchSecondaryDriveBackups(account)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val account = task.result
            viewModel.updateSecondarySignedInAccount(account)
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_data_management")
    ) {
        // TOP APP BAR - Clean Material 3
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ক্লাউড সিঙ্ক, লোকাল ব্যাকআপ ও ফাইল ট্রান্সফার" else "Independent Cloud Sync, Local Backup & Export/Import",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // MAIN NAVIGATION TABS (Material 3 TabRow)
        TabRow(
            selectedTabIndex = selectedMainTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            DataManagementTab.values().forEachIndexed { index, tab ->
                val isSelected = selectedMainTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedMainTab = index },
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

        // SCROLLABLE CONTENT BODY
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
                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                is BackupUiState.Error -> {
                    item {
                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            // =================================================================
            // TAB 1: ONLINE SYNC (SEPARATE PROVIDERS & ACCOUNTS FOR BOTH DRIVES)
            // =================================================================
            if (selectedMainTab == 0) {
                // Secondary Tab Selector for Drive 1 | Drive 2 | Dual Sync
                item {
                    SecondaryTabRow(
                        selectedTabIndex = onlineSubTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        listOf(
                            Triple(0, if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১" else "Drive 1 (Primary)", Icons.Default.CloudQueue),
                            Triple(1, if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২" else "Drive 2 (Secondary)", Icons.Default.CloudDone),
                            Triple(2, if (languageMode == LanguageMode.BANGLA) "ডুয়েল সিঙ্ক" else "Dual Sync", Icons.Default.Sync)
                        ).forEach { (index, title, icon) ->
                            val isSelected = onlineSubTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    onlineSubTabIndex = index
                                    activeDriveAccountTab = if (index == 1) 1 else 0
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Text(
                                            text = title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // DRIVE 1 (PRIMARY) SUB-TAB
                // -------------------------------------------------------------
                if (onlineSubTabIndex == 0) {
                    val primary = config.primaryAccount
                    val isGoogleDrive = primary.provider.equals("Google Drive", ignoreCase = true)
                    val isLinked = if (isGoogleDrive) signedInAccount != null else (primary.isLinked && primary.email.isNotBlank())
                    val displayEmail = if (isGoogleDrive) (signedInAccount?.email ?: "") else primary.email
                    val displayName = if (isGoogleDrive) (signedInAccount?.displayName ?: "") else primary.displayName

                    // 1. Cloud Provider Selector Card
                    item {
                        DriveProviderCard(
                            driveIndex = 1,
                            driveLabel = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ সার্ভিস প্রোভাইডার" else "Drive 1 Cloud Provider",
                            provider = primary.provider,
                            onChangeProvider = { showProviderDialogForAccount = 1 },
                            languageMode = languageMode
                        )
                    }

                    // 2. Account Connection Card
                    item {
                        DriveAccountConnectionCard(
                            driveIndex = 1,
                            provider = primary.provider,
                            isLinked = isLinked,
                            email = displayEmail,
                            displayName = displayName,
                            serverUrl = primary.serverUrl,
                            lastSyncTimestamp = if (primary.lastSyncTimestamp > 0L) primary.lastSyncTimestamp else config.lastSyncTimestamp,
                            isLoading = isLoading,
                            onConnect = {
                                if (isGoogleDrive) {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    googleSignInLauncher.launch(client.signInIntent)
                                } else {
                                    showAccountEditDialogForAccount = 1
                                }
                            },
                            onDisconnect = {
                                if (isGoogleDrive) {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    client.signOut().addOnCompleteListener {
                                        viewModel.updateSignedInAccount(null)
                                        viewModel.setPrimaryAccount("", "", false)
                                    }
                                } else {
                                    viewModel.setPrimaryAccount("", "", false, "")
                                }
                            },
                            onEditDetails = {
                                showAccountEditDialogForAccount = 1
                            },
                            onSyncNow = {
                                if (isGoogleDrive) {
                                    signedInAccount?.let { acc ->
                                        viewModel.backupToGoogleDrive(acc)
                                    } ?: viewModel.triggerQuickSync()
                                } else {
                                    viewModel.triggerQuickSync()
                                }
                            },
                            languageMode = languageMode
                        )
                    }

                    // 3. Drive 1 Configuration Settings Card
                    item {
                        DriveSettingsCard(
                            folderType = primary.driveFolderType,
                            autoSync = primary.autoSync,
                            wifiOnly = primary.wifiOnly,
                            uploadAttachments = primary.uploadAttachments,
                            onSelectFolderType = { showFolderTypeDialogForAccount = 1 },
                            onToggleAutoSync = { viewModel.setPrimaryAutoSync(it) },
                            onToggleWifiOnly = { viewModel.setPrimaryWifiOnly(it) },
                            onToggleUploadAttachments = { viewModel.setPrimaryUploadAttachments(it) },
                            languageMode = languageMode
                        )
                    }

                    // 4. Drive 1 Snapshots / Backups
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ ক্লাউড স্ন্যাপশটস (${driveBackups.size})" else "Drive 1 Cloud Snapshots (${driveBackups.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { signedInAccount?.let { viewModel.fetchDriveBackups(it) } }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (!isLinked) {
                        item {
                            EmptyStateCard(
                                message = if (languageMode == LanguageMode.BANGLA)
                                    "ড্রাইভ ১-এ ক্লাউড সিঙ্ক সক্রিয় করতে উপরের '${primary.provider}' একাউন্ট যুক্ত করুন।"
                                else
                                    "Connect your ${primary.provider} account above to manage Drive 1 cloud snapshots."
                            )
                        }
                    } else if (driveBackups.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = if (languageMode == LanguageMode.BANGLA)
                                    "ড্রাইভ ১-এ এখনো কোনো ব্যাকআপ নেই। 'সিঙ্ক করুন' বাটনে ট্যাপ করুন।"
                                else
                                    "No cloud snapshots found in Drive 1. Tap 'Sync Now' to save a backup."
                            )
                        }
                    } else {
                        items(driveBackups) { backupFile ->
                            DriveSnapshotItem(
                                backupFile = backupFile,
                                onRestore = {
                                    activeDriveAccountTab = 0
                                    restoreConfirmDriveFile = backupFile
                                },
                                onDelete = {
                                    activeDriveAccountTab = 0
                                    deleteConfirmDriveFile = backupFile
                                },
                                languageMode = languageMode
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // DRIVE 2 (SECONDARY) SUB-TAB
                // -------------------------------------------------------------
                if (onlineSubTabIndex == 1) {
                    val secondary = config.secondaryAccount
                    val isGoogleDrive = secondary.provider.equals("Google Drive", ignoreCase = true)
                    val isLinked = if (isGoogleDrive) secondarySignedInAccount != null else (secondary.isLinked && secondary.email.isNotBlank())
                    val displayEmail = if (isGoogleDrive) (secondarySignedInAccount?.email ?: "") else secondary.email
                    val displayName = if (isGoogleDrive) (secondarySignedInAccount?.displayName ?: "") else secondary.displayName

                    // 1. Cloud Provider Selector Card
                    item {
                        DriveProviderCard(
                            driveIndex = 2,
                            driveLabel = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ সার্ভিস প্রোভাইডার" else "Drive 2 Cloud Provider",
                            provider = secondary.provider,
                            onChangeProvider = { showProviderDialogForAccount = 2 },
                            languageMode = languageMode
                        )
                    }

                    // 2. Account Connection Card
                    item {
                        DriveAccountConnectionCard(
                            driveIndex = 2,
                            provider = secondary.provider,
                            isLinked = isLinked,
                            email = displayEmail,
                            displayName = displayName,
                            serverUrl = secondary.serverUrl,
                            lastSyncTimestamp = secondary.lastSyncTimestamp,
                            isLoading = isLoading,
                            onConnect = {
                                if (isGoogleDrive) {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    secondaryGoogleSignInLauncher.launch(client.signInIntent)
                                } else {
                                    showAccountEditDialogForAccount = 2
                                }
                            },
                            onDisconnect = {
                                if (isGoogleDrive) {
                                    viewModel.updateSecondarySignedInAccount(null)
                                    viewModel.setSecondaryAccount("", "", false)
                                } else {
                                    viewModel.setSecondaryAccount("", "", false, "")
                                }
                            },
                            onEditDetails = {
                                showAccountEditDialogForAccount = 2
                            },
                            onSyncNow = {
                                if (isGoogleDrive) {
                                    secondarySignedInAccount?.let { acc ->
                                        viewModel.backupToSecondaryGoogleDrive(acc)
                                    } ?: viewModel.triggerQuickSync()
                                } else {
                                    viewModel.triggerQuickSync()
                                }
                            },
                            languageMode = languageMode
                        )
                    }

                    // 3. Drive 2 Configuration Settings Card
                    item {
                        DriveSettingsCard(
                            folderType = secondary.driveFolderType,
                            autoSync = secondary.autoSync,
                            wifiOnly = secondary.wifiOnly,
                            uploadAttachments = secondary.uploadAttachments,
                            onSelectFolderType = { showFolderTypeDialogForAccount = 2 },
                            onToggleAutoSync = { viewModel.setSecondaryAutoSync(it) },
                            onToggleWifiOnly = { viewModel.setSecondaryWifiOnly(it) },
                            onToggleUploadAttachments = { viewModel.setSecondaryUploadAttachments(it) },
                            languageMode = languageMode
                        )
                    }

                    // 4. Drive 2 Snapshots / Backups
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ ক্লাউড স্ন্যাপশটস (${secondaryDriveBackups.size})" else "Drive 2 Cloud Snapshots (${secondaryDriveBackups.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = { secondarySignedInAccount?.let { viewModel.fetchSecondaryDriveBackups(it) } }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (!isLinked) {
                        item {
                            EmptyStateCard(
                                message = if (languageMode == LanguageMode.BANGLA)
                                    "ড্রাইভ ২-এ ক্লাউড ব্যাকআপ সক্রিয় করতে উপরের '${secondary.provider}' একাউন্ট যুক্ত করুন।"
                                else
                                    "Connect your ${secondary.provider} account above to manage Drive 2 cloud snapshots."
                            )
                        }
                    } else if (secondaryDriveBackups.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = if (languageMode == LanguageMode.BANGLA)
                                    "ড্রাইভ ২-এ এখনো কোনো ব্যাকআপ নেই। 'সিঙ্ক করুন' বাটনে ট্যাপ করুন।"
                                else
                                    "No cloud snapshots found in Drive 2. Tap 'Sync Now' to save a backup."
                            )
                        }
                    } else {
                        items(secondaryDriveBackups) { backupFile ->
                            DriveSnapshotItem(
                                backupFile = backupFile,
                                onRestore = {
                                    activeDriveAccountTab = 1
                                    restoreConfirmDriveFile = backupFile
                                },
                                onDelete = {
                                    activeDriveAccountTab = 1
                                    deleteConfirmDriveFile = backupFile
                                },
                                languageMode = languageMode
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // DUAL SYNC SUB-TAB (SIMULTANEOUS DUAL-CLOUD REDUNDANCY)
                // -------------------------------------------------------------
                if (onlineSubTabIndex == 2) {
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় ডুয়েল সিঙ্ক" else "Dual Cloud Auto-Sync",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "ডাটা পরিবর্তিত হলে একসাথে উভয় ড্রাইভেই ব্যাকআপ সুরক্ষিত রাখুন"
                                            else
                                                "Automatically upload snapshots to both cloud drives simultaneously",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Switch(
                                        checked = config.isDualSyncEnabled,
                                        onCheckedChange = { viewModel.setDualSyncEnabled(it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // Side by side Summary of Drive 1 and Drive 2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Drive 1 summary
                                    val isP1Linked = if (config.primaryAccount.provider.equals("Google Drive", ignoreCase = true)) signedInAccount != null else (config.primaryAccount.isLinked && config.primaryAccount.email.isNotBlank())
                                    OutlinedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(getProviderIcon(config.primaryAccount.provider), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Drive 1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(config.primaryAccount.provider, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = if (isP1Linked) (signedInAccount?.email ?: config.primaryAccount.email).ifEmpty { "Connected" } else "Not Linked",
                                                fontSize = 10.sp,
                                                color = if (isP1Linked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Drive 2 summary
                                    val isP2Linked = if (config.secondaryAccount.provider.equals("Google Drive", ignoreCase = true)) secondarySignedInAccount != null else (config.secondaryAccount.isLinked && config.secondaryAccount.email.isNotBlank())
                                    OutlinedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(getProviderIcon(config.secondaryAccount.provider), contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Drive 2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(config.secondaryAccount.provider, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = if (isP2Linked) (secondarySignedInAccount?.email ?: config.secondaryAccount.email).ifEmpty { "Connected" } else "Not Linked",
                                                fontSize = 10.sp,
                                                color = if (isP2Linked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.backupToBothDrives(signedInAccount, secondarySignedInAccount)
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "উভয় ড্রাইভে একসাথে ব্যাকআপ নিন" else "Sync Both Cloud Drives Now",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 2: LOCAL BACKUP
            // =================================================================
            if (selectedMainTab == 1) {
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "লোকাল ব্যাকআপ ফোল্ডার" else "Local Storage Directory",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = BackupManager.formatDirectoryDisplayName(config.localBackupDirectory),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { dirPickerLauncher.launch(null) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("SAF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { showFolderPickerDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "নির্বাচন" else "Select", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Auto phone backup switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় লোকাল ব্যাকআপ" else "Daily Auto Phone Backup",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "প্রতিদিন নির্দিষ্ট সময়ে লোকাল স্ন্যাপশট সংরক্ষণ করুন" else "Automatically save daily snapshot at scheduled time",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Switch(
                                    checked = config.isAutoPhoneBackupEnabled,
                                    onCheckedChange = { viewModel.setAutoPhoneBackupEnabled(it) }
                                )
                            }

                            if (config.isAutoPhoneBackupEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (languageMode == LanguageMode.BANGLA) "প্রতিদিনের সময়" else "Scheduled Backup Time", fontSize = 12.sp)
                                        }
                                        Text(
                                            text = config.formattedScheduledTime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!hasStoragePermission && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                    storagePermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    )
                                }
                                viewModel.createLocalBackup {
                                    refreshLocalBackups()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "ব্যাকআপ তৈরি করুন" else "Create Local Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text(if (languageMode == LanguageMode.BANGLA) "ফাইল রিস্টোর" else "Restore From File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Local Backups Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত লোকাল ব্যাকআপ (${localBackups.size})" else "Local Snapshots (${localBackups.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { refreshLocalBackups() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (localBackups.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = if (languageMode == LanguageMode.BANGLA) "কোনো লোকাল ব্যাকআপ পাওয়া যায়নি" else "No local backups found in this folder."
                        )
                    }
                } else {
                    items(localBackups) { file ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))
                        val sizeKb = (file.length() / 1024).coerceAtLeast(1)

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                                    IconButton(
                                        onClick = { deleteConfirmLocalFile = file },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = { restoreConfirmLocalFile = file },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "রিস্টোর" else "Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: EXPORT / IMPORT (CSV, QIF, JSON, Demo Sandbox)
            // =================================================================
            if (selectedMainTab == 2) {
                item {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডাটা এক্সপোর্ট (রপ্তানি)" else "DATA EXPORT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsListTile(
                                icon = Icons.Default.TableChart,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ফাইল এক্সপোর্ট" else "Export to CSV / Excel",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিল্টার ও কলাম কাস্টমাইজ করে ফাইল তৈরি করুন" else "Custom date filter, transaction types & columns selector",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = { showCsvExportDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            SettingsListTile(
                                icon = Icons.Default.Description,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ ডাটাবেজ ব্যাকআপ (JSON)" else "Full Database JSON Export",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "যেকোনো লোকেশনে ডাটাবেজ ব্যাকআপ সেভ করুন" else "Save complete database backup file to any directory",
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

                item {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডাটা ইম্পোর্ট (আমদানি)" else "DATA IMPORT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsListTile(
                                icon = Icons.Default.UploadFile,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ফাইল ইম্পোর্ট" else "Import from CSV / Excel",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় প্রিভিউ ও কলাম ম্যাপিং" else "Smart CSV parser with auto mapping and preview dialog",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    csvFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "*/*"))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            SettingsListTile(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "QIF ফরম্যাট থেকে ইম্পোর্ট" else "Import from QIF Format",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যাংক স্টেটমেন্ট ও Quicken ফরম্যাট (.qif)" else "Quicken Interchange Format (.qif) for bank exports",
                                trailingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                },
                                onClick = {
                                    qifFileLauncher.launch(arrayOf("text/plain", "application/qif", "*/*"))
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            SettingsListTile(
                                icon = Icons.Default.FileDownload,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = if (languageMode == LanguageMode.BANGLA) "JSON ব্যাকআপ থেকে রিস্টোর" else "Restore from JSON File",
                                subtitle = if (languageMode == LanguageMode.BANGLA) "পূর্বে ব্যাকআপ নেওয়া JSON ফাইল থেকে সম্পূর্ণ ডাটা প্রতিস্থাপন" else "Restore and replace database from existing JSON backup",
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

                item {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডেমো স্যান্ডবক্স মোড" else "DEMO SANDBOX MODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isDemoMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            text = if (isDemoMode) "Sample data active • Real data is safely isolated" else "Real database active",
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
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reset sample transactions & budgets:", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    OutlinedButton(
                                        onClick = { viewModel.resetDemoData() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS & ACTION PROMPTS
    // =========================================================================

    // 1. Cloud Provider Selection Dialog for Drive 1 or Drive 2
    showProviderDialogForAccount?.let { driveIndex ->
        val currentSelected = if (driveIndex == 1) config.primaryAccount.provider else config.secondaryAccount.provider
        AlertDialog(
            onDismissRequest = { showProviderDialogForAccount = null },
            title = {
                Text(
                    text = if (driveIndex == 1) "Select Provider for Drive 1" else "Select Provider for Drive 2",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SUPPORTED_CLOUD_PROVIDERS.forEach { provider ->
                        val isSelected = currentSelected == provider
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (driveIndex == 1) {
                                        viewModel.setPrimaryProvider(provider)
                                    } else {
                                        viewModel.setSecondaryProvider(provider)
                                    }
                                    showProviderDialogForAccount = null
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        if (driveIndex == 1) {
                                            viewModel.setPrimaryProvider(provider)
                                        } else {
                                            viewModel.setSecondaryProvider(provider)
                                        }
                                        showProviderDialogForAccount = null
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = getProviderIcon(provider),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = provider,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProviderDialogForAccount = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Account Credentials Setup Dialog for Non-Google providers (OneDrive, Dropbox, Nextcloud, Custom Server)
    showAccountEditDialogForAccount?.let { driveIndex ->
        val acc = if (driveIndex == 1) config.primaryAccount else config.secondaryAccount
        var editEmail by remember { mutableStateOf(acc.email) }
        var editName by remember { mutableStateOf(acc.displayName) }
        var editServer by remember { mutableStateOf(acc.serverUrl) }

        AlertDialog(
            onDismissRequest = { showAccountEditDialogForAccount = null },
            title = {
                Text(
                    text = "Configure ${acc.provider} (Drive $driveIndex)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter account credentials or connection details for ${acc.provider}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Account Email / Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name / Account Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (acc.provider.contains("Nextcloud", ignoreCase = true) || acc.provider.contains("WebDAV", ignoreCase = true) || acc.provider.contains("Custom", ignoreCase = true)) {
                        OutlinedTextField(
                            value = editServer,
                            onValueChange = { editServer = it },
                            label = { Text("Server URL / WebDAV Endpoint") },
                            singleLine = true,
                            placeholder = { Text("https://cloud.example.com/dav") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (driveIndex == 1) {
                            viewModel.setPrimaryAccount(
                                email = editEmail.trim(),
                                displayName = editName.trim().ifEmpty { editEmail.trim() },
                                isLinked = editEmail.trim().isNotEmpty(),
                                serverUrl = editServer.trim()
                            )
                        } else {
                            viewModel.setSecondaryAccount(
                                email = editEmail.trim(),
                                displayName = editName.trim().ifEmpty { editEmail.trim() },
                                isLinked = editEmail.trim().isNotEmpty(),
                                serverUrl = editServer.trim()
                            )
                        }
                        showAccountEditDialogForAccount = null
                    }
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountEditDialogForAccount = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Folder Type Selection Dialog
    showFolderTypeDialogForAccount?.let { targetAcc ->
        val currentType = if (targetAcc == 1) config.primaryAccount.driveFolderType else config.secondaryAccount.driveFolderType
        val folderOptions = listOf(
            "Visible 'Budgeter' Folder" to if (languageMode == LanguageMode.BANGLA) "মূল ক্লাউড ডিরেক্টরিতে দৃশ্যমান 'Budgeter' ফোল্ডারে সেভ হবে" else "Directly visible and browsable in cloud storage root folder",
            "Hidden App Data" to if (languageMode == LanguageMode.BANGLA) "লুকায়িত ও সুরক্ষিত অ্যাপ ডাটাতে সেভ হবে" else "Isolated and protected in private AppData folder",
            "Both (Visible & Hidden)" to if (languageMode == LanguageMode.BANGLA) "একসাথে দৃশ্যমান এবং লুকায়িত উভয় স্থানে কপি রাখা হবে" else "Saves duplicate snapshot copies in both folders simultaneously"
        )
        AlertDialog(
            onDismissRequest = { showFolderTypeDialogForAccount = null },
            title = {
                Text(
                    text = if (targetAcc == 1) "Drive 1 Storage Location" else "Drive 2 Storage Location",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    folderOptions.forEach { (option, desc) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentType == option) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (targetAcc == 1) {
                                        viewModel.setPrimaryFolderType(option)
                                    } else {
                                        viewModel.setSecondaryFolderType(option)
                                    }
                                    showFolderTypeDialogForAccount = null
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = currentType == option,
                                    onClick = {
                                        if (targetAcc == 1) {
                                            viewModel.setPrimaryFolderType(option)
                                        } else {
                                            viewModel.setSecondaryFolderType(option)
                                        }
                                        showFolderTypeDialogForAccount = null
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(option, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderTypeDialogForAccount = null }) {
                    Text("Done")
                }
            }
        )
    }

    // 4. Local Folder Picker Sheet
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

    // 5. Cloud Drive Restore Confirmation Dialog
    restoreConfirmDriveFile?.let { backupFile ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড ড্রাইভ থেকে পুনরুদ্ধার?" else "Restore From Cloud Drive?",
            message = if (languageMode == LanguageMode.BANGLA)
                "'${backupFile.name}' বাজেটারে পুনরুদ্ধার করা হবে। বর্তমান ডেটা এই স্ন্যাপশট দ্বারা প্রতিস্থাপিত হবে।"
            else
                "This will restore '${backupFile.name}' into Budgeter. Current local data will be replaced with this snapshot.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "পুনরুদ্ধার নিশ্চিত করুন" else "Confirm Restore",
            isDestructive = false,
            requiresAuth = securityConfig.requireAuthForBackupRestore,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                if (activeDriveAccountTab == 1) {
                    secondarySignedInAccount?.let { acc ->
                        viewModel.restoreFromSecondaryGoogleDrive(acc, backupFile)
                    }
                } else {
                    signedInAccount?.let { acc ->
                        viewModel.restoreFromGoogleDrive(acc, backupFile)
                    }
                }
                restoreConfirmDriveFile = null
            },
            onDismiss = { restoreConfirmDriveFile = null }
        )
    }

    // 6. Cloud Drive Delete Confirmation Dialog
    deleteConfirmDriveFile?.let { backupFile ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ব্যাকআপ ডিলিট করবেন?" else "Delete Cloud Backup?",
            message = if (languageMode == LanguageMode.BANGLA)
                "আপনি কি নিশ্চিত যে '${backupFile.name}' ব্যাকআপ ফাইলটি ক্লাউড ড্রাইভ থেকে মুছে ফেলতে চান?"
            else
                "Are you sure you want to permanently delete '${backupFile.name}' from your cloud drive?",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "ডিলিট করুন" else "Delete",
            isDestructive = true,
            requiresAuth = securityConfig.requireAuthForBackupDeletion,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                if (activeDriveAccountTab == 1) {
                    secondarySignedInAccount?.let { acc ->
                        viewModel.deleteSecondaryDriveBackup(acc, backupFile)
                    }
                } else {
                    signedInAccount?.let { acc ->
                        viewModel.deleteDriveBackup(acc, backupFile)
                    }
                }
                deleteConfirmDriveFile = null
            },
            onDismiss = { deleteConfirmDriveFile = null }
        )
    }

    // 7. Local Restore Confirmation Dialog
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

    // 8. Local Delete Confirmation Dialog
    deleteConfirmLocalFile?.let { file ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "লোকাল ব্যাকআপ ডিলিট করবেন?" else "Delete Local Backup?",
            message = if (languageMode == LanguageMode.BANGLA)
                "আপনি কি নিশ্চিত যে '${file.name}' ফাইলটি ডিভাইস স্টোরেজ থেকে মুছে ফেলতে চান?"
            else
                "Are you sure you want to delete '${file.name}' from device storage?",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "ডিলিট করুন" else "Delete",
            isDestructive = true,
            requiresAuth = securityConfig.requireAuthForBackupDeletion,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                try {
                    BackupManager.deleteLocalBackup(context, file, config.localBackupDirectory)
                    refreshLocalBackups()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                deleteConfirmLocalFile = null
            },
            onDismiss = { deleteConfirmLocalFile = null }
        )
    }

    // 9. CSV Import Preview Dialog
    csvImportPreview?.let { preview ->
        CsvImportPreviewDialog(
            preview = preview,
            languageMode = languageMode,
            isImporting = isImportingCsv,
            onConfirmImport = { skipDuplicates, autoCreateEntities, customHeaderMap, repairedRows, autoRepairUnsupported ->
                viewModel.confirmCsvImport(
                    skipDuplicates = skipDuplicates,
                    autoCreateEntities = autoCreateEntities,
                    customHeaderMap = customHeaderMap,
                    repairedRows = repairedRows,
                    autoRepairUnsupported = autoRepairUnsupported
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

    // 10. CSV Export Configuration Dialog
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

// =============================================================================
// REUSABLE MATERIAL 3 UI COMPONENTS
// =============================================================================

@Composable
private fun DriveProviderCard(
    driveIndex: Int,
    driveLabel: String,
    provider: String,
    onChangeProvider: () -> Unit,
    languageMode: LanguageMode
) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (driveIndex == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getProviderIcon(provider),
                            contentDescription = null,
                            tint = if (driveIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = driveLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = provider,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = onChangeProvider,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পরিবর্তন" else "Change",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DriveAccountConnectionCard(
    driveIndex: Int,
    provider: String,
    isLinked: Boolean,
    email: String,
    displayName: String,
    serverUrl: String,
    lastSyncTimestamp: Long,
    isLoading: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onEditDetails: () -> Unit,
    onSyncNow: () -> Unit,
    languageMode: LanguageMode
) {
    val containerColor = if (driveIndex == 1) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    }
    val brandColor = if (driveIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
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
                        color = if (isLinked) brandColor else MaterialTheme.colorScheme.outline,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (displayName.isNotBlank()) displayName else if (isLinked) "Connected Account" else if (languageMode == LanguageMode.BANGLA) "একাউন্ট যুক্ত নেই" else "No Account Connected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (serverUrl.isNotBlank()) {
                            Text(
                                text = serverUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isLinked) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEditDetails, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = brandColor, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDisconnect, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.LinkOff, contentDescription = "Disconnect", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (languageMode == LanguageMode.BANGLA) "কানেক্ট" else "Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lastSyncText = if (lastSyncTimestamp > 0L) {
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
                } else if (languageMode == LanguageMode.BANGLA) "এখনো সিঙ্ক হয়নি" else "Not synced yet"

                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সর্বশেষ সিঙ্ক: $lastSyncText" else "Last synced: $lastSyncText",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = onSyncNow,
                    enabled = !isLoading && isLinked,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "সিঙ্ক করুন" else "Sync Drive $driveIndex Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveSettingsCard(
    folderType: String,
    autoSync: Boolean,
    wifiOnly: Boolean,
    uploadAttachments: Boolean,
    onSelectFolderType: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onToggleWifiOnly: (Boolean) -> Unit,
    onToggleUploadAttachments: (Boolean) -> Unit,
    languageMode: LanguageMode
) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            SettingsListTile(
                icon = Icons.Default.FolderOpen,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "স্টোরেজ ফোল্ডার লোকেশন" else "Storage Destination Folder",
                subtitle = folderType,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (folderType.contains("Visible")) "Visible" else if (folderType.contains("Both")) "Both" else "Hidden",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                    }
                },
                onClick = onSelectFolderType
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            SettingsListTile(
                icon = Icons.Default.Sync,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "অটো-সিঙ্ক (স্বয়ংক্রিয়)" else "Automatic Background Sync",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা পরিবর্তনের সাথে সাথে তাৎক্ষণিক ক্লাউডে সংরক্ষণ করুন" else "Upload snapshots on every transaction & budget change",
                trailingContent = {
                    Switch(checked = autoSync, onCheckedChange = onToggleAutoSync)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            SettingsListTile(
                icon = Icons.Default.Wifi,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "কেবল ওয়াই-ফাই (Wi-Fi Only)" else "Wi-Fi Only Sync",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মোবাইল ডাটা সাশ্রয়ে কেবল ওয়াই-ফাইতে সিঙ্ক হবে" else "Restrict snapshot uploads to active Wi-Fi connection",
                trailingContent = {
                    Switch(checked = wifiOnly, onCheckedChange = onToggleWifiOnly)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            SettingsListTile(
                icon = Icons.Default.AttachFile,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "সংযুক্তি ও ভাউচার ছবি" else "Receipts & Voucher Attachments",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ভাউচার ও রসিদের ছবি স্ন্যাপশটে অন্তর্ভুক্ত করুন" else "Include image attachments in cloud backup snapshots",
                trailingContent = {
                    Switch(checked = uploadAttachments, onCheckedChange = onToggleUploadAttachments)
                }
            )
        }
    }
}

@Composable
private fun DriveSnapshotItem(
    backupFile: GoogleDriveBackupFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    languageMode: LanguageMode
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (backupFile.location == DriveBackupLocation.VISIBLE_APP_FOLDER) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("Visible Folder", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("Hidden AppData", fontSize = 9.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(backupFile.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                val dateStr = if (backupFile.modifiedTime.isNotBlank()) {
                    backupFile.modifiedTime.replace("T", " ").substringBefore(".")
                } else "Cloud Backup"
                val sizeKb = (backupFile.size / 1024).coerceAtLeast(1)
                Text("$dateStr • $sizeKb KB", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onRestore,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "রিস্টোর" else "Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = message, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
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
