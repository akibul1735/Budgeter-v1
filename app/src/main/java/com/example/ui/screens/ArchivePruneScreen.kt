package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountType
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.DatePickerModal
import com.example.ui.dialogs.SecurityAuthDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.ui.viewmodel.ArchiveUiState
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AffectedAccountImpact
import com.example.util.ArchiveImpactSummary
import com.example.util.ArchiveVerificationResult
import com.example.util.ImportArchivePreview
import com.example.util.LanguageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ArchivePresetRange(val labelEn: String, val labelBn: String) {
    PREV_FISCAL_YEAR("Previous Fiscal Year (Jul 1 - Jun 30)", "পূর্ববর্তী অর্থবছর (১ জুলাই - ৩০ জুন)"),
    CALENDAR_YEAR_2024("Calendar Year 2024", "ক্যালেন্ডার বছর ২০২৪"),
    CALENDAR_YEAR_2023("Calendar Year 2023", "ক্যালেন্ডার বছর ২০২৩"),
    OLDER_THAN_1_YEAR("Older than 1 Year", "১ বছরের অধিক পুরোনো"),
    OLDER_THAN_2_YEARS("Older than 2 Years", "২ বছরের অধিক পুরোনো"),
    CUSTOM("Custom Date Range", "কাস্টম সময়সীমা")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivePruneScreen(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Archive & Prune, 1: History & Restore

    val archiveUiState by viewModel.archiveUiState.collectAsStateWithLifecycle()
    val impactSummary by viewModel.archiveImpactSummary.collectAsStateWithLifecycle()
    val localArchives by viewModel.localArchives.collectAsStateWithLifecycle()
    val importPreview by viewModel.importArchivePreview.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    val backupConfig by viewModel.backupSettingsConfig.collectAsStateWithLifecycle()

    // Preset & Custom Range State
    var selectedPreset by remember { mutableStateOf(ArchivePresetRange.OLDER_THAN_1_YEAR) }
    var startDateEpochMs by remember { mutableLongStateOf(0L) }
    var endDateEpochMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dateRangeLabel by remember { mutableStateOf("") }

    // Dialog & Auth States
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showFinalConfirmAuthDialog by remember { mutableStateOf(false) }
    var showImportConfirmAuthDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var archivePassword by remember { mutableStateOf("") }
    var enableEncryption by remember { mutableStateOf(false) }
    var saveToCloudDrive by remember { mutableStateOf(true) }
    var showPasswordVisibility by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var importPasswordInput by remember { mutableStateOf("") }
    var lastCompletedResult by remember { mutableStateOf<ArchiveVerificationResult?>(null) }

    // SAF Picker for Importing Archive
    val archivePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            viewModel.previewImportArchiveFile(uri)
        }
    }

    // Function to calculate start and end epoch ms for presets
    fun applyPresetRange(preset: ArchivePresetRange) {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val currentYear = cal.get(Calendar.YEAR)

        when (preset) {
            ArchivePresetRange.PREV_FISCAL_YEAR -> {
                // Previous Fiscal Year: Jul 1 (currentYear - 2) to Jun 30 (currentYear - 1) OR standard fiscal year
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear - 1)
                    set(Calendar.MONTH, Calendar.JULY)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, Calendar.JUNE)
                    set(Calendar.DAY_OF_MONTH, 30)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                startDateEpochMs = 0L // Include everything up to the fiscal year end
                endDateEpochMs = endCal.timeInMillis
                dateRangeLabel = "Fiscal Year Ending Jun 30, ${currentYear}"
            }
            ArchivePresetRange.CALENDAR_YEAR_2024 -> {
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2024)
                    set(Calendar.MONTH, Calendar.DECEMBER)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                startDateEpochMs = 0L
                endDateEpochMs = endCal.timeInMillis
                dateRangeLabel = "Calendar Year 2024 & Prior"
            }
            ArchivePresetRange.CALENDAR_YEAR_2023 -> {
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2023)
                    set(Calendar.MONTH, Calendar.DECEMBER)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                startDateEpochMs = 0L
                endDateEpochMs = endCal.timeInMillis
                dateRangeLabel = "Calendar Year 2023 & Prior"
            }
            ArchivePresetRange.OLDER_THAN_1_YEAR -> {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -365)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                startDateEpochMs = 0L
                endDateEpochMs = endCal.timeInMillis
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                dateRangeLabel = "Up to ${sdf.format(Date(endDateEpochMs))} (Older than 1 yr)"
            }
            ArchivePresetRange.OLDER_THAN_2_YEARS -> {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -730)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                startDateEpochMs = 0L
                endDateEpochMs = endCal.timeInMillis
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                dateRangeLabel = "Up to ${sdf.format(Date(endDateEpochMs))} (Older than 2 yrs)"
            }
            ArchivePresetRange.CUSTOM -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                dateRangeLabel = "${sdf.format(Date(startDateEpochMs))} - ${sdf.format(Date(endDateEpochMs))}"
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadLocalArchives()
        applyPresetRange(selectedPreset)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও ডাটা প্রুনিং" else "Smart Archive & Prune",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স অপরিবর্তিত রেখে ডাটাবেজ অপ্টিমাইজ করুন" else "Shrink database & keep 100% balance accuracy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("archive_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও প্রুন" else "Archive & Prune", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.loadLocalArchives()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "আর্কাইভ হিস্ট্রি ও রিস্টোর" else "History & Restore", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            // Global Status / Message Banner
            AnimatedVisibility(visible = archiveUiState !is ArchiveUiState.Idle) {
                when (val state = archiveUiState) {
                    is ArchiveUiState.Loading -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                Spacer(Modifier.width(16.dp))
                                Text(state.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    is ArchiveUiState.Success -> {
                        Surface(
                            color = SolidIncome.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    state.verificationResult?.savedLocalPath?.let { path ->
                                        Text("Saved: $path", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { viewModel.resetArchiveState() }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Dismiss", tint = SolidIncome)
                                }
                            }
                        }
                    }
                    is ArchiveUiState.Error -> {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Operation Aborted",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                TextButton(onClick = { viewModel.resetArchiveState() }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                    ArchiveUiState.Idle -> {}
                }
            }

            if (selectedTab == 0) {
                // ==========================================
                // TAB 1: ARCHIVE & PRUNE WIZARD
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Concept Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "স্মার্ট ফিসক্যাল ইয়ার আর্কাইভ" else "Smart Financial Roll-Forward",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "পুরাতন লেনদেন এনক্রিপ্ট করে সরিয়ে শুরুর ব্যালেন্সে রূপান্তর করে। ব্যালেন্স ও রিপোর্ট ১০০% অপরিবর্তিত থাকবে।"
                                        else
                                            "Exports old transactions into encrypted archive and adjusts Starting Balances so all account balances match 100% perfectly.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // STEP 1: DATE RANGE SELECTION
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "আর্কাইভের সময়সীমা নির্বাচন করুন" else "Select Date Range to Archive",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                // Presets Grid / Chips
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ArchivePresetRange.entries.forEach { preset ->
                                        val isSelected = selectedPreset == preset
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    selectedPreset = preset
                                                    applyPresetRange(preset)
                                                    viewModel.clearArchiveImpact()
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (preset == ArchivePresetRange.CUSTOM) Icons.Default.DateRange else Icons.Default.CalendarMonth,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(
                                                        text = if (languageMode == LanguageMode.BANGLA) preset.labelBn else preset.labelEn,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                if (selectedPreset == ArchivePresetRange.CUSTOM) {
                                    Spacer(Modifier.height(12.dp))
                                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showStartDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Start Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                                Text(sdf.format(Date(startDateEpochMs)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { showEndDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("End Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                                Text(sdf.format(Date(endDateEpochMs)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 2: STORAGE DESTINATION & ENCRYPTION OPTIONS
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "স্টোরেজ ও এনক্রিপশন" else "Storage & Encryption Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                // Local Storage Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Save Local Archive (.zip/.barch)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Documents/Budgeter/Archives & internal cache", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(20.dp))
                                }

                                Spacer(Modifier.height(10.dp))

                                // Cloud Storage Sync Toggle
                                val hasCloudConnected = backupConfig.primaryAccount.email.isNotBlank() || backupConfig.secondaryAccount.email.isNotBlank()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = if (hasCloudConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Upload to Connected Cloud Drive", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            if (hasCloudConnected) "Google Drive / Dropbox (Budgeter/Archives)" else "No cloud storage account linked in Backup & Sync",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Switch(
                                        checked = saveToCloudDrive && hasCloudConnected,
                                        onCheckedChange = { saveToCloudDrive = it },
                                        enabled = hasCloudConnected
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                // Password Encryption Option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AES-256 Passphrase Protection", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Encrypt archive payload with custom password", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Switch(
                                        checked = enableEncryption,
                                        onCheckedChange = {
                                            enableEncryption = it
                                            if (!it) archivePassword = ""
                                        }
                                    )
                                }

                                if (enableEncryption) {
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = archivePassword,
                                        onValueChange = { archivePassword = it },
                                        label = { Text("Archive Password / Passphrase") },
                                        singleLine = true,
                                        visualTransformation = if (showPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showPasswordVisibility = !showPasswordVisibility }) {
                                                Icon(
                                                    imageVector = if (showPasswordVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // STEP 3: ANALYZE IMPACT BUTTON & PREVIEW
                    item {
                        if (impactSummary == null) {
                            Button(
                                onClick = {
                                    viewModel.analyzeArchiveImpact(
                                        startDateEpochMs = startDateEpochMs,
                                        endDateEpochMs = endDateEpochMs,
                                        dateRangeLabel = dateRangeLabel
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("analyze_impact_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "প্রভাব বিশ্লেষণ ও নতুন ব্যালেন্স প্রিভিউ দেখুন" else "Analyze & Preview Historical Impact",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            val summary = impactSummary!!
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Impact Highlights Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(SolidIncome, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "হিস্ট্রিক্যাল ইমপ্যাক্ট প্রিভিউ" else "Historical Impact Preview",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Surface(
                                                color = SolidIncome.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "100% Balanced",
                                                    color = SolidIncome,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(14.dp))

                                        // Metrics Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            MetricBadge(
                                                title = "Archived Txs",
                                                value = "${summary.totalTransactions}",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            MetricBadge(
                                                title = "Space Saved",
                                                value = "~${"%.1f".format(summary.estimatedDbReductionKb)} KB",
                                                color = SolidIncome,
                                                modifier = Modifier.weight(1f)
                                            )
                                            MetricBadge(
                                                title = "Total Expense",
                                                value = "৳${"%.0f".format(summary.totalExpense)}",
                                                color = SolidExpense,
                                                modifier = Modifier.weight(1f)
                                            )
                                            MetricBadge(
                                                title = "Total Income",
                                                value = "৳${"%.0f".format(summary.totalIncome)}",
                                                color = SolidIncome,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(Modifier.height(16.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ব্যালেন্স রূপান্তর টেবিল" else "Account Starting Balance Conversions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Old Starting Balance + Archived Impact = New Starting Balance (Post-prune balance remains 100% identical)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        Spacer(Modifier.height(10.dp))

                                        // Account Impact Table
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            summary.affectedAccounts.forEach { accImpact ->
                                                AccountImpactRow(accImpact, languageMode)
                                            }
                                        }
                                    }
                                }

                                // Safety Guarantee Checklist Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Pruning Safety & Atomicity Guarantees", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(Modifier.height(8.dp))
                                        SafetyCheckItem("Encrypted archive created & saved to disk first")
                                        SafetyCheckItem("In-memory checksum & transaction count strictly verified")
                                        SafetyCheckItem("Starting balances mathematically adjusted for zero balance deviation")
                                        SafetyCheckItem("Zero data deleted if archive creation or verification fails")
                                        SafetyCheckItem("Full restoration supported anytime via History & Restore")
                                    }
                                }

                                // Final Action Button
                                Button(
                                    onClick = {
                                        showFinalConfirmAuthDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("execute_archive_prune_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Archive, contentDescription = null)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও ডাটাবেজ প্রুন সম্পন্ন করুন" else "Archive & Prune ${summary.totalTransactions} Transactions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 2: ARCHIVE HISTORY & RESTORE
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pick archive from file picker
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Unarchive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Import External Archive", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Select a .zip or .barch file from device or cloud", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Button(
                                    onClick = {
                                        archivePickerLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Pick File")
                                }
                            }
                        }
                    }

                    // Detected Local Archives
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ডিভাইসে সংরক্ষিত আর্কাইভসমূহ" else "Detected Local Archives on Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (localArchives.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No previous archives found on this device", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(localArchives) { file ->
                            LocalArchiveFileCard(
                                file = file,
                                languageMode = languageMode,
                                onRestore = {
                                    val uri = Uri.fromFile(file)
                                    pendingImportUri = uri
                                    viewModel.previewImportArchiveFile(uri)
                                },
                                onShare = {
                                    try {
                                        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/zip"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Budget Archive"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot share file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = if (startDateEpochMs > 0) startDateEpochMs else System.currentTimeMillis(),
            languageMode = languageMode,
            onDateSelected = {
                startDateEpochMs = it
                showStartDatePicker = false
                applyPresetRange(ArchivePresetRange.CUSTOM)
                viewModel.clearArchiveImpact()
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = endDateEpochMs,
            languageMode = languageMode,
            onDateSelected = {
                endDateEpochMs = it
                showEndDatePicker = false
                applyPresetRange(ArchivePresetRange.CUSTOM)
                viewModel.clearArchiveImpact()
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // FINAL CONFIRMATION & AUTHENTICATION DIALOG FOR PRUNING
    if (showFinalConfirmAuthDialog && impactSummary != null) {
        val summary = impactSummary!!
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও প্রুন নিশ্চিতকরণ" else "Confirm Archive & Prune",
            message = if (languageMode == LanguageMode.BANGLA)
                "${summary.totalTransactions}টি লেনদেন এনক্রিপ্ট করে আর্কাইভ করা হবে এবং শুরুর ব্যালেন্স সমন্বয় করা হবে। আপনি কি নিশ্চিত?"
            else
                "This will export ${summary.totalTransactions} transactions to an encrypted archive and adjust starting balances for ${summary.affectedAccounts.size} accounts. Total net worth remains 100% identical.",
            confirmButtonText = if (languageMode == LanguageMode.BANGLA) "প্রুন সম্পন্ন করুন" else "Archive & Prune Now",
            requiresAuth = true,
            securityConfig = securityConfig,
            isDestructive = true,
            languageMode = languageMode,
            onVerifyPin = { pin -> viewModel.verifySecurityPin(pin) },
            onConfirm = {
                showFinalConfirmAuthDialog = false
                viewModel.executeArchiveAndPrune(
                    impactSummary = summary,
                    userPassword = if (enableEncryption && archivePassword.isNotBlank()) archivePassword else null,
                    saveToDrive = saveToCloudDrive,
                    onComplete = { result ->
                        lastCompletedResult = result
                    }
                )
            },
            onDismiss = { showFinalConfirmAuthDialog = false }
        )
    }

    // IMPORT PREVIEW MODAL
    if (importPreview != null && pendingImportUri != null) {
        val preview = importPreview!!
        AlertDialog(
            onDismissRequest = {
                viewModel.clearImportPreview()
                pendingImportUri = null
            },
            icon = {
                Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Restore Fiscal Archive", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Archive Range: ${preview.metadata.dateRangeLabel}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Total Transactions: ${preview.totalTransactionsToRestore}", style = MaterialTheme.typography.bodySmall)
                    Text("New Transactions to Insert: ${preview.newTransactionsCount}", style = MaterialTheme.typography.bodySmall, color = SolidIncome, fontWeight = FontWeight.Bold)
                    if (preview.existingDuplicateCount > 0) {
                        Text("Duplicate Transactions (Skipped): ${preview.existingDuplicateCount}", style = MaterialTheme.typography.bodySmall, color = SolidExpense)
                    }
                    if (preview.affectedAccounts.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Opening Balance Reversals:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        preview.affectedAccounts.forEach { acc ->
                            Text("• ${acc.accountNameEn}: ৳${"%.2f".format(acc.currentInitialBalance)} → ৳${"%.2f".format(acc.newInitialBalance)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (preview.validationMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(preview.validationMessage, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmAuthDialog = true
                    },
                    enabled = preview.isValid
                ) {
                    Text("Restore Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearImportPreview()
                    pendingImportUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // IMPORT CONFIRMATION & AUTHENTICATION DIALOG
    if (showImportConfirmAuthDialog && pendingImportUri != null) {
        SecurityAuthDialog(
            title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ রিস্টোর নিশ্চিতকরণ" else "Authorize Archive Restoration",
            message = "Authenticate to restore archived transactions and adjust active starting balances.",
            confirmButtonText = "Restore Now",
            requiresAuth = true,
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { pin -> viewModel.verifySecurityPin(pin) },
            onConfirm = {
                showImportConfirmAuthDialog = false
                viewModel.executeImportArchiveFile(
                    uri = pendingImportUri!!,
                    onSuccess = {
                        pendingImportUri = null
                    }
                )
            },
            onDismiss = { showImportConfirmAuthDialog = false }
        )
    }
}

@Composable
private fun MetricBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun AccountImpactRow(
    impact: AffectedAccountImpact,
    languageMode: LanguageMode
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA && impact.accountNameBn.isNotBlank()) impact.accountNameBn else impact.accountNameEn,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = impact.accountType.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Starting Bal: ৳${"%.2f".format(impact.currentInitialBalance)} → ৳${"%.2f".format(impact.newInitialBalance)} (Offset: ${if (impact.netArchivedImpact >= 0) "+" else ""}৳${"%.2f".format(impact.netArchivedImpact)})",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SolidIncome.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "৳${"%.0f".format(impact.expectedTotalBalanceAfter)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SolidIncome,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SafetyCheckItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LocalArchiveFileCard(
    file: File,
    languageMode: LanguageMode,
    onRestore: () -> Unit,
    onShare: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val formattedDate = sdf.format(Date(file.lastModified()))
    val fileSizeKb = file.length() / 1024.0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(file.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$formattedDate • ${"%.1f".format(fileSizeKb)} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Button(
                    onClick = onRestore,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Restore", fontSize = 12.sp)
                }
            }
        }
    }
}
