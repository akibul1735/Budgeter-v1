package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.CreateDocumentWithInitialUri
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.OpenDocumentWithInitialUri
import com.example.util.StorageLocationHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.components.AppPermissionType
import com.example.ui.components.PermissionRationaleDialog
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import com.example.util.DropboxService
import com.example.util.GoogleDriveBackupFile
import com.example.util.GoogleDriveService
import com.example.ui.dialogs.DetectedBackupsListDialog
import com.example.ui.dialogs.RestoreOrMergeOptionsDialog
import com.example.ui.viewmodel.DetectedBackupInfo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import com.example.data.remote.OnlineIconResult
import com.example.data.remote.OnlineImageResult
import com.example.data.remote.OnlineIconSearchService
import com.example.ui.components.IconCropEditorModal

enum class DataManagementTab(val titleEn: String, val titleBn: String, val icon: ImageVector) {
    ONLINE("Online Sync", "অনলাইন সিঙ্ক", Icons.Default.CloudSync),
    LOCAL("Local Storage", "লোকাল স্টোরেজ", Icons.Default.SdStorage),
    EXPORT_IMPORT("Export Import", "এক্সপোর্ট / ইমপোর্ট", Icons.Default.SwapVert)
}

enum class OnlineSyncTab(val titleEn: String, val titleBn: String, val icon: ImageVector) {
    DRIVE_1("Drive 1", "ড্রাইভ ১", Icons.Default.Cloud),
    DRIVE_2("Drive 2", "ড্রাইভ ২", Icons.Default.CloudDone),
    COMMON("Common", "কমন", Icons.Default.Tune),
    ICONS("Icons", "আইকন", Icons.Default.Category),
    IMAGES("Images", "ছবি", Icons.Default.AddPhotoAlternate)
}

val SUPPORTED_CLOUD_PROVIDERS = listOf(
    "Google Drive",
    "Dropbox"
)

