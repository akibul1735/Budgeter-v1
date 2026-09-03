package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.RecurringBill
import com.example.data.model.RecurringBillWithDetails
import com.example.data.model.Transaction
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.BudgetRepository
import com.example.data.repository.FinancialOverview
import com.example.sync.SyncManager
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.FontPreset
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.theme.ThemePreferences
import com.example.util.BackupManager
import com.example.util.DriveBackupResult
import com.example.util.GoogleDriveBackupFile
import com.example.util.GoogleDriveService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import android.content.Context

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    private val themePrefs: ThemePreferences = ThemePreferences.getInstance(application)
    private val appPrefs = application.getSharedPreferences("budgeter_app_prefs", Context.MODE_PRIVATE)

    val themeConfig: StateFlow<AppThemeConfig> = themePrefs.themeConfig

    fun setThemePalette(palette: ThemePalette) = themePrefs.setPalette(palette)
    fun setThemeMode(mode: ThemeMode) = themePrefs.setMode(mode)
    fun setColorIntensity(intensity: ColorIntensity) = themePrefs.setColorIntensity(intensity)
    fun setDynamicColor(enabled: Boolean) = themePrefs.setDynamicColor(enabled)
    fun setFontPreset(fontPreset: FontPreset) = themePrefs.setFontPreset(fontPreset)

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BudgetRepository(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            recurringBillDao = db.recurringBillDao()
        )
    }

    private val _languageMode = MutableStateFlow(loadLanguageMode())
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    private fun loadLanguageMode(): LanguageMode {
        val saved = appPrefs.getString("app_language_mode", LanguageMode.ENGLISH.name) ?: LanguageMode.ENGLISH.name
        return try {
            LanguageMode.valueOf(saved)
        } catch (_: Exception) {
            LanguageMode.ENGLISH
        }
    }

    fun setLanguageMode(mode: LanguageMode) {
        _languageMode.value = mode
        appPrefs.edit().putString("app_language_mode", mode.name).apply()
    }

    private val _backupUiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupUiState: StateFlow<BackupUiState> = _backupUiState.asStateFlow()

    fun clearBackupState() {
        _backupUiState.value = BackupUiState.Idle
    }

    val financialOverview: StateFlow<FinancialOverview> = repository.financialOverview
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinancialOverview(
                totalAssets = 0.0,
                totalLiabilities = 0.0,
                netWorth = 0.0,
                monthlyIncome = 0.0,
                monthlyExpense = 0.0,
                monthlyNetSavings = 0.0,
                totalDebits = 0.0,
                totalCredits = 0.0,
                isLedgerBalanced = true
            )
        )

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = repository.accountsWithBalances
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactionsWithDetails: StateFlow<List<TransactionWithDetails>> = repository.transactionsWithDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recurringBillsWithDetails: StateFlow<List<RecurringBillWithDetails>> = repository.recurringBillsWithDetails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.insertAccount(account)
            } else {
                repository.updateAccount(account)
            }
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun saveRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            if (bill.id == 0L) {
                repository.insertRecurringBill(bill)
            } else {
                repository.updateRecurringBill(bill)
            }
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.payRecurringBill(bill)
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun triggerInstantSync() {
        SyncManager.triggerInstantJsonSync(getApplication())
    }

    fun trigger24hDatabaseBackup() {
        SyncManager.forceImmediateDatabaseBackup(getApplication())
    }

    fun createLocalBackup(onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            try {
                val file = BackupManager.createLocalBackupFile(
                    context = getApplication(),
                    accountDao = repository.accountDao,
                    categoryDao = repository.categoryDao,
                    transactionDao = repository.transactionDao,
                    recurringBillDao = repository.recurringBillDao
                )
                _backupUiState.value = BackupUiState.Success("Backup created successfully: ${file.name}")
                onFileReady(file)
            } catch (e: Exception) {
                _backupUiState.value = BackupUiState.Error("Backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val success = BackupManager.exportBackupToUri(
                context = getApplication(),
                uri = uri,
                accountDao = repository.accountDao,
                categoryDao = repository.categoryDao,
                transactionDao = repository.transactionDao,
                recurringBillDao = repository.recurringBillDao
            )
            if (success) {
                _backupUiState.value = BackupUiState.Success("Backup exported successfully to storage")
            } else {
                _backupUiState.value = BackupUiState.Error("Export failed")
            }
        }
    }

    fun restoreBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = BackupManager.restoreBackupFromUri(
                context = getApplication(),
                uri = uri,
                accountDao = repository.accountDao,
                categoryDao = repository.categoryDao,
                transactionDao = repository.transactionDao,
                recurringBillDao = repository.recurringBillDao
            )
            result.onSuccess { count ->
                _backupUiState.value = BackupUiState.Success("Restored $count records successfully!")
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("Restore failed: ${err.localizedMessage}")
            }
        }
    }

    private val _driveBackups = MutableStateFlow<List<GoogleDriveBackupFile>>(emptyList())
    val driveBackups: StateFlow<List<GoogleDriveBackupFile>> = _driveBackups.asStateFlow()

    private val _signedInGoogleAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val signedInGoogleAccount: StateFlow<GoogleSignInAccount?> = _signedInGoogleAccount.asStateFlow()

    fun updateSignedInAccount(account: GoogleSignInAccount?) {
        _signedInGoogleAccount.value = account
        if (account != null) {
            fetchDriveBackups(account)
        } else {
            _driveBackups.value = emptyList()
        }
    }

    fun fetchDriveBackups(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val result = GoogleDriveService.listDriveBackups(getApplication(), account)
            result.onSuccess { list ->
                _driveBackups.value = list
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("Could not list Drive backups: ${err.localizedMessage}")
            }
        }
    }

    fun backupToGoogleDrive(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = GoogleDriveService.uploadBackupToDrive(
                context = getApplication(),
                account = account,
                accountDao = repository.accountDao,
                categoryDao = repository.categoryDao,
                transactionDao = repository.transactionDao,
                recurringBillDao = repository.recurringBillDao
            )
            result.onSuccess { driveRes ->
                _backupUiState.value = BackupUiState.Success("Database backed up to Google Drive (Visible 'Budgeter' folder & Hidden app folder)")
                fetchDriveBackups(account)
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("Google Drive backup failed: ${err.localizedMessage}")
            }
        }
    }

    fun restoreFromGoogleDrive(account: GoogleSignInAccount, backupFile: GoogleDriveBackupFile) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = GoogleDriveService.restoreFromDriveFile(
                context = getApplication(),
                account = account,
                fileId = backupFile.id,
                accountDao = repository.accountDao,
                categoryDao = repository.categoryDao,
                transactionDao = repository.transactionDao,
                recurringBillDao = repository.recurringBillDao
            )
            result.onSuccess { count ->
                _backupUiState.value = BackupUiState.Success("Successfully restored $count records from Google Drive!")
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("Restore from Drive failed: ${err.localizedMessage}")
            }
        }
    }

    fun deleteDriveBackup(account: GoogleSignInAccount, backupFile: GoogleDriveBackupFile) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val success = GoogleDriveService.deleteDriveBackup(getApplication(), account, backupFile.id)
            if (success) {
                _backupUiState.value = BackupUiState.Success("Drive backup deleted")
                fetchDriveBackups(account)
            } else {
                _backupUiState.value = BackupUiState.Error("Failed to delete Drive backup")
            }
        }
    }
}
