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
import com.example.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BudgetRepository(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            recurringBillDao = db.recurringBillDao()
        )
    }

    private val _languageMode = MutableStateFlow(LanguageMode.BILINGUAL)
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    fun setLanguageMode(mode: LanguageMode) {
        _languageMode.value = mode
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
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.insertAccount(account)
            } else {
                repository.updateAccount(account)
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun saveRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            if (bill.id == 0L) {
                repository.insertRecurringBill(bill)
            } else {
                repository.updateRecurringBill(bill)
            }
        }
    }

    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
        }
    }

    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.payRecurringBill(bill)
        }
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
}