fun getProviderIcon(provider: String): ImageVector {
    return when {
        provider.contains("Dropbox", ignoreCase = true) -> Icons.Default.Folder
        else -> Icons.Default.Cloud
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSyncSettingsScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    backupUiState: BackupUiState,
    onNavigateToReset: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
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

    var isCloudTreeExpanded by remember { mutableStateOf(true) }
    var isLocalTreeExpanded by remember { mutableStateOf(true) }
    var isMoreTreeExpanded by remember { mutableStateOf(true) }

    var isDrive1AdvancedExpanded by remember { mutableStateOf(false) }
    var isDrive2TreeExpanded by remember { mutableStateOf(false) }
    var isLocalStorageExpanded by remember { mutableStateOf(false) }
    var isExportImportExpanded by remember { mutableStateOf(true) }

    var activeDriveAccountTab by remember { mutableIntStateOf(0) }
    var selectedOnlineSyncTab by remember { mutableStateOf(OnlineSyncTab.DRIVE_1) }
    var showFolderTypeDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var showProviderDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var showAccountEditDialogForAccount by remember { mutableStateOf<Int?>(null) }
    var localBackups by remember { mutableStateOf<List<File>>(emptyList()) }

    // Online Icons & Images tab states
    var iconSearchQuery by remember { mutableStateOf("") }
    var iconSearchResults by remember { mutableStateOf<List<OnlineIconResult>>(emptyList()) }
    var isSearchingIcons by remember { mutableStateOf(false) }
    var iconSearchError by remember { mutableStateOf<String?>(null) }
    var iconSearchPage by remember { mutableIntStateOf(1) }
    var hasMoreIcons by remember { mutableStateOf(false) }
    var isDownloadingIconUrl by remember { mutableStateOf<String?>(null) }

    var imageSearchQuery by remember { mutableStateOf("") }
    var imageSearchResults by remember { mutableStateOf<List<OnlineImageResult>>(emptyList()) }
    var isSearchingImages by remember { mutableStateOf(false) }
    var imageSearchError by remember { mutableStateOf<String?>(null) }
    var imageSearchPage by remember { mutableIntStateOf(1) }
    var hasMoreImages by remember { mutableStateOf(false) }

    var cropEditorImageUrl by remember { mutableStateOf<String?>(null) }
    var cropEditorInitialName by remember { mutableStateOf("Custom Image") }
    var showCropEditor by remember { mutableStateOf(false) }

    var customIconsList by remember { mutableStateOf<List<File>>(emptyList()) }
    var iconCacheStats by remember { mutableStateOf(IconHelper.IconCacheStats()) }
    var isCleaningCache by remember { mutableStateOf(false) }

    fun refreshCustomIconsAndStats() {
        customIconsList = IconHelper.getCustomIcons(context)
        coroutineScope.launch {
            iconCacheStats = viewModel.getIconCacheStats()
        }
    }

    var isDrive1FilesVisible by remember { mutableStateOf(false) }
    var isDrive2FilesVisible by remember { mutableStateOf(false) }
    var isLocalSnapshotsVisible by remember { mutableStateOf(false) }

    var pendingSecurityAction by remember { mutableStateOf<PendingSecurityAction?>(null) }

    fun executeWithAuth(
        title: String,
        message: String,
        confirmButtonText: String = if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করুন" else "Confirm",
        isDestructive: Boolean = false,
        action: () -> Unit
    ) {
        if (securityConfig.hasPin || securityConfig.isBiometricEnabled) {
            pendingSecurityAction = PendingSecurityAction(
                title = title,
                message = message,
                confirmButtonText = confirmButtonText,
                isDestructive = isDestructive,
                onExecute = action
            )
        } else {
            action()
        }
    }
    var showFolderPickerDialog by remember { mutableStateOf(false) }
    var showCsvExportDialog by remember { mutableStateOf(false) }
    var pendingCsvExportConfig by remember { mutableStateOf<CsvExportConfig?>(null) }
    var restoreConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var deleteConfirmDriveFile by remember { mutableStateOf<GoogleDriveBackupFile?>(null) }
    var restoreConfirmLocalFile by remember { mutableStateOf<File?>(null) }
    var deleteConfirmLocalFile by remember { mutableStateOf<File?>(null) }
    val detectedBackups by viewModel.detectedBackups.collectAsStateWithLifecycle()
    var showScanBackupsDialog by remember { mutableStateOf(false) }
    var selectedBackupForOptionsDialog by remember { mutableStateOf<DetectedBackupInfo?>(null) }

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

    // Gallery picker launcher (opens gallery view first, with Browse option to access file manager)
    val onlineGalleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cropEditorImageUrl = it.toString()
            cropEditorInitialName = "Custom Gallery Image"
            showCropEditor = true
        }
    }

    // Direct File Manager launcher with custom file manager app support
    val onlineDirectFileManagerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            cropEditorImageUrl = uri.toString()
            cropEditorInitialName = "Custom File Image"
            showCropEditor = true
        }
    }

    // 1. Gallery option: Directly opens the gallery view (menu provides Browse option)
    val openOnlineGallery = {
        onlineGalleryPickerLauncher.launch("image/*")
    }

    // 2. Direct File Manager launcher: Directly opens the file manager / storage provider without permissions
    val openOnlineDirectFileManager = {
        val openDocIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "image/*",
                    "image/png",
                    "image/jpeg",
                    "image/jpg",
                    "image/webp",
                    "image/svg+xml",
                    "image/gif",
                    "image/bmp"
                )
            )
        }
        try {
            onlineDirectFileManagerLauncher.launch(openDocIntent)
        } catch (_: Exception) {
            onlineGalleryPickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(iconSearchQuery, selectedOnlineSyncTab) {
        if (selectedOnlineSyncTab == OnlineSyncTab.ICONS) {
            refreshCustomIconsAndStats()
            if (iconSearchQuery.trim().length >= 2) {
                isSearchingIcons = true
                iconSearchError = null
                iconSearchPage = 1
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchIcons(iconSearchQuery.trim(), page = 1)
                    }
                    iconSearchResults = res
                    hasMoreIcons = res.isNotEmpty()
                } catch (e: Exception) {
                    iconSearchError = e.message ?: "Failed to search icons"
                } finally {
                    isSearchingIcons = false
                }
            } else if (iconSearchQuery.isBlank()) {
                isSearchingIcons = true
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchIcons("bank payment shopping tech food travel", page = 1)
                    }
                    iconSearchResults = res
                    hasMoreIcons = true
                } catch (_: Exception) {
                } finally {
                    isSearchingIcons = false
                }
            }
        }
    }

    LaunchedEffect(imageSearchQuery, selectedOnlineSyncTab) {
        if (selectedOnlineSyncTab == OnlineSyncTab.IMAGES) {
            refreshCustomIconsAndStats()
            if (imageSearchQuery.trim().length >= 2) {
                isSearchingImages = true
                imageSearchError = null
                imageSearchPage = 1
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchImages(imageSearchQuery.trim(), page = 1)
                    }
                    imageSearchResults = res
                    hasMoreImages = res.isNotEmpty()
                } catch (e: Exception) {
                    imageSearchError = e.message ?: "Failed to search images"
                } finally {
                    isSearchingImages = false
                }
            } else if (imageSearchQuery.isBlank()) {
                isSearchingImages = true
                try {
                    val res = withContext(Dispatchers.IO) {
                        OnlineIconSearchService.searchImages("finance grocery restaurant office", page = 1)
                    }
                    imageSearchResults = res
                    hasMoreImages = true
                } catch (_: Exception) {
                } finally {
                    isSearchingImages = false
                }
            }
        }
    }

    LaunchedEffect(config.localBackupDirectory, config.primaryAccount.provider, config.primaryAccount.accessToken, config.secondaryAccount.provider, config.secondaryAccount.accessToken) {
        refreshLocalBackups()
        if (config.primaryAccount.provider.contains("Google", ignoreCase = true) && config.primaryAccount.isLinked && signedInAccount == null) {
            val existingAccount = GoogleDriveService.getSignedInAccount(context)
            if (existingAccount != null && (config.primaryAccount.email.isEmpty() || existingAccount.email.equals(config.primaryAccount.email, ignoreCase = true))) {
                viewModel.updateSignedInAccount(existingAccount)
            }
        }
        viewModel.fetchCloudBackups(1)
        viewModel.fetchCloudBackups(2)
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
                    displayName = account.displayName ?: (account.email ?: ""),
                    isLinked = true,
                    photoUrl = account.photoUrl?.toString() ?: ""
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
                    displayName = account.displayName ?: (account.email ?: ""),
                    isLinked = true,
                    photoUrl = account.photoUrl?.toString() ?: ""
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
        contract = CreateDocumentWithInitialUri("application/json") {
            StorageLocationHelper.getInitialStorageUri(context, config.localBackupDirectory)
        }
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackupToUri(it) }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithInitialUri {
            StorageLocationHelper.getInitialStorageUri(context, config.localBackupDirectory)
        }
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreBackupFromUri(it) }
    }

    val csvFileLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithInitialUri {
            StorageLocationHelper.getInitialStorageUri(context, config.localBackupDirectory)
        }
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromCsv(it) }
    }

    val qifFileLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentWithInitialUri {
            StorageLocationHelper.getInitialStorageUri(context, config.localBackupDirectory)
        }
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromQif(it) }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = CreateDocumentWithInitialUri("text/csv") {
            StorageLocationHelper.getInitialStorageUri(context, config.localBackupDirectory)
        }
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
                        text = if (languageMode == LanguageMode.BANGLA) "অনলাইন সিঙ্ক ও ব্যাকআপ" else "Backup & Cloud Sync",
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

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        var selectedManagementTab by remember { mutableStateOf(DataManagementTab.ONLINE) }

        TabRow(
            selectedTabIndex = selectedManagementTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            DataManagementTab.values().forEach { tab ->
                val isSelected = selectedManagementTab == tab
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedManagementTab = tab
                        viewModel.clearBackupUiState()
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(6.dp))
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
            // Quick Action: Scan for All Previous Backups across all drives and storage (Available in Online Sync and Local Storage)
            if (selectedManagementTab == DataManagementTab.ONLINE || selectedManagementTab == DataManagementTab.LOCAL) {
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ব্যাকআপ খুঁজুন ও স্ক্যান করুন" else "Scan & Find Previous Backups",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "ড্রাইভ ১, ড্রাইভ ২ এবং ডিভাইস স্টোরেজের সকল ব্যাকআপ খুঁজুন ও রিস্টোর/মার্জ করুন"
                                        else
                                            "Detect snapshots from Cloud Drives & Local Storage to Merge/Restore",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.scanForPreviousBackups()
                                    showScanBackupsDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "স্ক্যান" else "Scan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // SECTION 1: ONLINE SYNC
            // =================================================================
            if (selectedManagementTab == DataManagementTab.ONLINE) {
                // Tab-specific Status Feedback
                item {
                    TabStatusFeedbackCard(
                        state = backupUiState,
                        onDismiss = { viewModel.clearBackupUiState() }
                    )
                }

                // Tabs for Online Sync: Drive 1, Drive 2, Common, Icons, Images
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedOnlineSyncTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        OnlineSyncTab.values().forEach { subTab ->
                            val isSelected = selectedOnlineSyncTab == subTab
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    selectedOnlineSyncTab = subTab
                                    viewModel.clearBackupUiState()
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = subTab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) subTab.titleBn else subTab.titleEn,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // TAB 1: DRIVE 1
                // -------------------------------------------------------------
                if (selectedOnlineSyncTab == OnlineSyncTab.DRIVE_1) {
                    val primary = config.primaryAccount
                    val isGoogleDrive = primary.provider.equals("Google Drive", ignoreCase = true)
                    val isPrimaryLinked = if (isGoogleDrive) (signedInAccount != null || (primary.isLinked && primary.email.isNotBlank())) else (primary.isLinked && (primary.email.isNotBlank() || primary.accessToken.isNotBlank()))
                    val displayEmail = if (isGoogleDrive) (signedInAccount?.email ?: primary.email) else primary.email
                    val displayName = if (isGoogleDrive) (signedInAccount?.displayName ?: primary.displayName.ifEmpty { displayEmail }) else primary.displayName
                    val displayPhotoUrl = primary.photoUrl.ifEmpty { signedInAccount?.photoUrl?.toString() ?: "" }

                    item {
                        DriveAccountConnectionCard(
                            driveIndex = 1,
                            provider = primary.provider,
                            isLinked = isPrimaryLinked,
                            email = displayEmail,
                            displayName = displayName,
                            photoUrl = displayPhotoUrl,
                            serverUrl = primary.serverUrl,
                            lastSyncTimestamp = if (primary.lastSyncTimestamp > 0L) primary.lastSyncTimestamp else config.lastSyncTimestamp,
                            isLoading = isLoading,
                            onConnect = {
                                if (isGoogleDrive) {
                                    GoogleDriveService.launchGoogleSignIn(context, googleSignInLauncher)
                                } else if (primary.provider.contains("Dropbox", ignoreCase = true)) {
                                    viewModel.startDropboxAuth(context, primary.appKey.ifEmpty { DropboxService.DEFAULT_APP_KEY }, 1)
                                } else {
                                    showAccountEditDialogForAccount = 1
                                }
                            },
                            onDisconnect = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ আনলিঙ্ক করবেন?" else "Unlink Drive 1 Account?",
                                    message = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ একাউন্ট সংযোগ বিচ্ছিন্ন করতে বায়োমেট্রিক বা পাসকোড যাচাই করুন।" else "Authenticate with Biometric or PIN to unlink Drive 1 account.",
                                    confirmButtonText = if (languageMode == LanguageMode.BANGLA) "আনলিঙ্ক" else "Unlink",
                                    isDestructive = true
                                ) {
                                    if (isGoogleDrive) {
                                        val client = GoogleDriveService.getGoogleSignInClient(context)
                                        client.signOut().addOnCompleteListener {
                                            viewModel.updateSignedInAccount(null)
                                            viewModel.setPrimaryAccount("", "", false)
                                        }
                                    } else if (primary.provider.contains("Dropbox", ignoreCase = true)) {
                                        viewModel.disconnectDropbox(1)
                                    } else {
                                        viewModel.setPrimaryAccount("", "", false, "", "")
                                    }
                                }
                            },
                            onEditDetails = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "একাউন্ট পরিবর্তন" else "Change Account Details",
                                    message = if (languageMode == LanguageMode.BANGLA) "একাউন্ট বিবরণ পরিবর্তন করতে বায়োমেট্রিক বা পাসকোড দিন।" else "Authenticate with Biometric or PIN to change account details."
                                ) {
                                    showAccountEditDialogForAccount = 1
                                }
                            },
                            onSyncNow = {
                                viewModel.backupToCloudProvider(1)
                            },
                            languageMode = languageMode
                        )
                    }

                    item {
                        DriveProviderCard(
                            driveIndex = 1,
                            driveLabel = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ ক্লাউড সার্ভিস" else "Drive 1 Cloud Service",
                            provider = primary.provider,
                            onChangeProvider = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড প্রোভাইডার পরিবর্তন" else "Change Cloud Provider",
                                    message = if (languageMode == LanguageMode.BANGLA) "ক্লাউড প্রোভাইডার পরিবর্তন করতে বায়োমেট্রিক বা পাসকোড দিন।" else "Authenticate with Biometric or PIN to change cloud provider."
                                ) {
                                    showProviderDialogForAccount = 1
                                }
                            },
                            languageMode = languageMode
                        )
                    }

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

                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDrive1FilesVisible) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            ),
                            onClick = { isDrive1FilesVisible = !isDrive1FilesVisible },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ১ অনলাইন সিঙ্ক ফাইল (${driveBackups.size})" else "Drive 1 Online Sync Files (${driveBackups.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (isDrive1FilesVisible) {
                                                if (languageMode == LanguageMode.BANGLA) "ফাইল তালিকা লুকাতে ট্যাপ করুন" else "Tap to hide sync files"
                                            } else {
                                                if (languageMode == LanguageMode.BANGLA) "সিঙ্ক ফাইলগুলো দেখতে ট্যাপ করুন" else "Tap to view sync files"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.fetchCloudBackups(1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isDrive1FilesVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isDrive1FilesVisible) {
                        if (!isPrimaryLinked) {
                            item {
                                EmptyStateCard(
                                    message = if (languageMode == LanguageMode.BANGLA)
                                        "ড্রাইভ ১-এ অনলাইন সিঙ্ক সক্রিয় করতে উপরের '${primary.provider}' একাউন্ট যুক্ত করুন।"
                                    else
                                        "Connect your ${primary.provider} account above to manage Drive 1 online sync."
                                )
                            }
                        } else if (driveBackups.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = if (languageMode == LanguageMode.BANGLA)
                                        "ড্রাইভ ১-এ এখনো কোনো সিঙ্ক ফাইল নেই। 'সিঙ্ক করুন' বাটনে ট্যাপ করুন।"
                                    else
                                        "No online sync files found in Drive 1. Tap 'Sync Drive 1 Now' to sync."
                                )
                            }
                        } else {
                            items(driveBackups, key = { it.id }) { backupFile ->
                                DriveSnapshotItem(
                                    backupFile = backupFile,
                                    onRestore = {
                                        val parsedTime = DateUtils.parseIsoTimestamp(backupFile.modifiedTime)
                                        val info = DetectedBackupInfo(
                                            fileId = backupFile.id,
                                            fileName = backupFile.name,
                                            sourceProvider = "Google Drive (Drive 1)",
                                            timestamp = parsedTime,
                                            deviceName = backupFile.deviceName,
                                            installationId = backupFile.installationId,
                                            accountsCount = backupFile.accountsCount,
                                            transactionsCount = backupFile.transactionsCount,
                                            driveIndex = 1,
                                            rawBackupFile = backupFile
                                        )
                                        selectedBackupForOptionsDialog = info
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
                }

                // -------------------------------------------------------------
                // TAB 2: DRIVE 2
                // -------------------------------------------------------------
                if (selectedOnlineSyncTab == OnlineSyncTab.DRIVE_2) {
                    val secondary = config.secondaryAccount
                    val isSecondaryGoogleDrive = secondary.provider.equals("Google Drive", ignoreCase = true)
                    val isSecondaryLinked = if (isSecondaryGoogleDrive) (secondarySignedInAccount != null || (secondary.isLinked && secondary.email.isNotBlank())) else (secondary.isLinked && (secondary.email.isNotBlank() || secondary.accessToken.isNotBlank()))
                    val secondaryDisplayEmail = if (isSecondaryGoogleDrive) (secondarySignedInAccount?.email ?: secondary.email) else secondary.email
                    val secondaryDisplayName = if (isSecondaryGoogleDrive) (secondarySignedInAccount?.displayName ?: secondary.displayName.ifEmpty { secondaryDisplayEmail }) else secondary.displayName
                    val secondaryDisplayPhotoUrl = secondary.photoUrl.ifEmpty { secondarySignedInAccount?.photoUrl?.toString() ?: "" }

                    item {
                        DriveAccountConnectionCard(
                            driveIndex = 2,
                            provider = secondary.provider,
                            isLinked = isSecondaryLinked,
                            email = secondaryDisplayEmail,
                            displayName = secondaryDisplayName,
                            photoUrl = secondaryDisplayPhotoUrl,
                            serverUrl = secondary.serverUrl,
                            lastSyncTimestamp = secondary.lastSyncTimestamp,
                            isLoading = isLoading,
                            onConnect = {
                                if (isSecondaryGoogleDrive) {
                                    GoogleDriveService.launchGoogleSignIn(context, secondaryGoogleSignInLauncher)
                                } else if (secondary.provider.contains("Dropbox", ignoreCase = true)) {
                                    viewModel.startDropboxAuth(context, secondary.appKey.ifEmpty { DropboxService.DEFAULT_APP_KEY }, 2)
                                } else {
                                    showAccountEditDialogForAccount = 2
                                }
                            },
                            onDisconnect = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ আনলিঙ্ক করবেন?" else "Unlink Drive 2 Account?",
                                    message = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ একাউন্ট সংযোগ বিচ্ছিন্ন করতে বায়োমেট্রিক বা পাসকোড যাচাই করুন।" else "Authenticate with Biometric or PIN to unlink Drive 2 account.",
                                    confirmButtonText = if (languageMode == LanguageMode.BANGLA) "আনলিঙ্ক" else "Unlink",
                                    isDestructive = true
                                ) {
                                    if (isSecondaryGoogleDrive) {
                                        val client = GoogleDriveService.getGoogleSignInClient(context)
                                        client.signOut().addOnCompleteListener {
                                            viewModel.updateSecondarySignedInAccount(null)
                                            viewModel.setSecondaryAccount("", "", false)
                                        }
                                    } else if (secondary.provider.contains("Dropbox", ignoreCase = true)) {
                                        viewModel.disconnectDropbox(2)
                                    } else {
                                        viewModel.setSecondaryAccount("", "", false, "", "")
                                    }
                                }
                            },
                            onEditDetails = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "একাউন্ট পরিবর্তন" else "Change Account Details",
                                    message = if (languageMode == LanguageMode.BANGLA) "একাউন্ট বিবরণ পরিবর্তন করতে বায়োমেট্রিক বা পাসকোড দিন।" else "Authenticate with Biometric or PIN to change account details."
                                ) {
                                    showAccountEditDialogForAccount = 2
                                }
                            },
                            onSyncNow = {
                                viewModel.backupToCloudProvider(2)
                            },
                            languageMode = languageMode
                        )
                    }

                    item {
                        DriveProviderCard(
                            driveIndex = 2,
                            driveLabel = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ ক্লাউড সার্ভিস" else "Drive 2 Cloud Service",
                            provider = secondary.provider,
                            onChangeProvider = {
                                executeWithAuth(
                                    title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড প্রোভাইডার পরিবর্তন" else "Change Cloud Provider",
                                    message = if (languageMode == LanguageMode.BANGLA) "ক্লাউড প্রোভাইডার পরিবর্তন করতে বায়োমেট্রিক বা পাসকোড দিন।" else "Authenticate with Biometric or PIN to change cloud provider."
                                ) {
                                    showProviderDialogForAccount = 2
                                }
                            },
                            languageMode = languageMode
                        )
                    }

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

                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDrive2FilesVisible) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            ),
                            onClick = { isDrive2FilesVisible = !isDrive2FilesVisible },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ড্রাইভ ২ অনলাইন সিঙ্ক ফাইল (${secondaryDriveBackups.size})" else "Drive 2 Online Sync Files (${secondaryDriveBackups.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = if (isDrive2FilesVisible) {
                                                if (languageMode == LanguageMode.BANGLA) "ফাইল তালিকা লুকাতে ট্যাপ করুন" else "Tap to hide sync files"
                                            } else {
                                                if (languageMode == LanguageMode.BANGLA) "সিঙ্ক ফাইলগুলো দেখতে ট্যাপ করুন" else "Tap to view sync files"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.fetchCloudBackups(2) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isDrive2FilesVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isDrive2FilesVisible) {
                        if (!isSecondaryLinked) {
                            item {
                                EmptyStateCard(
                                    message = if (languageMode == LanguageMode.BANGLA)
                                        "ড্রাইভ ২-এ অনলাইন সিঙ্ক সক্রিয় করতে উপরের '${secondary.provider}' একাউন্ট যুক্ত করুন।"
                                    else
                                        "Connect your ${secondary.provider} account above to manage Drive 2 online sync."
                                )
                            }
                        } else if (secondaryDriveBackups.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = if (languageMode == LanguageMode.BANGLA)
                                        "ড্রাইভ ২-এ এখনো কোনো সিঙ্ক ফাইল নেই। 'সিঙ্ক করুন' বাটনে ট্যাপ করুন।"
                                    else
                                        "No online sync files found in Drive 2. Tap 'Sync Drive 2 Now' to sync."
                                )
                            }
                        } else {
                            items(secondaryDriveBackups, key = { it.id }) { backupFile ->
                                DriveSnapshotItem(
                                    backupFile = backupFile,
                                    onRestore = {
                                        val parsedTime = DateUtils.parseIsoTimestamp(backupFile.modifiedTime)
                                        val info = DetectedBackupInfo(
                                            fileId = backupFile.id,
                                            fileName = backupFile.name,
                                            sourceProvider = "Secondary (${config.secondaryAccount.provider})",
                                            timestamp = parsedTime,
                                            deviceName = backupFile.deviceName,
                                            installationId = backupFile.installationId,
                                            accountsCount = backupFile.accountsCount,
                                            transactionsCount = backupFile.transactionsCount,
                                            driveIndex = 2,
                                            rawBackupFile = backupFile
                                        )
                                        selectedBackupForOptionsDialog = info
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
                }

                // -------------------------------------------------------------
                // TAB 3: COMMON
                // -------------------------------------------------------------
                if (selectedOnlineSyncTab == OnlineSyncTab.COMMON) {
                    // 1. Dual Cloud Auto-Sync & Manual Sync Both Drives Now
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় ডুয়েল ক্লাউড সিঙ্ক" else "Dual Cloud Auto-Sync",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "ডাটা পরিবর্তিত হলে একসাথে উভয় ড্রাইভেই (ড্রাইভ ১ ও ড্রাইভ ২) অনলাইন সিঙ্ক করুন"
                                            else
                                                "Automatically sync online to both Drive 1 and Drive 2 simultaneously",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Switch(
                                        checked = config.isDualSyncEnabled,
                                        onCheckedChange = { viewModel.setDualSyncEnabled(it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.backupToBothDrives(signedInAccount, secondarySignedInAccount)
                                    },
                                    enabled = !isLoading,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "উভয় ড্রাইভে একসাথে অনলাইন সিঙ্ক করুন" else "Sync Both Drives Online Now",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Lifecycle Auto-Sync Controls (App Start / App Close)
                    item {
                        LifecycleSyncCard(
                            autoSyncOnAppStart = config.autoSyncOnAppStart,
                            autoSyncOnAppClose = config.autoSyncOnAppClose,
                            onToggleAutoSyncOnStart = { viewModel.setAutoSyncOnAppStart(it) },
                            onToggleAutoSyncOnClose = { viewModel.setAutoSyncOnAppClose(it) },
                            languageMode = languageMode
                        )
                    }

                    // 3. Multi-Device Online Sync Policy Info Card
                    item {
                        val bPrefs = com.example.util.BackupPreferences.getInstance(context)
                        val deviceName = bPrefs.getDeviceName()
                        val installationId = bPrefs.getInstallationId()
                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ডিভাইস ও অনলাইন সিঙ্ক পলিসি" else "Device & Online Sync Policy",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "উভয় ড্রাইভেই বারবার পুরানো ব্যাকআপ ফাইল ডাম্প না করে স্মার্ট অনলাইন সিঙ্ক পরিচালিত হয়। একই ডিভাইস থেকে সিঙ্ক করলে ড্রাইভ প্রতি ঠিক ১টি সিঙ্ক ফাইল আপডেট হয়। ডিভাইস পরিবর্তন হলে একাধিক ডিভাইসের সিঙ্ক ফাইল আলাদা সংরক্ষিত থাকে।"
                                    else
                                        "Both drives perform smart Online Sync instead of duplicate backup dumps. Syncing from the same device updates exactly 1 sync file per drive. If you change devices, separate sync files are preserved for each device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "বর্তমান ডিভাইস:" else "Current Device:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = deviceName,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ইনস্টলেশন আইডি:" else "Installation ID:",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = installationId.take(12) + "...",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // 4. Quick Action: Scan for All Previous Backups across all drives and storage
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী সকল ব্যাকআপ খুঁজুন" else "Scan All Previous Backups",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "উভয় ড্রাইভ ও মেমোরি স্ক্যান করুন" else "Detect files across all drives & storage",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.scanForPreviousBackups(forcePrompt = true)
                                        showScanBackupsDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "স্ক্যান" else "Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // TAB 4: ICONS (Online Icon & Brand Logo Search & Management)
                // -------------------------------------------------------------
                if (selectedOnlineSyncTab == OnlineSyncTab.ICONS) {
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অনলাইন আইকন ও ব্র্যান্ড লোগো" else "Online Icon & Brand Library",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ব্র্যান্ডের লোগো ও ভেক্টর আইকন খুঁজুন ও সংরক্ষণ করুন" else "Search high-res brand logos & vector symbols",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cache & Storage Info Card for Icons
                    item {
                        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
                        val unusedKb = (iconCacheStats.unusedBytes / 1024).coerceAtLeast(if (iconCacheStats.unusedCount > 0) 1 else 0)

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "আইকন স্টোরেজ মেমোরি" else "Icon Storage & Cache",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "${iconCacheStats.totalCount} icons (~$totalKb KB) • ${iconCacheStats.activeCount} in-use",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (iconCacheStats.unusedCount > 0) {
                                        Button(
                                            onClick = {
                                                isCleaningCache = true
                                                coroutineScope.launch {
                                                    val (count, _) = viewModel.clearUnusedIconCache()
                                                    refreshCustomIconsAndStats()
                                                    isCleaningCache = false
                                                    Toast.makeText(
                                                        context,
                                                        if (languageMode == LanguageMode.BANGLA) "$count টি অব্যবহৃত ক্যাশ আইকন মুছে ফেলা হয়েছে" else "Cleaned $count unused cache items",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            enabled = !isCleaningCache,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাশ মুছুন ($unusedKb KB)" else "Clear Cache ($unusedKb KB)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাশ ক্লিন" else "Clean",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search input
                    item {
                        OutlinedTextField(
                            value = iconSearchQuery,
                            onValueChange = { iconSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    if (languageMode == LanguageMode.BANGLA) "আইকন বা ব্র্যান্ডের নাম লিখুন (যেমন Bkash, Netflix, Amazon)..." else "Search logo or icon (e.g. Bkash, Netflix, Amazon)...",
                                    fontSize = 12.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                if (iconSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { iconSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Preset chips
                    item {
                        val sampleQueries = listOf("Bkash", "Nagad", "Google", "Amazon", "Netflix", "Spotify", "Uber", "Food", "Medical", "Salary", "Shopping", "Gym", "Car")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(sampleQueries) { query ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (iconSearchQuery == query) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.clickable { iconSearchQuery = query }
                                ) {
                                    Text(
                                        text = query,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (iconSearchQuery == query) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Search Results / Grid
                    if (isSearchingIcons) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                            }
                        }
                    } else if (iconSearchResults.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো আইকন পাওয়া যায়নি" else "No icons found for \"$iconSearchQuery\"",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        val chunkedIcons = iconSearchResults.chunked(2)
                        items(chunkedIcons) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (item in rowItems) {
                                    val isDownloading = isDownloadingIconUrl == item.downloadUrl
                                    OutlinedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = item.previewUrl,
                                                    contentDescription = item.name,
                                                    modifier = Modifier.size(42.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }

                                            Text(
                                                text = item.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        cropEditorImageUrl = item.downloadUrl
                                                        cropEditorInitialName = item.name
                                                        showCropEditor = true
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Crop, contentDescription = "Crop", modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Crop", fontSize = 10.5.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        isDownloadingIconUrl = item.downloadUrl
                                                        coroutineScope.launch {
                                                            try {
                                                                withContext(Dispatchers.IO) {
                                                                    OnlineIconSearchService.downloadAndSaveIcon(context, item.downloadUrl, item.name)
                                                                }
                                                                refreshCustomIconsAndStats()
                                                                Toast.makeText(
                                                                    context,
                                                                    if (languageMode == LanguageMode.BANGLA) "${item.name} সংরক্ষিত হয়েছে!" else "Saved ${item.name} to icons!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            } finally {
                                                                isDownloadingIconUrl = null
                                                            }
                                                        }
                                                    },
                                                    enabled = !isDownloading,
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp)
                                                ) {
                                                    if (isDownloading) {
                                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = MaterialTheme.colorScheme.onPrimary)
                                                    } else {
                                                        Icon(Icons.Default.Download, contentDescription = "Save", modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("Save", fontSize = 10.5.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (hasMoreIcons) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        val nextPage = iconSearchPage + 1
                                        iconSearchPage = nextPage
                                        coroutineScope.launch {
                                            try {
                                                val nextResults = withContext(Dispatchers.IO) {
                                                    OnlineIconSearchService.searchIcons(iconSearchQuery.ifBlank { "bank google shopping" }, page = nextPage)
                                                }
                                                iconSearchResults = iconSearchResults + nextResults
                                                hasMoreIcons = nextResults.isNotEmpty()
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "আরো আইকন দেখুন" else "Load More Icons", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // TAB 5: IMAGES (Online Image Search & Crop/Edit Studio)
                // -------------------------------------------------------------
                if (selectedOnlineSyncTab == OnlineSyncTab.IMAGES) {
                    item {
                        OutlinedCard(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অনলাইন ইমেজ ও ফটো স্টুডিও" else "Online Image Search & Editing Studio",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "উচ্চমানের ছবি খুঁজুন, ক্রপ ও রোটেট করুন এবং কম্প্রেশন করে সেভ করুন" else "Search images, crop/rotate, and save compressed high quality",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { openOnlineGallery() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "গ্যালারি" else "Gallery",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { openOnlineDirectFileManager() },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.2f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফাইল ম্যানেজার" else "File Manager",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cache & Storage Info Card for Images
                    item {
                        val totalKb = (iconCacheStats.totalBytes / 1024).coerceAtLeast(if (iconCacheStats.totalCount > 0) 1 else 0)
                        val unusedKb = (iconCacheStats.unusedBytes / 1024).coerceAtLeast(if (iconCacheStats.unusedCount > 0) 1 else 0)

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "কাস্টম ইমেজ মেমোরি বিবরণ" else "Custom Image Storage & Cache",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "${iconCacheStats.totalCount} saved • ${iconCacheStats.activeCount} in-use (~$totalKb KB)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (iconCacheStats.unusedCount > 0) {
                                        Button(
                                            onClick = {
                                                isCleaningCache = true
                                                coroutineScope.launch {
                                                    val (count, _) = viewModel.clearUnusedIconCache()
                                                    refreshCustomIconsAndStats()
                                                    isCleaningCache = false
                                                    Toast.makeText(
                                                        context,
                                                        if (languageMode == LanguageMode.BANGLA) "$count টি ক্যাশ ইমেজ মুছে ফেলা হয়েছে" else "Cleaned $count unused cache items",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            enabled = !isCleaningCache,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাশ মুছুন ($unusedKb KB)" else "Clear Cache ($unusedKb KB)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাশ ক্লিন" else "Clean",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search input
                    item {
                        OutlinedTextField(
                            value = imageSearchQuery,
                            onValueChange = { imageSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    if (languageMode == LanguageMode.BANGLA) "ছবি খুঁজুন (যেমন Coffee, Travel, Shopping, Salary)..." else "Search photos (e.g. Coffee, Travel, Shopping)...",
                                    fontSize = 12.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            },
                            trailingIcon = {
                                if (imageSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { imageSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Preset chips for images
                    item {
                        val sampleImgQueries = listOf("Coffee", "Food", "Travel", "Office", "Groceries", "Salary", "Investment", "Shopping", "Tech", "Car")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(sampleImgQueries) { query ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (imageSearchQuery == query) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.clickable { imageSearchQuery = query }
                                ) {
                                    Text(
                                        text = query,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (imageSearchQuery == query) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Image Search Results Grid (3 columns)
                    if (isSearchingImages) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    } else if (imageSearchResults.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো ছবি পাওয়া যায়নি" else "No images found for \"$imageSearchQuery\"",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        val chunkedImages = imageSearchResults.chunked(3)
                        items(chunkedImages) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (item in rowItems) {
                                    OutlinedCard(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                cropEditorImageUrl = item.downloadUrl
                                                cropEditorInitialName = item.name
                                                showCropEditor = true
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            ) {
                                                AsyncImage(
                                                    model = item.previewUrl,
                                                    contentDescription = item.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                Surface(
                                                    shape = RoundedCornerShape(bottomStart = 6.dp),
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier.align(Alignment.TopEnd)
                                                ) {
                                                    Text(
                                                        text = item.source,
                                                        fontSize = 8.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Crop,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Crop & Save",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                                for (i in 0 until (3 - rowItems.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (hasMoreImages) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        val nextPage = imageSearchPage + 1
                                        imageSearchPage = nextPage
                                        coroutineScope.launch {
                                            try {
                                                val nextResults = withContext(Dispatchers.IO) {
                                                    OnlineIconSearchService.searchImages(imageSearchQuery.ifBlank { "nature finance" }, page = nextPage)
                                                }
                                                imageSearchResults = imageSearchResults + nextResults
                                                hasMoreImages = nextResults.isNotEmpty()
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "আরো ছবি দেখুন" else "Load More Images", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

    // =================================================================
    // SECTION 2: LOCAL STORAGE
    // =================================================================
    if (selectedManagementTab == DataManagementTab.LOCAL) {
        // Tab-specific Status Feedback
        item {
            TabStatusFeedbackCard(
                state = backupUiState,
                onDismiss = { viewModel.clearBackupUiState() }
            )
        }

        item {
            TreeSectionHeader(
                title = if (languageMode == LanguageMode.BANGLA) "ডিভাইস লোকাল স্টোরেজ" else "Device Local Storage",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফোন মেমোরি ও অফলাইন স্ন্যাপশট" else "Phone storage & offline snapshots",
                icon = Icons.Default.SdStorage,
                isExpanded = isLocalTreeExpanded,
                onToggle = { isLocalTreeExpanded = !isLocalTreeExpanded },
                badgeText = "${localBackups.size} " + (if (languageMode == LanguageMode.BANGLA) "টি ফাইল" else "Files")
            )
        }

        if (isLocalTreeExpanded) {
                // Action buttons: Create Local Backup & Restore From File (Common Opened Feature)
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
                                importFileLauncher.launch(StorageLocationHelper.JSON_MIME_TYPES)
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

                // Local Backups Header / Button
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isLocalSnapshotsVisible) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        ),
                        onClick = { isLocalSnapshotsVisible = !isLocalSnapshotsVisible },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.SdStorage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত লোকাল স্ন্যাপশট (${localBackups.size})" else "Local Snapshots (${localBackups.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isLocalSnapshotsVisible) {
                                            if (languageMode == LanguageMode.BANGLA) "ফাইল তালিকা লুকাতে ট্যাপ করুন" else "Tap to hide snapshot files"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "ফাইলগুলো দেখতে ট্যাপ করুন" else "Tap to view snapshot files"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { refreshLocalBackups() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isLocalSnapshotsVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                if (isLocalSnapshotsVisible) {
                    if (localBackups.isEmpty()) {
                        item {
                            EmptyStateCard(
                                message = if (languageMode == LanguageMode.BANGLA) "কোনো লোকাল ব্যাকআপ পাওয়া যায়নি" else "No local backups found in this folder."
                            )
                        }
                    } else {
                        items(localBackups) { file ->
                            val fileTimestamp = BackupManager.getBackupFileTimestamp(file)
                            val dateStr = "${DateUtils.formatDate(fileTimestamp, languageMode)} • ${DateUtils.formatTime(fileTimestamp, languageMode)}"
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
                                            onClick = {
                                                val info = DetectedBackupInfo(
                                                    fileId = file.name,
                                                    fileName = file.name,
                                                    sourceProvider = "Local Storage",
                                                    timestamp = fileTimestamp,
                                                    localFile = file
                                                )
                                                selectedBackupForOptionsDialog = info
                                            },
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

                // Unused Custom Icons Cache Cleaner
                item {
                    var cacheStats by remember { mutableStateOf(IconHelper.IconCacheStats()) }
                    var isClearingIcons by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        withContext(Dispatchers.IO) {
                            try {
                                val db = com.example.data.local.AppDatabase.getDatabase(context)
                                val catIcons = db.categoryDao().getAllCategoriesSnapshot().map { it.iconName }
                                val accIcons = db.accountDao().getAllAccountsSnapshot().map { it.iconName }
                                val goalIcons = db.savingsGoalDao().getAllGoals().firstOrNull()?.map { it.iconName } ?: emptyList()
                                val active = (catIcons + accIcons + goalIcons).filter { it.isNotBlank() }.toSet()
                                val stats = IconHelper.getUnusedCustomIconsStats(context, active)
                                withContext(Dispatchers.Main) {
                                    cacheStats = stats
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = if (cacheStats.unusedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "অব্যবহৃত আইকন ক্যাশ" else "Unused Custom Icons Cache",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    val unusedKb = (cacheStats.unusedBytes / 1024).coerceAtLeast(0)
                                    Text(
                                        text = if (cacheStats.unusedCount > 0) {
                                            if (languageMode == LanguageMode.BANGLA)
                                                "${cacheStats.unusedCount} টি অব্যবহৃত আইকন (~$unusedKb KB খালি করা যাবে)"
                                            else
                                                "${cacheStats.unusedCount} unused icons (~$unusedKb KB reclaimable)"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "কোনো অতিরিক্ত আইকন ক্যাশ নেই" else "Storage is clean, no unassigned icons"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (cacheStats.unusedCount > 0) {
                                Button(
                                    onClick = {
                                        isClearingIcons = true
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                val db = com.example.data.local.AppDatabase.getDatabase(context)
                                                val catIcons = db.categoryDao().getAllCategoriesSnapshot().map { it.iconName }
                                                val accIcons = db.accountDao().getAllAccountsSnapshot().map { it.iconName }
                                                val goalIcons = db.savingsGoalDao().getAllGoals().firstOrNull()?.map { it.iconName } ?: emptyList()
                                                val active = (catIcons + accIcons + goalIcons).filter { it.isNotBlank() }.toSet()
                                                val (deletedCount, freedBytes) = IconHelper.clearUnusedCustomIcons(context, active)
                                                val updatedStats = IconHelper.getUnusedCustomIconsStats(context, active)
                                                withContext(Dispatchers.Main) {
                                                    cacheStats = updatedStats
                                                    isClearingIcons = false
                                                    val freedKb = (freedBytes / 1024).coerceAtLeast(1)
                                                    Toast.makeText(
                                                        context,
                                                        if (languageMode == LanguageMode.BANGLA)
                                                            "$deletedCount টি অব্যবহৃত আইকন মুছে $freedKb KB খালি করা হয়েছে"
                                                        else
                                                            "Cleaned $deletedCount unused icons ($freedKb KB freed)",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isClearingIcons,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    if (isClearingIcons) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = androidx.compose.ui.graphics.Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            if (languageMode == LanguageMode.BANGLA) "ক্যাশ মুছুন" else "Clear Cache",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tree Sub-branch 2.1: Storage Directory & Daily Scheduled Auto-Backup
                item {
                    TreeSubBranchCard(
                        title = if (languageMode == LanguageMode.BANGLA) "স্টোরেজ ফোল্ডার ও স্বয়ংক্রিয় ব্যাকআপ শিডিউল" else "Storage Folder & Scheduled Backup",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যাকআপ ডিরেক্টরি লোকেশন ও প্রতিদিনের সময় নির্ধারণ" else "Folder path configuration & daily scheduled backup time",
                        icon = Icons.Default.Folder,
                        isExpanded = isLocalStorageExpanded,
                        onToggle = { isLocalStorageExpanded = !isLocalStorageExpanded }
                    ) {
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফোল্ডার লোকেশন" else "Directory Location",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = BackupManager.formatDirectoryDisplayName(config.localBackupDirectory),
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { dirPickerLauncher.launch(null) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("SAF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { showFolderPickerDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (languageMode == LanguageMode.BANGLA) "নির্বাচন" else "Select", fontSize = 11.sp)
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় লোকাল ব্যাকআপ" else "Daily Auto Phone Backup",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "প্রতিদিন নির্দিষ্ট সময়ে লোকাল স্ন্যাপশট সংরক্ষণ করুন" else "Automatically save daily snapshot at scheduled time",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Switch(
                                        checked = config.isAutoPhoneBackupEnabled,
                                        onCheckedChange = { viewModel.setAutoPhoneBackupEnabled(it) }
                                    )
                                }

                                if (config.isAutoPhoneBackupEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (languageMode == LanguageMode.BANGLA) "প্রতিদিনের সময়" else "Scheduled Time", fontSize = 11.5.sp)
                                            }
                                            Text(
                                                text = config.formattedScheduledTime,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    // =================================================================
    // SECTION 3: EXPORT IMPORT
    // =================================================================
    if (selectedManagementTab == DataManagementTab.EXPORT_IMPORT) {
        // Tab-specific Status Feedback
        item {
            TabStatusFeedbackCard(
                state = backupUiState,
                onDismiss = { viewModel.clearBackupUiState() }
            )
        }

        item {
            TreeSectionHeader(
                title = if (languageMode == LanguageMode.BANGLA) "এক্সপোর্ট ও ইম্পোর্ট সেটিংস" else "Export & Import Settings",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ডাটা এক্সপোর্ট ও ইম্পোর্ট" else "Data export & import",
                icon = Icons.Default.SwapVert,
                isExpanded = isMoreTreeExpanded,
                onToggle = { isMoreTreeExpanded = !isMoreTreeExpanded },
                badgeText = if (languageMode == LanguageMode.BANGLA) "এক্সপোর্ট / ইমপোর্ট" else "Export / Import"
            )
        }

        if (isMoreTreeExpanded) {
                // Tree Sub-branch 3.1: Data Export (CSV, Excel, JSON)
                item {
                    TreeSubBranchCard(
                        title = if (languageMode == LanguageMode.BANGLA) "ডাটা এক্সপোর্ট (রপ্তানি)" else "Data Export (CSV & JSON)",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "এক্সেল স্প্রেডশীট বা পূর্ণাঙ্গ ডাটাবেজ ব্যাকআপ" else "Export to CSV/Excel or full database backup",
                        icon = Icons.Default.FileUpload,
                        isExpanded = isExportImportExpanded,
                        onToggle = { isExportImportExpanded = !isExportImportExpanded }
                    ) {
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

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

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

                // Tree Sub-branch 3.2: Data Import (CSV, QIF, JSON)
                item {
                    TreeSubBranchCard(
                        title = if (languageMode == LanguageMode.BANGLA) "ডাটা ইম্পোর্ট (আমদানি)" else "Data Import & Restore",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "CSV, QIF বা JSON ফাইল থেকে ডাটা লোড" else "Smart CSV parser, QIF bank statements & JSON restore",
                        icon = Icons.Default.FileDownload,
                        isExpanded = true,
                        onToggle = { }
                    ) {
                        SettingsListTile(
                            icon = Icons.Default.UploadFile,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ফাইল ইম্পোর্ট" else "Import from CSV / Excel",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় প্রিভিউ ও কলাম ম্যাপিং" else "Smart CSV parser with auto mapping and preview dialog",
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                            },
                            onClick = {
                                csvFileLauncher.launch(StorageLocationHelper.CSV_EXCEL_MIME_TYPES)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingsListTile(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = if (languageMode == LanguageMode.BANGLA) "QIF ফরম্যাট থেকে ইম্পোর্ট" else "Import from QIF Format",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যাংক স্টেটমেন্ট ও Quicken ফরম্যাট (.qif)" else "Quicken Interchange Format (.qif) for bank exports",
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                            },
                            onClick = {
                                qifFileLauncher.launch(StorageLocationHelper.QIF_MIME_TYPES)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingsListTile(
                            icon = Icons.Default.FileDownload,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = if (languageMode == LanguageMode.BANGLA) "JSON ব্যাকআপ থেকে রিস্টোর" else "Restore from JSON File",
                            subtitle = if (languageMode == LanguageMode.BANGLA) "পূর্বে ব্যাকআপ নেওয়া JSON ফাইল থেকে সম্পূর্ণ ডাটা প্রতিস্থাপন" else "Restore and replace database from existing JSON backup",
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                            },
                            onClick = {
                                importFileLauncher.launch(StorageLocationHelper.JSON_MIME_TYPES)
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
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

    // 2. Account Credentials Setup Dialog for Non-Google providers (Dropbox, etc.)
    showAccountEditDialogForAccount?.let { driveIndex ->
        val acc = if (driveIndex == 1) config.primaryAccount else config.secondaryAccount
        val isDropbox = acc.provider.contains("Dropbox", ignoreCase = true)

        var editAppKey by remember { mutableStateOf(acc.appKey.ifEmpty { DropboxService.DEFAULT_APP_KEY }) }
        var editEmail by remember { mutableStateOf(acc.email) }
        var editName by remember { mutableStateOf(acc.displayName) }
        var editServer by remember { mutableStateOf(acc.serverUrl) }
        var editToken by remember { mutableStateOf(acc.accessToken) }
        var showManualTokenSection by remember { mutableStateOf(false) }
        var isVerifying by remember { mutableStateOf(false) }
        var verifyFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

        AlertDialog(
            onDismissRequest = { showAccountEditDialogForAccount = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isDropbox) Icons.Default.CloudSync else Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Configure ${acc.provider} (Drive $driveIndex)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isDropbox) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text("Dropbox Browser OAuth 2.0 (PKCE)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = "Log in securely via your web browser with automatic token refresh. No manual token expiration or copy-pasting required.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = editAppKey,
                            onValueChange = { editAppKey = it.trim() },
                            label = { Text("Dropbox App Key") },
                            placeholder = { Text("e.g. 1f4eghd7kix90h9") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dropbox.com/developers/apps"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Dropbox Console", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (editAppKey.isNotBlank()) {
                                        viewModel.startDropboxAuth(context, editAppKey, driveIndex)
                                        showAccountEditDialogForAccount = null
                                    }
                                },
                                enabled = editAppKey.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sign In (Browser)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Required Dropbox Console Redirect URI:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                Text("budgeter://dropbox-auth", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (acc.isLinked && (acc.refreshToken.isNotBlank() || acc.accessToken.isNotBlank())) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Linked as: ${acc.displayName.ifEmpty { acc.email }}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(if (acc.refreshToken.isNotBlank()) "OAuth Active • Auto-refreshing" else "Access Token active", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.disconnectDropbox(driveIndex)
                                            showAccountEditDialogForAccount = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Disconnect", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { showManualTokenSection = !showManualTokenSection },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showManualTokenSection) "Hide Manual Access Token" else "Manual Access Token (Advanced / Alternative)", fontSize = 11.sp)
                        }

                        if (showManualTokenSection) {
                            OutlinedTextField(
                                value = editToken,
                                onValueChange = {
                                    editToken = it
                                    verifyFeedback = null
                                },
                                label = { Text("Manual Access Token") },
                                placeholder = { Text("Paste access token here...") },
                                singleLine = false,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    isVerifying = true
                                    verifyFeedback = null
                                    viewModel.connectDropbox(driveIndex, editToken) { success, msg ->
                                        isVerifying = false
                                        verifyFeedback = Pair(success, msg)
                                        if (success) {
                                            val updated = if (driveIndex == 1) config.primaryAccount else config.secondaryAccount
                                            editEmail = updated.email
                                            editName = updated.displayName
                                        }
                                    }
                                },
                                enabled = editToken.isNotBlank() && !isVerifying,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Verifying Token...")
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Test & Verify Token")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Enter account credentials or connection details for ${acc.provider}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    verifyFeedback?.let { (success, msg) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = if (success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

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
                        val isLinked = editEmail.trim().isNotEmpty() || editToken.trim().isNotEmpty()
                        if (driveIndex == 1) {
                            viewModel.setPrimaryAccount(
                                email = editEmail.trim(),
                                displayName = editName.trim().ifEmpty { editEmail.trim() },
                                isLinked = isLinked,
                                serverUrl = editServer.trim(),
                                accessToken = editToken.trim()
                            )
                            viewModel.fetchCloudBackups(1)
                        } else {
                            viewModel.setSecondaryAccount(
                                email = editEmail.trim(),
                                displayName = editName.trim().ifEmpty { editEmail.trim() },
                                isLinked = isLinked,
                                serverUrl = editServer.trim(),
                                accessToken = editToken.trim()
                            )
                            viewModel.fetchCloudBackups(2)
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
                    viewModel.restoreFromCloudProvider(2, backupFile)
                } else {
                    viewModel.restoreFromCloudProvider(1, backupFile)
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
                    viewModel.deleteFromCloudProvider(2, backupFile)
                } else {
                    viewModel.deleteFromCloudProvider(1, backupFile)
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

    // Biometric / Passcode Auth for Unlink or Change Cloud Account
    pendingSecurityAction?.let { pending ->
        SecurityAuthDialog(
            title = pending.title,
            message = pending.message,
            confirmButtonText = pending.confirmButtonText,
            isDestructive = pending.isDestructive,
            requiresAuth = securityConfig.hasPin || securityConfig.isBiometricEnabled,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onConfirm = {
                val action = pending.onExecute
                pendingSecurityAction = null
                action()
            },
            onDismiss = { pendingSecurityAction = null }
        )
    }

    // Restore or Merge Selection Dialog
    selectedBackupForOptionsDialog?.let { backupInfo ->
        RestoreOrMergeOptionsDialog(
            backupTitle = backupInfo.fileName,
            backupInfo = backupInfo,
            languageMode = languageMode,
            onMerge = {
                viewModel.restoreDetectedBackup(backupInfo, merge = true)
                selectedBackupForOptionsDialog = null
            },
            onRestoreReplace = {
                viewModel.restoreDetectedBackup(backupInfo, merge = false)
                selectedBackupForOptionsDialog = null
            },
            onDismiss = { selectedBackupForOptionsDialog = null }
        )
    }

    // Detected Backups List & Scan Dialog
    if (showScanBackupsDialog) {
        DetectedBackupsListDialog(
            detectedBackups = detectedBackups,
            isFirstLaunchPrompt = false,
            languageMode = languageMode,
            onScanAgain = { viewModel.scanForPreviousBackups() },
            onSelectBackupToRestore = { backup, isMerge ->
                viewModel.restoreDetectedBackup(backup, isMerge)
                showScanBackupsDialog = false
            },
            onDismiss = { showScanBackupsDialog = false }
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

    if (showCropEditor && cropEditorImageUrl != null) {
        IconCropEditorModal(
            imageUrl = cropEditorImageUrl,
            onDismiss = {
                showCropEditor = false
                cropEditorImageUrl = null
            },
            onCroppedIconSaved = { savedKey ->
                showCropEditor = false
                cropEditorImageUrl = null
                refreshCustomIconsAndStats()
                Toast.makeText(
                    context,
                    if (languageMode == LanguageMode.BANGLA) "আইকন সফলভাবে সংরক্ষিত হয়েছে!" else "Saved icon to custom icons!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
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
    photoUrl: String = "",
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
                        modifier = Modifier.size(42.dp)
                    ) {
                        if (isLinked && photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isLinked) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                title = if (languageMode == LanguageMode.BANGLA) "অটো-সিঙ্ক (স্বয়ংক্রিয়)" else "Automatic Cloud Sync",
                subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ শুরু ও বন্ধের সময় এই ড্রাইভে ক্লাউড স্ন্যাপশট আপলোড করুন" else "Upload snapshots to this cloud drive during app lifecycle sync",
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
                val dateStr = DateUtils.formatSyncDateTime(backupFile.modifiedTime, languageMode)
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
private fun LifecycleSyncCard(
    autoSyncOnAppStart: Boolean,
    autoSyncOnAppClose: Boolean,
    onToggleAutoSyncOnStart: (Boolean) -> Unit,
    onToggleAutoSyncOnClose: (Boolean) -> Unit,
    languageMode: LanguageMode
) {
    OutlinedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় সিঙ্ক স্ট্র্যাটেজি" else "Lifecycle Auto-Sync Options",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "অ্যাপ শুরু বা বন্ধের সময় সিঙ্ক হবে (যেকোনো একটি অবশ্যই সক্রিয় থাকবে)"
                        else
                            "Sync on start and/or close. At least one must be enabled.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 15.sp
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 1. Auto Sync on App Start
            SettingsListTile(
                icon = Icons.Default.CloudQueue,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাপ শুরুর সময় অটো সিঙ্ক" else "Auto sync on app start",
                subtitle = if (languageMode == LanguageMode.BANGLA)
                    "অ্যাপ খোলার সময় ব্যাকগ্রাউন্ডে ক্লাউড ও লোকাল স্ন্যাপশট সিঙ্ক হবে"
                else
                    "Automatically trigger background sync when app starts",
                trailingContent = {
                    Switch(
                        checked = autoSyncOnAppStart,
                        onCheckedChange = { onToggleAutoSyncOnStart(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 2. Auto Sync on App Close
            SettingsListTile(
                icon = Icons.Default.CloudUpload,
                iconTint = MaterialTheme.colorScheme.primary,
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাপ বন্ধের সময় অটো সিঙ্ক" else "Auto sync on app close",
                subtitle = if (languageMode == LanguageMode.BANGLA)
                    "অ্যাপ বন্ধ বা ব্যাকগ্রাউন্ডে যাওয়ার সময় ব্যাকআপ সুরক্ষিত রাখা হবে"
                else
                    "Automatically trigger background sync when app closes",
                trailingContent = {
                    Switch(
                        checked = autoSyncOnAppClose,
                        onCheckedChange = { onToggleAutoSyncOnClose(it) }
                    )
                }
            )
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

@Composable
private fun TreeSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        color = if (isExpanded) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            1.dp,
            if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (!badgeText.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        lineHeight = 14.sp
                    )
                }
            }

            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TreeSubBranchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggle() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

private data class PendingSecurityAction(
    val title: String,
    val message: String,
    val confirmButtonText: String = "Confirm",
    val isDestructive: Boolean = false,
    val onExecute: () -> Unit
)

@Composable
private fun TabStatusFeedbackCard(
    state: BackupUiState,
    onDismiss: () -> Unit
) {
    when (state) {
        is BackupUiState.Success -> {
            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        is BackupUiState.Error -> {
            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

