package com.example.data.local

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.BillStatus
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurrencePeriod
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import java.util.Calendar

object DemoDatabaseInitializer {

    suspend fun seedDemoData(
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao
    ) {
        if (accountDao.getAccountCount() > 0) return

        // 1. Seed Parent Accounts
        val cashParentId = accountDao.insertAccount(
            Account(
                nameEn = "Cash & Cash Equivalents",
                nameBn = "নগদ ও সমতুল্য",
                type = AccountType.ASSET,
                iconName = "Payments",
                colorHex = "#10B981",
                isSystem = true
            )
        )
        val bankParentId = accountDao.insertAccount(
            Account(
                nameEn = "Bank Accounts",
                nameBn = "ব্যাংক হিসাব",
                type = AccountType.ASSET,
                iconName = "AccountBalance",
                colorHex = "#1E56A0",
                isSystem = true
            )
        )
        val mfsParentId = accountDao.insertAccount(
            Account(
                nameEn = "Mobile Financial Services",
                nameBn = "মোবাইল ব্যাংকিং (MFS)",
                type = AccountType.ASSET,
                iconName = "PhoneAndroid",
                colorHex = "#D81B60",
                isSystem = true
            )
        )
        val creditParentId = accountDao.insertAccount(
            Account(
                nameEn = "Credit Cards & Overdrafts",
                nameBn = "ক্রেডিট কার্ড ও ওভারড্রাফট",
                type = AccountType.LIABILITY,
                iconName = "CreditCard",
                colorHex = "#EF4444",
                isSystem = true
            )
        )
        val loanParentId = accountDao.insertAccount(
            Account(
                nameEn = "Loans & Payable Debts",
                nameBn = "ঋণ ও দেয় দেনা",
                type = AccountType.LIABILITY,
                iconName = "MoneyOff",
                colorHex = "#F59E0B",
                isSystem = true
            )
        )
        val equityParentId = accountDao.insertAccount(
            Account(
                nameEn = "Owner's Equity / Opening Balance",
                nameBn = "মালিকানা স্বত্ব / প্রারম্ভিক উদ্বৃত্ত",
                type = AccountType.EQUITY,
                iconName = "AccountBalanceWallet",
                colorHex = "#8B5CF6",
                isSystem = true
            )
        )

        // 2. Seed Sub-Accounts with Rich Initial Balances
        val cashWalletId = accountDao.insertAccount(
            Account(
                nameEn = "Cash Wallet",
                nameBn = "মানিব্যাগ নগদ",
                type = AccountType.ASSET,
                parentId = cashParentId,
                initialBalance = 12500.0,
                iconName = "Wallet",
                colorHex = "#10B981"
            )
        )
        val cashHomeId = accountDao.insertAccount(
            Account(
                nameEn = "Emergency Cash Safe",
                nameBn = "জরুরি সঞ্চয় নগদ",
                type = AccountType.ASSET,
                parentId = cashParentId,
                initialBalance = 35000.0,
                iconName = "Savings",
                colorHex = "#059669"
            )
        )
        val bankSalaryId = accountDao.insertAccount(
            Account(
                nameEn = "BRAC Bank Salary A/C",
                nameBn = "ব্র্যাক ব্যাংক স্যালারি একাউন্ট",
                type = AccountType.ASSET,
                parentId = bankParentId,
                initialBalance = 82400.0,
                iconName = "AccountBalance",
                colorHex = "#1E56A0"
            )
        )
        val bankSavingsId = accountDao.insertAccount(
            Account(
                nameEn = "City Bank High Yield Savings",
                nameBn = "সিটি ব্যাংক সঞ্চয়ী হিসাব",
                type = AccountType.ASSET,
                parentId = bankParentId,
                initialBalance = 145000.0,
                iconName = "Savings",
                colorHex = "#3B82F6"
            )
        )
        val bkashId = accountDao.insertAccount(
            Account(
                nameEn = "bKash Personal Wallet",
                nameBn = "বিকাশ ব্যক্তিগত ওয়ালেট",
                type = AccountType.ASSET,
                parentId = mfsParentId,
                initialBalance = 7850.0,
                iconName = "PhoneIphone",
                colorHex = "#E2136E"
            )
        )
        val nagadId = accountDao.insertAccount(
            Account(
                nameEn = "Nagad Wallet",
                nameBn = "নগদ ওয়ালেট",
                type = AccountType.ASSET,
                parentId = mfsParentId,
                initialBalance = 4200.0,
                iconName = "PhoneIphone",
                colorHex = "#F37023"
            )
        )
        val creditCardId = accountDao.insertAccount(
            Account(
                nameEn = "City Maxx Visa Card",
                nameBn = "সিটি ম্যাক্স ভিসা কার্ড",
                type = AccountType.LIABILITY,
                parentId = creditParentId,
                initialBalance = 16500.0,
                iconName = "CreditCard",
                colorHex = "#EF4444"
            )
        )
        val carLoanId = accountDao.insertAccount(
            Account(
                nameEn = "Personal / Auto Loan",
                nameBn = "ব্যক্তিগত ও গাড়ি ঋণ",
                type = AccountType.LIABILITY,
                parentId = loanParentId,
                initialBalance = 120000.0,
                iconName = "Handshake",
                colorHex = "#F59E0B"
            )
        )

        // 3. Seed Income Categories & Sub-Categories
        val catSalaryId = categoryDao.insertCategory(
            Category(
                nameEn = "Salary & Wages",
                nameBn = "বেতন ও মজুরি",
                type = CategoryType.INCOME,
                iconName = "Work",
                colorHex = "#10B981"
            )
        )
        val subSalaryBaseId = categoryDao.insertCategory(
            Category(
                nameEn = "Monthly Base Salary",
                nameBn = "মাসিক মূল বেতন",
                type = CategoryType.INCOME,
                parentId = catSalaryId,
                iconName = "Payments",
                colorHex = "#10B981"
            )
        )
        val subSalaryBonusId = categoryDao.insertCategory(
            Category(
                nameEn = "Performance Bonus",
                nameBn = "পারফরম্যান্স বোনাস",
                type = CategoryType.INCOME,
                parentId = catSalaryId,
                iconName = "Stars",
                colorHex = "#059669"
            )
        )

        val catBusinessId = categoryDao.insertCategory(
            Category(
                nameEn = "Business & Freelance",
                nameBn = "ব্যবসা ও ফ্রিল্যান্সিং",
                type = CategoryType.INCOME,
                iconName = "LaptopMac",
                colorHex = "#3B82F6"
            )
        )
        val subFreelanceId = categoryDao.insertCategory(
            Category(
                nameEn = "Upwork & Remote Dev",
                nameBn = "আপওয়ার্ক ও রিমোট কাজ",
                type = CategoryType.INCOME,
                parentId = catBusinessId,
                iconName = "Code",
                colorHex = "#3B82F6"
            )
        )
        val subConsultingId = categoryDao.insertCategory(
            Category(
                nameEn = "Tech Consulting",
                nameBn = "টেক পরামর্শ ফি",
                type = CategoryType.INCOME,
                parentId = catBusinessId,
                iconName = "Handshake",
                colorHex = "#2563EB"
            )
        )

        val catInvestmentId = categoryDao.insertCategory(
            Category(
                nameEn = "Investments & Returns",
                nameBn = "বিনিয়োগ ও মুনাফা",
                type = CategoryType.INCOME,
                iconName = "TrendingUp",
                colorHex = "#8B5CF6"
            )
        )
        val subDividendId = categoryDao.insertCategory(
            Category(
                nameEn = "Dividends & Profit Share",
                nameBn = "ডিভিডেন্ড ও মুনাফা",
                type = CategoryType.INCOME,
                parentId = catInvestmentId,
                iconName = "ShowChart",
                colorHex = "#8B5CF6"
            )
        )

        // 4. Seed Expense Categories & Sub-Categories
        val catFoodId = categoryDao.insertCategory(
            Category(
                nameEn = "Food & Groceries",
                nameBn = "খাবার ও বাজার",
                type = CategoryType.EXPENSE,
                iconName = "Restaurant",
                colorHex = "#EF4444",
                budgetLimit = 22000.0
            )
        )
        val subGroceriesId = categoryDao.insertCategory(
            Category(
                nameEn = "Daily Bazar & Supermarket",
                nameBn = "দৈনিক কাঁচাবাজার ও সুপারশপ",
                type = CategoryType.EXPENSE,
                parentId = catFoodId,
                iconName = "ShoppingCart",
                colorHex = "#EF4444"
            )
        )
        val subDiningId = categoryDao.insertCategory(
            Category(
                nameEn = "Restaurants & Dining",
                nameBn = "রেস্তোরাঁ ও ডাইনিং",
                type = CategoryType.EXPENSE,
                parentId = catFoodId,
                iconName = "DinnerDining",
                colorHex = "#F87171"
            )
        )
        val subCoffeeId = categoryDao.insertCategory(
            Category(
                nameEn = "Tea, Snacks & Coffee",
                nameBn = "চা, নাস্তা ও ক্যাফে",
                type = CategoryType.EXPENSE,
                parentId = catFoodId,
                iconName = "LocalCafe",
                colorHex = "#FCA5A5"
            )
        )

        val catHousingId = categoryDao.insertCategory(
            Category(
                nameEn = "Housing & Utilities",
                nameBn = "বাসা ভাড়া ও ইউটিলিটি",
                type = CategoryType.EXPENSE,
                iconName = "Home",
                colorHex = "#F59E0B",
                budgetLimit = 32000.0
            )
        )
        val subRentId = categoryDao.insertCategory(
            Category(
                nameEn = "Apartment Rent",
                nameBn = "ফ্ল্যাট ভাড়া",
                type = CategoryType.EXPENSE,
                parentId = catHousingId,
                iconName = "Apartment",
                colorHex = "#F59E0B"
            )
        )
        val subElectricityId = categoryDao.insertCategory(
            Category(
                nameEn = "Electricity & Power Bill",
                nameBn = "বিদ্যুৎ বিল (DESCO)",
                type = CategoryType.EXPENSE,
                parentId = catHousingId,
                iconName = "ElectricBolt",
                colorHex = "#FBBF24"
            )
        )
        val subInternetId = categoryDao.insertCategory(
            Category(
                nameEn = "Broadband & Fiber WiFi",
                nameBn = "ব্রডব্যান্ড ও ওয়াইফাই ইন্টারনেট",
                type = CategoryType.EXPENSE,
                parentId = catHousingId,
                iconName = "Wifi",
                colorHex = "#FCD34D"
            )
        )

        val catTransportId = categoryDao.insertCategory(
            Category(
                nameEn = "Transportation",
                nameBn = "যাতায়াত ও পরিবহন",
                type = CategoryType.EXPENSE,
                iconName = "DirectionsCar",
                colorHex = "#06B6D4",
                budgetLimit = 7500.0
            )
        )
        val subMetroId = categoryDao.insertCategory(
            Category(
                nameEn = "Metro Rail & MRT Pass",
                nameBn = "মেট্রোরেল ও এমআরটি পাস",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "Train",
                colorHex = "#06B6D4"
            )
        )
        val subRideshareId = categoryDao.insertCategory(
            Category(
                nameEn = "Uber, Pathao & CNG",
                nameBn = "উবার, পাঠাও ও সিএনজি",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "TwoWheeler",
                colorHex = "#22D3EE"
            )
        )
        val subFuelId = categoryDao.insertCategory(
            Category(
                nameEn = "Octane Fuel & Maintenance",
                nameBn = "জ্বালানি ও মেইনটেন্যান্স",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "LocalGasStation",
                colorHex = "#67E8F9"
            )
        )

        val catLifestyleId = categoryDao.insertCategory(
            Category(
                nameEn = "Shopping & Lifestyle",
                nameBn = "কেনাকাটা ও লাইফস্টাইল",
                type = CategoryType.EXPENSE,
                iconName = "ShoppingBag",
                colorHex = "#8B5CF6",
                budgetLimit = 10000.0
            )
        )
        val subClothingId = categoryDao.insertCategory(
            Category(
                nameEn = "Apparel & Fashion",
                nameBn = "পোশাক ও ফ্যাশন",
                type = CategoryType.EXPENSE,
                parentId = catLifestyleId,
                iconName = "Checkroom",
                colorHex = "#8B5CF6"
            )
        )
        val subGadgetsId = categoryDao.insertCategory(
            Category(
                nameEn = "Gadgets & Accessories",
                nameBn = "গ্যাজেটস ও ইলেকট্রনিক্স",
                type = CategoryType.EXPENSE,
                parentId = catLifestyleId,
                iconName = "Devices",
                colorHex = "#A78BFA"
            )
        )

        val catHealthId = categoryDao.insertCategory(
            Category(
                nameEn = "Health & Wellness",
                nameBn = "স্বাস্থ্য ও ওষুধ",
                type = CategoryType.EXPENSE,
                iconName = "LocalHospital",
                colorHex = "#EC4899",
                budgetLimit = 5000.0
            )
        )
        val subPharmacyId = categoryDao.insertCategory(
            Category(
                nameEn = "Pharmacy & Medicines",
                nameBn = "ওষুধ ও ফার্মেসি",
                type = CategoryType.EXPENSE,
                parentId = catHealthId,
                iconName = "Medication",
                colorHex = "#EC4899"
            )
        )

        val catEntertainmentId = categoryDao.insertCategory(
            Category(
                nameEn = "Entertainment & Subs",
                nameBn = "বিনোদন ও সাবস্ক্রিপশন",
                type = CategoryType.EXPENSE,
                iconName = "Movie",
                colorHex = "#6366F1",
                budgetLimit = 4000.0
            )
        )
        val subStreamingId = categoryDao.insertCategory(
            Category(
                nameEn = "Streaming & Cinema",
                nameBn = "নেটফ্লিক্স, স্পটিফাই ও সিনেমা",
                type = CategoryType.EXPENSE,
                parentId = catEntertainmentId,
                iconName = "Theaters",
                colorHex = "#6366F1"
            )
        )

        // Seed "Others" Groups
        val catOthersExpenseId = categoryDao.insertCategory(
            Category(
                nameEn = "Others",
                nameBn = "অন্যান্য",
                type = CategoryType.EXPENSE,
                parentId = null,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        )
        categoryDao.insertCategory(
            Category(
                nameEn = "General",
                nameBn = "সাধারণ",
                type = CategoryType.EXPENSE,
                parentId = catOthersExpenseId,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        )

        val catOthersIncomeId = categoryDao.insertCategory(
            Category(
                nameEn = "Others",
                nameBn = "অন্যান্য",
                type = CategoryType.INCOME,
                parentId = null,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        )
        categoryDao.insertCategory(
            Category(
                nameEn = "General",
                nameBn = "সাধারণ",
                type = CategoryType.INCOME,
                parentId = catOthersIncomeId,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        )

        // 5. Seed Recurring Bills
        val cal = Calendar.getInstance()
        val curYear = cal.get(Calendar.YEAR)
        val curMonth = cal.get(Calendar.MONTH) + 1

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val rentDueMs = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 5)
        val wifiDueMs = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 15)
        val powerDueMs = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 22)
        val streamDueMs = cal.timeInMillis

        recurringBillDao.insertBill(
            RecurringBill(
                title = "Apartment Rent / বাসা ভাড়া",
                type = TransactionType.EXPENSE,
                amount = 26000.0,
                recurrencePeriod = RecurrencePeriod.MONTHLY,
                nextDueDateEpochMs = rentDueMs,
                creditAccountId = bankSalaryId,
                categoryId = catHousingId,
                subCategoryId = subRentId,
                status = BillStatus.PENDING,
                payeeOrPayer = "House Owner / বাড়িওয়ালা"
            )
        )
        recurringBillDao.insertBill(
            RecurringBill(
                title = "Dot Internet WiFi / ব্রডব্যান্ড",
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                recurrencePeriod = RecurrencePeriod.MONTHLY,
                nextDueDateEpochMs = wifiDueMs,
                creditAccountId = bkashId,
                categoryId = catHousingId,
                subCategoryId = subInternetId,
                status = BillStatus.PAID,
                payeeOrPayer = "Dot Internet Ltd."
            )
        )
        recurringBillDao.insertBill(
            RecurringBill(
                title = "DESCO Prepaid Electricity / বিদ্যুৎ বিল",
                type = TransactionType.EXPENSE,
                amount = 3500.0,
                recurrencePeriod = RecurrencePeriod.MONTHLY,
                nextDueDateEpochMs = powerDueMs,
                creditAccountId = bkashId,
                categoryId = catHousingId,
                subCategoryId = subElectricityId,
                status = BillStatus.PENDING,
                payeeOrPayer = "DESCO Prepaid"
            )
        )
        recurringBillDao.insertBill(
            RecurringBill(
                title = "Netflix & Spotify Subscription",
                type = TransactionType.EXPENSE,
                amount = 1650.0,
                recurrencePeriod = RecurrencePeriod.MONTHLY,
                nextDueDateEpochMs = streamDueMs,
                creditAccountId = creditCardId,
                categoryId = catEntertainmentId,
                subCategoryId = subStreamingId,
                status = BillStatus.PENDING,
                payeeOrPayer = "Netflix Digital"
            )
        )

        // 6. Seed Monthly Budgets for Categories
        val defaultBudgets = listOf(
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catFoodId, budgetedAmount = 22000.0),
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catHousingId, budgetedAmount = 32000.0),
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catTransportId, budgetedAmount = 7500.0),
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catLifestyleId, budgetedAmount = 10000.0),
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catHealthId, budgetedAmount = 5000.0),
            MonthlyBudget(year = curYear, month = curMonth, itemType = "EXPENSE", itemId = catEntertainmentId, budgetedAmount = 4000.0)
        )
        defaultBudgets.forEach { monthlyBudgetDao.upsertBudget(it) }

        // Also seed previous month budget
        val prevYear = if (curMonth == 1) curYear - 1 else curYear
        val prevMonth = if (curMonth == 1) 12 else curMonth - 1
        val prevBudgets = listOf(
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catFoodId, budgetedAmount = 20000.0),
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catHousingId, budgetedAmount = 30000.0),
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catTransportId, budgetedAmount = 7000.0),
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catLifestyleId, budgetedAmount = 8000.0),
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catHealthId, budgetedAmount = 4000.0),
            MonthlyBudget(year = prevYear, month = prevMonth, itemType = "EXPENSE", itemId = catEntertainmentId, budgetedAmount = 3500.0)
        )
        prevBudgets.forEach { monthlyBudgetDao.upsertBudget(it) }

        // 7. Seed 30+ Realistic Transactions (Expenses, Incomes, Transfers with Tags)
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        val demoTransactions = listOf(
            // --- Current Month Incomes ---
            Transaction(
                type = TransactionType.INCOME,
                amount = 95000.0,
                dateEpochMs = now - (dayMs * 2),
                debitAccountId = bankSalaryId,
                creditAccountId = null,
                categoryId = catSalaryId,
                subCategoryId = subSalaryBaseId,
                note = "Monthly salary direct deposit #salary #office",
                payeeOrPayer = "Brain Station 23 / ব্রেন স্টেশন",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.INCOME,
                amount = 32500.0,
                dateEpochMs = now - (dayMs * 8),
                debitAccountId = bankSalaryId,
                creditAccountId = null,
                categoryId = catBusinessId,
                subCategoryId = subFreelanceId,
                note = "Upwork React Native milestone payout #freelance #dev #tech",
                payeeOrPayer = "Upwork Escrow / আপওয়ার্ক",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.INCOME,
                amount = 8000.0,
                dateEpochMs = now - (dayMs * 14),
                debitAccountId = bkashId,
                creditAccountId = null,
                categoryId = catInvestmentId,
                subCategoryId = subDividendId,
                note = "Quarterly stock dividend profit #invest #dividends",
                payeeOrPayer = "Beximco Pharma / বেক্সিমকো",
                status = TransactionStatus.CLEARED
            ),

            // --- Transfers Between Accounts ---
            Transaction(
                type = TransactionType.TRANSFER,
                amount = 15000.0,
                dateEpochMs = now - (dayMs * 2) + 7200000L,
                debitAccountId = bkashId,
                creditAccountId = bankSalaryId,
                note = "Fund transfer to bKash wallet #transfer #bkash",
                payeeOrPayer = "Self Transfer"
            ),
            Transaction(
                type = TransactionType.TRANSFER,
                amount = 10000.0,
                dateEpochMs = now - (dayMs * 5),
                debitAccountId = cashWalletId,
                creditAccountId = bankSalaryId,
                note = "ATM cash withdrawal for weekly bazar #cash #atm",
                payeeOrPayer = "BRAC Bank ATM"
            ),
            Transaction(
                type = TransactionType.TRANSFER,
                amount = 5000.0,
                dateEpochMs = now - (dayMs * 11),
                debitAccountId = nagadId,
                creditAccountId = bankSalaryId,
                note = "Add money to Nagad wallet #transfer #nagad",
                payeeOrPayer = "Self Transfer"
            ),

            // --- Expenses: Food & Groceries ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 3850.0,
                dateEpochMs = now - (dayMs * 1),
                creditAccountId = cashWalletId,
                categoryId = catFoodId,
                subCategoryId = subGroceriesId,
                note = "Fresh fish, chicken, beef & seasonal veggies #groceries #bazar #daily",
                payeeOrPayer = "Kawran Bazar / কারওয়ান বাজার",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 2650.0,
                dateEpochMs = now - (dayMs * 3),
                creditAccountId = bkashId,
                categoryId = catFoodId,
                subCategoryId = subGroceriesId,
                note = "Monthly household grocery & toiletries #groceries #shwapno #household",
                payeeOrPayer = "Shwapno Supermarket / স্বপ্ন",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1850.0,
                dateEpochMs = now - (dayMs * 4),
                creditAccountId = creditCardId,
                categoryId = catFoodId,
                subCategoryId = subDiningId,
                note = "Weekend dinner with colleagues #dining #dinner #office",
                payeeOrPayer = "Takeout Burger / টেকআউট",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 320.0,
                dateEpochMs = now - (dayMs * 1) + 14400000L,
                creditAccountId = cashWalletId,
                categoryId = catFoodId,
                subCategoryId = subCoffeeId,
                note = "Evening coffee & bakery snacks #coffee #snacks #tea",
                payeeOrPayer = "North End Coffee / কফি",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 210.0,
                dateEpochMs = now - (dayMs * 6),
                creditAccountId = cashWalletId,
                categoryId = catFoodId,
                subCategoryId = subCoffeeId,
                note = "Afternoon cha and singara with friends #tea #snacks",
                payeeOrPayer = "Tong Chai / টং দোকান",
                status = TransactionStatus.CLEARED
            ),

            // --- Expenses: Housing & Utilities ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 26000.0,
                dateEpochMs = now - (dayMs * 10),
                creditAccountId = bankSalaryId,
                categoryId = catHousingId,
                subCategoryId = subRentId,
                note = "Apartment monthly rent paid #rent #home #housing",
                payeeOrPayer = "House Owner / বাড়িওয়ালা",
                status = TransactionStatus.RECONCILED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                dateEpochMs = now - (dayMs * 7),
                creditAccountId = bkashId,
                categoryId = catHousingId,
                subCategoryId = subInternetId,
                note = "High speed broadband WiFi bill #wifi #internet #bills",
                payeeOrPayer = "Dot Internet / ডট ইন্টারনেট",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 3150.0,
                dateEpochMs = now - (dayMs * 12),
                creditAccountId = bkashId,
                categoryId = catHousingId,
                subCategoryId = subElectricityId,
                note = "DESCO prepaid electric meter recharge #power #electricity #bills",
                payeeOrPayer = "DESCO Electric / ডেসকো",
                status = TransactionStatus.CLEARED
            ),

            // --- Expenses: Transportation ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1000.0,
                dateEpochMs = now - (dayMs * 3) + 3600000L,
                creditAccountId = bkashId,
                categoryId = catTransportId,
                subCategoryId = subMetroId,
                note = "MRT Pass card balance top-up #metro #mrt #commute",
                payeeOrPayer = "Dhaka Metro Rail / মেট্রোরেল",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 480.0,
                dateEpochMs = now - (dayMs * 2) + 18000000L,
                creditAccountId = bkashId,
                categoryId = catTransportId,
                subCategoryId = subRideshareId,
                note = "Pathao bike ride to Dhanmondi meeting #transport #pathao #ride",
                payeeOrPayer = "Pathao Rides / পাঠাও",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 750.0,
                dateEpochMs = now - (dayMs * 9),
                creditAccountId = creditCardId,
                categoryId = catTransportId,
                subCategoryId = subRideshareId,
                note = "Uber Premier ride during rain #uber #ride #commute",
                payeeOrPayer = "Uber Bangladesh / উবার",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 2500.0,
                dateEpochMs = now - (dayMs * 13),
                creditAccountId = creditCardId,
                categoryId = catTransportId,
                subCategoryId = subFuelId,
                note = "Octane refuel for motorbike #fuel #octane #bike",
                payeeOrPayer = "Trust Filling Station / ফিলিং স্টেশন",
                status = TransactionStatus.CLEARED
            ),

            // --- Expenses: Shopping & Lifestyle ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 4500.0,
                dateEpochMs = now - (dayMs * 5) + 10800000L,
                creditAccountId = creditCardId,
                categoryId = catLifestyleId,
                subCategoryId = subClothingId,
                note = "Cotton panjabi & formal shirts #shopping #fashion #aarong",
                payeeOrPayer = "Aarong / আড়ং",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 2300.0,
                dateEpochMs = now - (dayMs * 14),
                creditAccountId = nagadId,
                categoryId = catLifestyleId,
                subCategoryId = subGadgetsId,
                note = "USB-C fast charger and braided cable #gadget #electronics #daraz",
                payeeOrPayer = "Daraz Online / দারাজ",
                status = TransactionStatus.CLEARED
            ),

            // --- Expenses: Health & Wellness ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1450.0,
                dateEpochMs = now - (dayMs * 4) + 7200000L,
                creditAccountId = cashWalletId,
                categoryId = catHealthId,
                subCategoryId = subPharmacyId,
                note = "Monthly vitamins & prescription medicines #medical #pharmacy #health",
                payeeOrPayer = "Lazz Pharma / লাজ ফার্মা",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                dateEpochMs = now - (dayMs * 11) + 14400000L,
                creditAccountId = creditCardId,
                categoryId = catHealthId,
                subCategoryId = subPharmacyId,
                note = "First aid and dental care supplies #health #care",
                payeeOrPayer = "Square Hospital Pharma / স্কয়ার",
                status = TransactionStatus.CLEARED
            ),

            // --- Expenses: Entertainment ---
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1650.0,
                dateEpochMs = now - (dayMs * 6) + 18000000L,
                creditAccountId = creditCardId,
                categoryId = catEntertainmentId,
                subCategoryId = subStreamingId,
                note = "IMAX 3D tickets with popcorn combo #movie #cinema #cineplex",
                payeeOrPayer = "Star Cineplex / স্টার সিনেপ্লেক্স",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                dateEpochMs = now - (dayMs * 15),
                creditAccountId = creditCardId,
                categoryId = catEntertainmentId,
                subCategoryId = subStreamingId,
                note = "Monthly Netflix 4K UHD subscription #netflix #streaming #sub",
                payeeOrPayer = "Netflix / নেটফ্লিক্স",
                status = TransactionStatus.CLEARED
            ),

            // --- Historical Transactions (Last Month) ---
            Transaction(
                type = TransactionType.INCOME,
                amount = 95000.0,
                dateEpochMs = now - (dayMs * 34),
                debitAccountId = bankSalaryId,
                creditAccountId = null,
                categoryId = catSalaryId,
                subCategoryId = subSalaryBaseId,
                note = "Last month base salary #salary #office",
                payeeOrPayer = "Brain Station 23 / ব্রেন স্টেশন",
                status = TransactionStatus.RECONCILED
            ),
            Transaction(
                type = TransactionType.INCOME,
                amount = 15000.0,
                dateEpochMs = now - (dayMs * 36),
                debitAccountId = bankSalaryId,
                creditAccountId = null,
                categoryId = catSalaryId,
                subCategoryId = subSalaryBonusId,
                note = "Project completion incentive bonus #bonus #salary",
                payeeOrPayer = "Brain Station 23 / ব্রেন স্টেশন",
                status = TransactionStatus.RECONCILED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 26000.0,
                dateEpochMs = now - (dayMs * 35),
                creditAccountId = bankSalaryId,
                categoryId = catHousingId,
                subCategoryId = subRentId,
                note = "Last month house rent #rent #home",
                payeeOrPayer = "House Owner / বাড়িওয়ালা",
                status = TransactionStatus.RECONCILED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 5400.0,
                dateEpochMs = now - (dayMs * 38),
                creditAccountId = creditCardId,
                categoryId = catLifestyleId,
                subCategoryId = subClothingId,
                note = "Seasonal clothing and shoes #shopping #fashion #yellow",
                payeeOrPayer = "Yellow Clothing / ইয়োলো",
                status = TransactionStatus.CLEARED
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 4200.0,
                dateEpochMs = now - (dayMs * 40),
                creditAccountId = cashWalletId,
                categoryId = catFoodId,
                subCategoryId = subGroceriesId,
                note = "Monthly dry bazar, rice and spices #groceries #bazar",
                payeeOrPayer = "Mina Bazar / মীনা বাজার",
                status = TransactionStatus.CLEARED
            )
        )

        demoTransactions.forEach { transactionDao.insertTransaction(it) }
    }
}
