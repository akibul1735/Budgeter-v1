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
import androidx.compose.material.icons.filled.Info
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
    val secondarySignedInAccount by viewModel.secondarySignedInGoogleAccount.collectAsStateWithLifecycle()
    val secondaryDriveBackups by viewModel.secondaryDriveBackups.collectAsStateWithLifecycle()

    val csvImportPreview by viewModel.csvImportPreview.collectAsStateWithLifecycle()
    val isImportingCsv by viewModel.isImportingCsv.collectAsStateWithLifecycle()
    val transactionsWithDetails by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var onlineSubTabIndex by remember { mutableIntStateOf(0) }
    var activeDriveAccountTab by remember { mutableIntStateOf(0) }
    var showFolderTypeDialogForAccount by remember { mutableStateOf<Int?>(null) }
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

    // Storage Permission check for Android <= 12
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

    // Primary Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.updateSignedInAccount(account)
            viewModel.setAccountLinked(account != null)
            if (account != null) viewModel.fetchDriveBackups(account)
        } catch (e: Exception) {
            e.printStackTrace()
            val current = GoogleDriveService.getSignedInAccount(context)
            viewModel.updateSignedInAccount(current)
            viewModel.setAccountLinked(current != null)
        }
    }

    // Secondary Google Sign-In Launcher (Dual Online Sync)
    val secondaryGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.updateSecondarySignedInAccount(account)
            if (account != null) viewModel.fetchSecondaryDriveBackups(account)
        } catch (e: Exception) {
            e.printStackTrace()
            val account = task.result
            viewModel.updateSecondarySignedInAccount(account)
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
            // OPTION 1: ONLINE (Dual Cloud Sync, Google Drive)
            // -------------------------------------------------------------
            if (selectedTabIndex == 0) {
                // Sub-Navigation: Drive 1 (Primary) | Drive 2 (Secondary) | Dual Sync
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple(0, if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ (প্রধান)" else "Drive 1 (Primary)", Icons.Default.CloudQueue),
                            Triple(1, if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ (সেকেন্ডারি)" else "Drive 2 (Secondary)", Icons.Default.CloudDone),
                            Triple(2, if (languageMode == LanguageMode.BANGLA) "ডুয়েল সিঙ্ক" else "Dual Sync", Icons.Default.Sync)
                        ).forEach { (index, title, icon) ->
                            val isSelected = onlineSubTabIndex == index
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onlineSubTabIndex = index
                                        activeDriveAccountTab = if (index == 1) 1 else 0
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) (if (index == 1) MaterialTheme.colorScheme.secondary else SolidPrimary) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // SUB-INTERFACE 1: PRIMARY ACCOUNT (DRIVE 1)
                // =========================================================
                if (onlineSubTabIndex == 0) {
                    // Account Profile & Connect Card
                    item {
                        val isLinked = signedInAccount != null
                        val userName = signedInAccount?.displayName ?: if (isLinked) "Google Account 1" else if (languageMode == LanguageMode.BANGLA) "প্রধান একাউন্ট যুক্ত নেই" else "No Primary Account Linked"
                        val userEmail = signedInAccount?.email ?: if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ সিঙ্ক সক্রিয় করতে সাইন-ইন করুন" else "Sign in to connect Primary Drive"

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
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
                                            color = if (isLinked) SolidPrimary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isLinked) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = SolidPrimary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ (প্রধান)" else "Drive 1 (Primary)",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SolidPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
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
                                    val lastSyncTime = if (config.primaryAccount.lastSyncTimestamp > 0L) {
                                        config.primaryAccount.lastSyncTimestamp
                                    } else config.lastSyncTimestamp
                                    val lastSyncText = if (lastSyncTime > 0L) {
                                        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lastSyncTime))
                                    } else if (languageMode == LanguageMode.BANGLA) "এখনো সিঙ্ক হয়নি" else "Not synced yet"

                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সর্বশেষ সিঙ্ক: $lastSyncText" else "Last synced: $lastSyncText",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.triggerQuickSync() },
                                        enabled = !isLoading && isLinked,
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
                                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ সিঙ্ক" else "Sync Drive 1 Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Drive 1 Settings & Preferences Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Drive 1 Folder Type selection
                                SettingsListTile(
                                    icon = Icons.Default.FolderOpen,
                                    iconTint = SolidPrimary,
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ স্টোরেজ ফোল্ডার" else "Drive 1 Storage Location",
                                    subtitle = config.primaryAccount.driveFolderType,
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (config.primaryAccount.driveFolderType.contains("Visible")) "Visible" else if (config.primaryAccount.driveFolderType.contains("Both")) "Both" else "Hidden",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidPrimary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                                        }
                                    },
                                    onClick = { showFolderTypeDialogForAccount = 1 }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 1 Auto-sync
                                SettingsListTile(
                                    icon = Icons.Default.Sync,
                                    iconTint = SolidPrimary,
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ অটো-সিঙ্ক" else "Drive 1 Automatic Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা পরিবর্তনের সাথে সাথে ড্রাইভ ১-এ তাৎক্ষণিক ব্যাকআপ নিন" else "Instantly upload data modifications to Drive 1 in background",
                                    trailingContent = {
                                        Switch(
                                            checked = config.primaryAccount.autoSync,
                                            onCheckedChange = { viewModel.setPrimaryAutoSync(it) }
                                        )
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 1 Wi-Fi Only
                                SettingsListTile(
                                    icon = Icons.Default.Wifi,
                                    iconTint = SolidPrimary,
                                    title = if (languageMode == LanguageMode.BANGLA) "কেবল ওয়াই-ফাই (Wi-Fi Only)" else "Drive 1 Wi-Fi Only Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "মোবাইল ডাটা সাশ্রয়ে কেবল ওয়াই-ফাইতে ড্রাইভ ১ আপলোড হবে" else "Restrict Drive 1 cloud snapshots to Wi-Fi connection",
                                    trailingContent = {
                                        Switch(
                                            checked = config.primaryAccount.wifiOnly,
                                            onCheckedChange = { viewModel.setPrimaryWifiOnly(it) }
                                        )
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 1 Attachments
                                SettingsListTile(
                                    icon = Icons.Default.AttachFile,
                                    iconTint = SolidPrimary,
                                    title = if (languageMode == LanguageMode.BANGLA) "সংযুক্তি আপলোড করুন" else "Drive 1 Receipt Attachments",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ভাউচার ও রসিদের ছবি ড্রাইভ ১ স্ন্যাপশটে সংরক্ষণ করুন" else "Include image vouchers and receipts in Drive 1 backups",
                                    trailingContent = {
                                        Switch(
                                            checked = config.primaryAccount.uploadAttachments,
                                            onCheckedChange = { viewModel.setPrimaryUploadAttachments(it) }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Drive 1 Snapshots Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ স্ন্যাপশটস (${driveBackups.size})" else "Drive 1 Snapshots (${driveBackups.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { signedInAccount?.let { viewModel.fetchDriveBackups(it) } }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp), tint = SolidPrimary)
                            }
                        }
                    }

                    // Drive 1 Snapshots List
                    if (signedInAccount == null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ স্ন্যাপশট দেখতে উপরের গুগল একাউন্টে সাইন-ইন করুন।" else "Sign in to Drive 1 above to view and restore cloud backups.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else if (driveBackups.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১-এ এখনো কোনো ব্যাকআপ নেই। 'ড্রাইভ ১ সিঙ্ক' বাটনে চাপুন।" else "No backups found in Drive 1. Tap 'Sync Drive 1 Now' to backup.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        items(driveBackups) { backupFile ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (backupFile.location == DriveBackupLocation.VISIBLE_APP_FOLDER) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = SolidPrimary.copy(alpha = 0.15f)) {
                                                    Text("Visible 'Budgeter'", fontSize = 9.sp, color = SolidPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            } else {
                                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)) {
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
                                            onClick = {
                                                activeDriveAccountTab = 0
                                                restoreConfirmDriveFile = backupFile
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "রিস্টোর" else "Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(
                                            onClick = {
                                                activeDriveAccountTab = 0
                                                deleteConfirmDriveFile = backupFile
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SolidExpense, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // SUB-INTERFACE 2: SECONDARY ACCOUNT (DRIVE 2)
                // =========================================================
                if (onlineSubTabIndex == 1) {
                    // Secondary Account Profile & Connect Card
                    item {
                        val isSecondaryLinked = secondarySignedInAccount != null
                        val secUserName = secondarySignedInAccount?.displayName ?: if (isSecondaryLinked) "Secondary Account" else if (languageMode == LanguageMode.BANGLA) "দ্বিতীয় একাউন্ট যুক্ত নেই" else "No Secondary Account Linked"
                        val secUserEmail = secondarySignedInAccount?.email ?: if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ সিঙ্ক সক্রিয় করতে দ্বিতীয় একাউন্ট যুক্ত করুন" else "Connect a second Google account for independent secondary sync"

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
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
                                            color = if (isSecondaryLinked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isSecondaryLinked) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(secUserName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ (সেকেন্ডারি)" else "Drive 2 (Secondary)",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(secUserEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                        }
                                    }

                                    if (isSecondaryLinked) {
                                        Switch(
                                            checked = true,
                                            onCheckedChange = {
                                                viewModel.updateSecondarySignedInAccount(null)
                                            }
                                        )
                                    } else {
                                        Button(
                                            onClick = {
                                                val client = GoogleDriveService.getGoogleSignInClient(context)
                                                secondaryGoogleSignInLauncher.launch(client.signInIntent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (languageMode == LanguageMode.BANGLA) "যুক্ত করুন" else "Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (isSecondaryLinked) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val lastSyncText = if (config.secondaryAccount.lastSyncTimestamp > 0L) {
                                            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(config.secondaryAccount.lastSyncTimestamp))
                                        } else if (languageMode == LanguageMode.BANGLA) "এখনো সিঙ্ক হয়নি" else "Not synced yet"

                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "সর্বশেষ সিঙ্ক: $lastSyncText" else "Last synced: $lastSyncText",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                secondarySignedInAccount?.let { viewModel.backupToSecondaryGoogleDrive(it) }
                                            },
                                            enabled = !isLoading,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ সিঙ্ক" else "Sync Drive 2 Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Drive 2 Settings & Preferences Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Drive 2 Folder Type selection
                                SettingsListTile(
                                    icon = Icons.Default.FolderOpen,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ স্টোরেজ ফোল্ডার" else "Drive 2 Storage Location",
                                    subtitle = config.secondaryAccount.driveFolderType,
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (config.secondaryAccount.driveFolderType.contains("Visible")) "Visible" else if (config.secondaryAccount.driveFolderType.contains("Both")) "Both" else "Hidden",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                                        }
                                    },
                                    onClick = { showFolderTypeDialogForAccount = 2 }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 2 Auto-sync
                                SettingsListTile(
                                    icon = Icons.Default.Sync,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ অটো-সিঙ্ক" else "Drive 2 Automatic Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা পরিবর্তনের সাথে সাথে ড্রাইভ ২-এ তাৎক্ষণিক ব্যাকআপ নিন" else "Instantly upload data modifications to Drive 2 in background",
                                    trailingContent = {
                                        Switch(
                                            checked = config.secondaryAccount.autoSync,
                                            onCheckedChange = { viewModel.setSecondaryAutoSync(it) }
                                        )
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 2 Wi-Fi Only
                                SettingsListTile(
                                    icon = Icons.Default.Wifi,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    title = if (languageMode == LanguageMode.BANGLA) "কেবল ওয়াই-ফাই (Wi-Fi Only)" else "Drive 2 Wi-Fi Only Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "মোবাইল ডাটা সাশ্রয়ে কেবল ওয়াই-ফাইতে ড্রাইভ ২ আপলোড হবে" else "Restrict Drive 2 cloud snapshots to Wi-Fi connection",
                                    trailingContent = {
                                        Switch(
                                            checked = config.secondaryAccount.wifiOnly,
                                            onCheckedChange = { viewModel.setSecondaryWifiOnly(it) }
                                        )
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Drive 2 Attachments
                                SettingsListTile(
                                    icon = Icons.Default.AttachFile,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    title = if (languageMode == LanguageMode.BANGLA) "সংযুক্তি আপলোড করুন" else "Drive 2 Receipt Attachments",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "ভাউচার ও রসিদের ছবি ড্রাইভ ২ স্ন্যাপশটে সংরক্ষণ করুন" else "Include image vouchers and receipts in Drive 2 backups",
                                    trailingContent = {
                                        Switch(
                                            checked = config.secondaryAccount.uploadAttachments,
                                            onCheckedChange = { viewModel.setSecondaryUploadAttachments(it) }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Drive 2 Snapshots Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ স্ন্যাপশটস (${secondaryDriveBackups.size})" else "Drive 2 Snapshots (${secondaryDriveBackups.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { secondarySignedInAccount?.let { viewModel.fetchSecondaryDriveBackups(it) } }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    // Drive 2 Snapshots List
                    if (secondarySignedInAccount == null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ স্ন্যাপশট দেখতে উপরের 'যুক্ত করুন' বাটনে চাপ দিন।" else "Connect a secondary Google account above to view and restore backups.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else if (secondaryDriveBackups.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২-এ এখনো কোনো ব্যাকআপ নেই। 'ড্রাইভ ২ সিঙ্ক' বাটনে চাপুন।" else "No backups found in Drive 2. Tap 'Sync Drive 2 Now' to backup.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        items(secondaryDriveBackups) { backupFile ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (backupFile.location == DriveBackupLocation.VISIBLE_APP_FOLDER) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)) {
                                                    Text("Visible 'Budgeter'", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            } else {
                                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)) {
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
                                            onClick = {
                                                activeDriveAccountTab = 1
                                                restoreConfirmDriveFile = backupFile
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "রিস্টোর" else "Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(
                                            onClick = {
                                                activeDriveAccountTab = 1
                                                deleteConfirmDriveFile = backupFile
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SolidExpense, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================
                // SUB-INTERFACE 3: DUAL SYNC (SIMULTANEOUS REDUNDANCY)
                // =========================================================
                if (onlineSubTabIndex == 2) {
                    // Dual Sync Hero Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SolidIncome.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SolidIncome.copy(alpha = 0.2f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Sync, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "একসাথে ২টি একাউন্টে সিঙ্ক (ডুয়েল সিঙ্ক)" else "Simultaneous Dual Cloud Sync",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SolidIncome
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "এক ক্লিকে একই সাথে ২টি গুগল ড্রাইভে পূর্ণাঙ্গ ব্যাকআপ নিশ্চিত করুন।"
                                            else
                                                "Backup simultaneously across both Google accounts for redundant cloud safety.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (signedInAccount == null && secondarySignedInAccount == null) {
                                            val client = GoogleDriveService.getGoogleSignInClient(context)
                                            googleSignInLauncher.launch(client.signInIntent)
                                        } else {
                                            viewModel.backupToBothDrives(signedInAccount, secondarySignedInAccount)
                                        }
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 11.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "একসাথে ২টি ড্রাইভে ব্যাকআপ নিন" else "Sync Both Cloud Accounts Simultaneously",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Dual Sync Preferences Card
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
                                    iconTint = SolidIncome,
                                    title = if (languageMode == LanguageMode.BANGLA) "ডুয়েল ব্যাকগ্রাউন্ড অটো-সিঙ্ক" else "Simultaneous Dual Auto-Sync",
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "যেকোনো লেনদেন পরিবর্তনের সাথে সাথে দুটি ড্রাইভেই ব্যাকগ্রাউন্ডে আপলোড করুন" else "Automatically keep both primary & secondary cloud copies synchronized",
                                    trailingContent = {
                                        Switch(
                                            checked = config.isDualSyncEnabled,
                                            onCheckedChange = { viewModel.setDualSyncEnabled(it) }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Account Status Comparison Matrix Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "একাউন্ট রেডান্ড্যান্সি স্ট্যাটাস" else "Cloud Redundancy Status Matrix",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Row for Drive 1 status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ (প্রধান)" else "Drive 1 (Primary)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SolidPrimary)
                                        Text(signedInAccount?.email ?: (if (languageMode == LanguageMode.BANGLA) "যুক্ত নেই" else "Not connected"), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("Folder: ${config.primaryAccount.driveFolderType}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (signedInAccount != null) SolidIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (signedInAccount != null) "${driveBackups.size} Backups" else "Offline",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (signedInAccount != null) SolidIncome else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Row for Drive 2 status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ (সেকেন্ডারি)" else "Drive 2 (Secondary)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                        Text(secondarySignedInAccount?.email ?: (if (languageMode == LanguageMode.BANGLA) "যুক্ত নেই" else "Not connected"), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("Folder: ${config.secondaryAccount.driveFolderType}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (secondarySignedInAccount != null) SolidIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (secondarySignedInAccount != null) "${secondaryDriveBackups.size} Backups" else "Offline",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (secondarySignedInAccount != null) SolidIncome else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // OPTION 2: LOCAL (Storage Backup, Custom Directory)
            // -------------------------------------------------------------
            if (selectedTabIndex == 1) {
                // Storage Permission Status (if needed for older Android)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && !hasStoragePermission) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SolidExpense.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "স্টোরেজ পারমিশন প্রয়োজন" else "Storage Permission Needed",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SolidExpense
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "লোকাল ফাইলে ব্যাকআপ সংরক্ষণ করতে পারমিশন দিন" else "Grant storage permission to write local snapshots",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        storagePermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "অনুমতি দিন" else "Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

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
                                            text = BackupManager.formatDirectoryDisplayName(config.localBackupDirectory),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { dirPickerLauncher.launch(null) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "SAF ফোল্ডার" else "Custom SAF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // Folder Type Selection Dialog (Primary or Secondary Account)
    if (showFolderTypeDialogForAccount != null) {
        val targetAcc = showFolderTypeDialogForAccount!!
        val currentType = if (targetAcc == 1) config.primaryAccount.driveFolderType else config.secondaryAccount.driveFolderType
        val folderOptions = listOf(
            "Visible 'Budgeter' Folder" to if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভের মূল ফোল্ডারে দৃশ্যমান 'Budgeter' ফোল্ডারে সেভ হবে" else "Directly visible and browsable in Google Drive root folder",
            "Hidden App Data" to if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ অ্যাপডাটাতে লুকায়িত ও সুরক্ষিতভাবে সেভ হবে" else "Encrypted and isolated in Google Drive AppData folder",
            "Both (Visible & Hidden)" to if (languageMode == LanguageMode.BANGLA) "একসাথে দৃশ্যমান এবং লুকায়িত উভয় স্থানে কপি রাখা হবে" else "Saves duplicate snapshot copies in both folders simultaneously"
        )
        AlertDialog(
            onDismissRequest = { showFolderTypeDialogForAccount = null },
            title = {
                Text(
                    text = if (targetAcc == 1) {
                        if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ স্টোরেজ ফোল্ডার" else "Drive 1 Storage Location"
                    } else {
                        if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ স্টোরেজ ফোল্ডার" else "Drive 2 Storage Location"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folderOptions.forEach { (option, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (targetAcc == 1) {
                                        viewModel.setPrimaryFolderType(option)
                                    } else {
                                        viewModel.setSecondaryFolderType(option)
                                    }
                                    showFolderTypeDialogForAccount = null
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(option, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderTypeDialogForAccount = null }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                }
            }
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
        val currentBackupsList = if (activeDriveAccountTab == 0) driveBackups else secondaryDriveBackups
        val currentAccount = if (activeDriveAccountTab == 0) signedInAccount else secondarySignedInAccount

        AlertDialog(
            onDismissRequest = { showDriveRestoreDialog = false },
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Google Drive Snapshots", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(
                            onClick = {
                                if (activeDriveAccountTab == 0) {
                                    signedInAccount?.let { viewModel.fetchDriveBackups(it) }
                                } else {
                                    secondarySignedInAccount?.let { viewModel.fetchSecondaryDriveBackups(it) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account 1 vs Account 2 Selector Tab Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeDriveAccountTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (activeDriveAccountTab == 0) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeDriveAccountTab = 0
                                    signedInAccount?.let { viewModel.fetchDriveBackups(it) }
                                }
                        ) {
                            Text(
                                text = "Drive 1 (${driveBackups.size})",
                                fontSize = 11.sp,
                                fontWeight = if (activeDriveAccountTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeDriveAccountTab == 0) SolidPrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeDriveAccountTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (activeDriveAccountTab == 1) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeDriveAccountTab = 1
                                    secondarySignedInAccount?.let { viewModel.fetchSecondaryDriveBackups(it) }
                                }
                        ) {
                            Text(
                                text = "Drive 2 (${secondaryDriveBackups.size})",
                                fontSize = 11.sp,
                                fontWeight = if (activeDriveAccountTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeDriveAccountTab == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (currentAccount == null) {
                        Text(
                            text = if (activeDriveAccountTab == 0) "Primary Google account is not signed in." else "Secondary Google account is not connected. Connect a second account to enable dual sync snapshots.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else if (currentBackupsList.isEmpty()) {
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
                            items(currentBackupsList) { backupFile ->
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
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (activeDriveAccountTab == 0) SolidPrimary else MaterialTheme.colorScheme.secondary
                                                ),
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

    // 5. Drive Delete Confirmation Dialog
    deleteConfirmDriveFile?.let { backupFile ->
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ব্যাকআপ ডিলিট করবেন?" else "Delete Drive Backup?",
            message = if (languageMode == LanguageMode.BANGLA)
                "আপনি কি নিশ্চিত যে '${backupFile.name}' ব্যাকআপ ফাইলটি গুগল ড্রাইভ স্টোরেজ থেকে চিরতরে মুছে ফেলতে চান?"
            else
                "Are you sure you want to permanently delete '${backupFile.name}' from your Google Drive storage?",
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

    // 8. CSV Import Preview Dialog
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
