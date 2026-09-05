package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BudgetAdjustment
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.RecurringBillWithDetails
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
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
import com.example.util.AutofillConfig
import com.example.util.AutofillPreferences
import com.example.util.BackupManager
import com.example.util.BackupPreferences
import com.example.util.BackupSettingsConfig
import com.example.util.CurrencyConfig
import com.example.util.CurrencyDisplayMode
import com.example.util.CurrencyItem
import com.example.util.CurrencyPreferences
import com.example.util.DataImportHelper
import com.example.util.DriveBackupResult
import com.example.util.GoogleDriveBackupFile
import com.example.util.GoogleDriveService
import com.example.util.AccountCalcConfig
import com.example.util.AccountCalculationPreferences
import com.example.util.AppTab
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.DashboardCardType
import com.example.util.DashboardConfig
import com.example.util.DashboardPreferences
import com.example.util.DisplayFormatConfig
import com.example.util.DisplayFormatPreferences
import com.example.util.ItemDisplayFormat
import com.example.util.NavigationTabConfig
import com.example.util.TabPosition
import com.example.util.TabPreferences
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import android.content.Context

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs: ThemePreferences = ThemePreferences.getInstance(application)
    private val appPrefs = application.getSharedPreferences("budgeter_app_prefs", Context.MODE_PRIVATE)
    private val backupPrefs: BackupPreferences = BackupPreferences.getInstance(application)
    private val tabPrefs: TabPreferences = TabPreferences.getInstance(application)
    private val accountCalcPrefs: AccountCalculationPreferences = AccountCalculationPreferences.getInstance(application)
    private val dashboardPrefs: DashboardPreferences = DashboardPreferences.getInstance(application)
    private val currencyPrefs: CurrencyPreferences = CurrencyPreferences.getInstance(application)
    private val displayFormatPrefs: DisplayFormatPreferences = DisplayFormatPreferences.getInstance(application)
    private val trashManager: com.example.util.TrashManager = com.example.util.TrashManager.getInstance(application)

    val tabConfig: StateFlow<NavigationTabConfig> = tabPrefs.config
    val accountCalcConfig: StateFlow<AccountCalcConfig> = accountCalcPrefs.config
    val dashboardConfig: StateFlow<DashboardConfig> = dashboardPrefs.config
    val currencyConfig: StateFlow<CurrencyConfig> = currencyPrefs.config
    val displayFormatConfig: StateFlow<DisplayFormatConfig> = displayFormatPrefs.config
    val trashedItems: StateFlow<List<com.example.util.TrashedItem>> = trashManager.trashedItems

    fun setItemDisplayFormat(format: ItemDisplayFormat) {
        displayFormatPrefs.setItemDisplayFormat(format)
    }

    fun setCurrency(currency: CurrencyItem) {
        currencyPrefs.setCurrency(currency)
    }

    fun setCurrencyDisplayMode(mode: CurrencyDisplayMode) {
        currencyPrefs.setDisplayMode(mode)
    }

    fun setCustomCurrency(code: String, symbol: String) {
        currencyPrefs.setCustomCurrency(code, symbol)
    }

    fun toggleDashboardCard(card: DashboardCardType, visible: Boolean) {
        dashboardPrefs.toggleCardVisibility(card, visible)
    }

    fun reorderDashboardCards(newOrder: List<DashboardCardType>) {
        dashboardPrefs.reorderCards(newOrder)
    }

    fun moveDashboardCard(fromIndex: Int, toIndex: Int) {
        dashboardPrefs.moveCard(fromIndex, toIndex)
    }

    fun setDailySummarySettings(
        mode: DailySummaryMode,
        period: DailySummaryPeriod,
        showValues: Boolean,
        showAverages: Boolean
    ) {
        dashboardPrefs.setDailySummarySettings(mode, period, showValues, showAverages)
    }

    fun setBudgetSummarySettings(
        shape: BudgetChartShape,
        categoryType: BudgetSummaryType,
        maxCategories: Int,
        showPercentages: Boolean,
        showTodayPace: Boolean
    ) {
        dashboardPrefs.setBudgetSummarySettings(shape, categoryType, maxCategories, showPercentages, showTodayPace)
    }

    fun setFavoriteAccounts(accountIds: Set<Long>) {
        dashboardPrefs.setFavoriteAccounts(accountIds)
    }

    fun toggleFavoriteAccount(accountId: Long) {
        dashboardPrefs.toggleFavoriteAccount(accountId)
    }

    fun setCalendarSettings(
        mode: CalendarDisplayMode,
        showIncome: Boolean,
        showExpense: Boolean
    ) {
        dashboardPrefs.setCalendarSettings(mode, showIncome, showExpense)
    }

    fun resetDashboardDefaults() {
        dashboardPrefs.resetToDefaults()
    }

    fun setAccountIncludeStatus(accountId: Long, isIncluded: Boolean) {
        accountCalcPrefs.setIncludeStatus(accountId, isIncluded)
    }

    fun setAccountAdjustment(accountId: Long, adjustment: Double) {
        accountCalcPrefs.setAdjustment(accountId, adjustment)
    }

    fun setAccountCalcSetting(accountId: Long, isIncluded: Boolean, adjustment: Double) {
        accountCalcPrefs.setSetting(accountId, isIncluded, adjustment)
    }

    fun resetAccountCalculation(accountId: Long) {
        accountCalcPrefs.resetAccount(accountId)
    }

    fun resetAllAccountCalculations() {
        accountCalcPrefs.resetAll()
    }

    fun setTabPosition(position: TabPosition) {
        tabPrefs.setPosition(position)
    }

    fun toggleTab(tab: AppTab, enabled: Boolean): Boolean {
        return tabPrefs.toggleTab(tab, enabled)
    }

    fun reorderTab(fromIndex: Int, toIndex: Int) {
        tabPrefs.reorderTab(fromIndex, toIndex)
    }

    fun resetTabDefaults() {
        tabPrefs.resetToDefaults()
    }

    // Demo Mode: Default is true so every feature gets rich demo data from now on!
    private val _isDemoMode = MutableStateFlow(appPrefs.getBoolean("app_is_demo_mode", true))
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private fun createRepository(isDemo: Boolean): BudgetRepository {
        val db = AppDatabase.getDatabase(getApplication(), viewModelScope, isDemoMode = isDemo)
        return BudgetRepository(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao(),
            recurringBillDao = db.recurringBillDao(),
            monthlyBudgetDao = db.monthlyBudgetDao(),
            budgetAdjustmentDao = db.budgetAdjustmentDao()
        )
    }

    private val _activeRepository = MutableStateFlow(createRepository(_isDemoMode.value))
    val activeRepo: BudgetRepository get() = _activeRepository.value

    fun setDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        appPrefs.edit().putBoolean("app_is_demo_mode", enabled).apply()
        val newRepo = createRepository(enabled)
        _activeRepository.value = newRepo
        viewModelScope.launch {
            newRepo.ensureOthersGroupIntegrity()
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            try {
                AppDatabase.resetDemoDatabase(getApplication(), viewModelScope)
                val newRepo = createRepository(isDemo = true)
                _activeRepository.value = newRepo
                _backupUiState.value = BackupUiState.Success("Demo data reset to fresh sample defaults!")
            } catch (e: Exception) {
                _backupUiState.value = BackupUiState.Error("Reset failed: ${e.localizedMessage}")
            }
        }
    }

    val themeConfig: StateFlow<AppThemeConfig> = themePrefs.themeConfig
    val backupSettingsConfig: StateFlow<BackupSettingsConfig> = backupPrefs.config

    fun setThemePalette(palette: ThemePalette) = themePrefs.setPalette(palette)
    fun setThemeMode(mode: ThemeMode) = themePrefs.setMode(mode)
    fun setColorIntensity(intensity: ColorIntensity) = themePrefs.setColorIntensity(intensity)
    fun setDynamicColor(enabled: Boolean) = themePrefs.setDynamicColor(enabled)
    fun setFontPreset(fontPreset: FontPreset) = themePrefs.setFontPreset(fontPreset)

    init {
        viewModelScope.launch {
            activeRepo.ensureOthersGroupIntegrity()
        }
    }

    private val initialCalendar = Calendar.getInstance()
    private val _selectedBudgetYear = MutableStateFlow(initialCalendar.get(Calendar.YEAR))
    val selectedBudgetYear: StateFlow<Int> = _selectedBudgetYear.asStateFlow()

    private val _selectedBudgetMonth = MutableStateFlow(initialCalendar.get(Calendar.MONTH) + 1)
    val selectedBudgetMonth: StateFlow<Int> = _selectedBudgetMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyBudgets: StateFlow<List<MonthlyBudget>> = combine(
        _selectedBudgetYear,
        _selectedBudgetMonth,
        _activeRepository
    ) { year, month, repo ->
        Triple(year, month, repo)
    }.flatMapLatest { (year, month, repo) ->
        repo.getMonthlyBudgets(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetAdjustments: StateFlow<List<BudgetAdjustment>> = combine(
        _selectedBudgetYear,
        _selectedBudgetMonth,
        _activeRepository
    ) { year, month, repo ->
        Triple(year, month, repo)
    }.flatMapLatest { (year, month, repo) ->
        repo.getBudgetAdjustments(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setBudgetYearMonth(year: Int, month: Int) {
        _selectedBudgetYear.value = year
        _selectedBudgetMonth.value = month.coerceIn(1, 12)
    }

    fun nextBudgetMonth() {
        var y = _selectedBudgetYear.value
        var m = _selectedBudgetMonth.value + 1
        if (m > 12) {
            m = 1
            y += 1
        }
        _selectedBudgetYear.value = y
        _selectedBudgetMonth.value = m
    }

    fun prevBudgetMonth() {
        var y = _selectedBudgetYear.value
        var m = _selectedBudgetMonth.value - 1
        if (m < 1) {
            m = 12
            y -= 1
        }
        _selectedBudgetYear.value = y
        _selectedBudgetMonth.value = m
    }

    fun saveMonthlyBudget(itemType: String, itemId: Long, amount: Double, isEnabled: Boolean = true) {
        viewModelScope.launch {
            val budget = MonthlyBudget(
                year = _selectedBudgetYear.value,
                month = _selectedBudgetMonth.value,
                itemType = itemType,
                itemId = itemId,
                budgetedAmount = amount,
                isEnabled = isEnabled,
                updatedAt = System.currentTimeMillis()
            )
            activeRepo.saveMonthlyBudget(budget)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveBudgetAdjustment(itemType: String, itemId: Long, newAmount: Double, isEnabled: Boolean = true, note: String = "") {
        viewModelScope.launch {
            activeRepo.saveBudgetAdjustment(
                year = _selectedBudgetYear.value,
                month = _selectedBudgetMonth.value,
                itemType = itemType,
                itemId = itemId,
                newAmount = newAmount,
                isEnabled = isEnabled,
                note = note
            )
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun resetBudgetToPrevious(itemType: String, itemId: Long) {
        viewModelScope.launch {
            activeRepo.resetBudgetToPrevious(
                year = _selectedBudgetYear.value,
                month = _selectedBudgetMonth.value,
                itemType = itemType,
                itemId = itemId
            )
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveMultipleMonthlyBudgets(budgets: List<MonthlyBudget>) {
        viewModelScope.launch {
            budgets.forEach { activeRepo.saveMonthlyBudget(it) }
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveCategoryAccountAllocations(
        categoryId: Long,
        allocations: Map<Long, Double>,
        year: Int = _selectedBudgetYear.value,
        month: Int = _selectedBudgetMonth.value
    ) {
        viewModelScope.launch {
            activeRepo.saveCategoryAccountAllocations(year, month, categoryId, allocations)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteCategoryAccountAllocation(
        categoryId: Long,
        accountId: Long,
        year: Int = _selectedBudgetYear.value,
        month: Int = _selectedBudgetMonth.value
    ) {
        viewModelScope.launch {
            activeRepo.deleteCategoryAccountAllocation(year, month, categoryId, accountId)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun copyBudgetsFromPreviousMonth() {
        viewModelScope.launch {
            var prevY = _selectedBudgetYear.value
            var prevM = _selectedBudgetMonth.value - 1
            if (prevM < 1) {
                prevM = 12
                prevY -= 1
            }
            activeRepo.copyBudgets(
                fromYear = prevY,
                fromMonth = prevM,
                toYear = _selectedBudgetYear.value,
                toMonth = _selectedBudgetMonth.value
            )
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val financialOverview: StateFlow<FinancialOverview> = _activeRepository
        .flatMapLatest { it.financialOverview }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = _activeRepository
        .flatMapLatest { it.accountsWithBalances }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allAccounts: StateFlow<List<Account>> = _activeRepository
        .flatMapLatest { it.allAccounts }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCategories: StateFlow<List<Category>> = _activeRepository
        .flatMapLatest { it.allCategories }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactionsWithDetails: StateFlow<List<TransactionWithDetails>> = _activeRepository
        .flatMapLatest { it.transactionsWithDetails }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val recurringBillsWithDetails: StateFlow<List<RecurringBillWithDetails>> = _activeRepository
        .flatMapLatest { it.recurringBillsWithDetails }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                activeRepo.insertTransaction(transaction)
            } else {
                activeRepo.updateTransaction(transaction)
            }
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun updateTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            activeRepo.updateTransactions(transactions)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            trashManager.addTransaction(transaction)
            activeRepo.deleteTransaction(transaction)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            transactions.forEach { trashManager.addTransaction(it) }
            activeRepo.deleteTransactions(transactions)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            if (account.id == 0L) {
                activeRepo.insertAccount(account)
            } else {
                activeRepo.updateAccount(account)
            }
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveAccounts(accounts: List<Account>) {
        viewModelScope.launch {
            activeRepo.updateAccounts(accounts)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun updateAccounts(accounts: List<Account>) = saveAccounts(accounts)

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            trashManager.addAccount(account)
            activeRepo.deleteAccount(account)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteAccounts(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.forEach { trashManager.addAccount(it) }
            activeRepo.deleteAccounts(accounts)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0L) {
                activeRepo.insertCategory(category)
            } else {
                activeRepo.updateCategory(category)
            }
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun saveCategories(categories: List<Category>) {
        viewModelScope.launch {
            activeRepo.updateCategories(categories)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun updateCategories(categories: List<Category>) = saveCategories(categories)

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            trashManager.addCategory(category)
            activeRepo.deleteCategory(category)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteCategories(categories: List<Category>) {
        viewModelScope.launch {
            categories.forEach { trashManager.addCategory(it) }
            activeRepo.deleteCategories(categories)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun restoreTrashedItem(item: com.example.util.TrashedItem) {
        viewModelScope.launch {
            try {
                when (item.type) {
                    com.example.util.TrashItemType.TRANSACTION -> {
                        val json = org.json.JSONObject(item.rawJsonData)
                        val tx = Transaction(
                            id = 0L,
                            type = TransactionType.valueOf(json.optString("type", TransactionType.EXPENSE.name)),
                            amount = json.optDouble("amount", 0.0),
                            dateEpochMs = json.optLong("dateEpochMs", System.currentTimeMillis()),
                            note = json.optString("note", ""),
                            payeeOrPayer = json.optString("payeeOrPayer", ""),
                            debitAccountId = if (json.has("debitAccountId") && !json.isNull("debitAccountId")) json.getLong("debitAccountId") else null,
                            creditAccountId = if (json.has("creditAccountId") && !json.isNull("creditAccountId")) json.getLong("creditAccountId") else null,
                            categoryId = if (json.has("categoryId") && !json.isNull("categoryId")) json.getLong("categoryId") else null
                        )
                        activeRepo.insertTransaction(tx)
                    }
                    com.example.util.TrashItemType.ACCOUNT -> {
                        val json = org.json.JSONObject(item.rawJsonData)
                        val acc = Account(
                            id = 0L,
                            nameEn = json.optString("nameEn", item.title),
                            nameBn = json.optString("nameBn", ""),
                            type = AccountType.valueOf(json.optString("type", AccountType.ASSET.name)),
                            parentId = if (json.has("parentId") && !json.isNull("parentId")) json.getLong("parentId") else null
                        )
                        activeRepo.insertAccount(acc)
                    }
                    com.example.util.TrashItemType.CATEGORY -> {
                        val json = org.json.JSONObject(item.rawJsonData)
                        val cat = Category(
                            id = 0L,
                            nameEn = json.optString("nameEn", item.title),
                            nameBn = json.optString("nameBn", ""),
                            type = CategoryType.valueOf(json.optString("type", CategoryType.EXPENSE.name)),
                            parentId = if (json.has("parentId") && !json.isNull("parentId")) json.getLong("parentId") else null
                        )
                        activeRepo.insertCategory(cat)
                    }
                }
                trashManager.removeItem(item.id)
                if (!_isDemoMode.value) {
                    SyncManager.triggerInstantJsonSync(getApplication())
                }
            } catch (e: Exception) {
                // If restore fails silently log
            }
        }
    }

    fun deleteTrashedItemPermanently(item: com.example.util.TrashedItem) {
        trashManager.removeItem(item.id)
    }

    fun emptyTrash() {
        trashManager.clearAll()
    }

    fun saveRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            if (bill.id == 0L) {
                activeRepo.insertRecurringBill(bill)
            } else {
                activeRepo.updateRecurringBill(bill)
            }
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            activeRepo.deleteRecurringBill(bill)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            activeRepo.payRecurringBill(bill)
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
        }
    }

    fun triggerInstantSync() {
        if (!_isDemoMode.value) {
            SyncManager.triggerInstantJsonSync(getApplication())
        }
    }

    fun trigger24hDatabaseBackup() {
        if (!_isDemoMode.value) {
            SyncManager.forceImmediateDatabaseBackup(getApplication())
        }
    }

    fun createLocalBackup(onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            try {
                val file = BackupManager.createLocalBackupFile(
                    context = getApplication(),
                    accountDao = activeRepo.accountDao,
                    categoryDao = activeRepo.categoryDao,
                    transactionDao = activeRepo.transactionDao,
                    recurringBillDao = activeRepo.recurringBillDao,
                    monthlyBudgetDao = activeRepo.monthlyBudgetDao
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
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao,
                recurringBillDao = activeRepo.recurringBillDao,
                monthlyBudgetDao = activeRepo.monthlyBudgetDao
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
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao,
                recurringBillDao = activeRepo.recurringBillDao,
                monthlyBudgetDao = activeRepo.monthlyBudgetDao
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
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao,
                recurringBillDao = activeRepo.recurringBillDao
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
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao,
                recurringBillDao = activeRepo.recurringBillDao
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

    // Quick Sync
    fun triggerQuickSync() {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            if (!_isDemoMode.value) {
                SyncManager.triggerInstantJsonSync(getApplication())
            }
            val now = System.currentTimeMillis()
            backupPrefs.recordSyncTimestamp(now)
            _backupUiState.value = BackupUiState.Success("QuickSync completed successfully")
        }
    }

    // Backup Settings Configuration
    fun setCloudProvider(provider: String) = backupPrefs.setCloudProvider(provider)
    fun setAccountLinked(linked: Boolean) = backupPrefs.setAccountLinked(linked)
    fun setLocalBackupDirectory(dir: String) = backupPrefs.setLocalBackupDirectory(dir)
    fun setAutoPhoneBackupEnabled(enabled: Boolean) = backupPrefs.setAutoPhoneBackupEnabled(enabled)
    fun setScheduledTime(hour: Int, minute: Int) = backupPrefs.setScheduledTime(hour, minute)
    fun setUploadAttachments(enabled: Boolean) = backupPrefs.setUploadAttachments(enabled)
    fun setAutoSyncData(enabled: Boolean) = backupPrefs.setAutoSyncData(enabled)
    fun setWifiOnly(enabled: Boolean) = backupPrefs.setWifiOnly(enabled)

    // Data Import: CSV (Excel)
    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = DataImportHelper.importCsv(
                context = getApplication(),
                uri = uri,
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao
            )
            result.onSuccess { count ->
                _backupUiState.value = BackupUiState.Success("Successfully imported $count transactions from CSV")
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("CSV Import failed: ${err.localizedMessage}")
            }
        }
    }

    // Data Import: QIF
    fun importFromQif(uri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = DataImportHelper.importQif(
                context = getApplication(),
                uri = uri,
                accountDao = activeRepo.accountDao,
                categoryDao = activeRepo.categoryDao,
                transactionDao = activeRepo.transactionDao
            )
            result.onSuccess { count ->
                _backupUiState.value = BackupUiState.Success("Successfully imported $count records from QIF file")
            }.onFailure { err ->
                _backupUiState.value = BackupUiState.Error("QIF Import failed: ${err.localizedMessage}")
            }
        }
    }

    // Reset All Data to Initial Defaults
    fun resetAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            activeRepo.resetDatabaseToDefaults()
            _backupUiState.value = BackupUiState.Success("All data reset to initial defaults successfully")
            onComplete()
        }
    }
}
