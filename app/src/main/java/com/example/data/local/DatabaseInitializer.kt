package com.example.data.local

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType

object DatabaseInitializer {

    suspend fun seedInitialData(
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
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

        // 2. Seed Sub-Accounts
        val cashWalletId = accountDao.insertAccount(
            Account(
                nameEn = "Wallet Cash",
                nameBn = "মানিব্যাগ নগদ",
                type = AccountType.ASSET,
                parentId = cashParentId,
                initialBalance = 3500.0,
                iconName = "Wallet",
                colorHex = "#10B981"
            )
        )
        val cashHomeId = accountDao.insertAccount(
            Account(
                nameEn = "Home Emergency Safe",
                nameBn = "ঘরের জরুরি নগদ",
                type = AccountType.ASSET,
                parentId = cashParentId,
                initialBalance = 10000.0,
                iconName = "Savings",
                colorHex = "#059669"
            )
        )
        val bankPrimaryId = accountDao.insertAccount(
            Account(
                nameEn = "Primary Checking Account",
                nameBn = "মূল চলতি / সঞ্চয়ী ব্যাংক হিসাব",
                type = AccountType.ASSET,
                parentId = bankParentId,
                initialBalance = 45000.0,
                iconName = "AccountBalance",
                colorHex = "#1E56A0"
            )
        )
        val bkashId = accountDao.insertAccount(
            Account(
                nameEn = "bKash Wallet",
                nameBn = "বিকাশ ওয়ালেট",
                type = AccountType.ASSET,
                parentId = mfsParentId,
                initialBalance = 4200.0,
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
                initialBalance = 2500.0,
                iconName = "PhoneIphone",
                colorHex = "#F37023"
            )
        )
        val creditCardId = accountDao.insertAccount(
            Account(
                nameEn = "Visa / Master Credit Card",
                nameBn = "ভিসা / মাস্টার ক্রেডিট কার্ড",
                type = AccountType.LIABILITY,
                parentId = creditParentId,
                initialBalance = 0.0,
                iconName = "CreditCard",
                colorHex = "#EF4444"
            )
        )
        val personalLoanId = accountDao.insertAccount(
            Account(
                nameEn = "Personal Loan / Borrowed",
                nameBn = "ব্যক্তিগত ঋণ / ধার",
                type = AccountType.LIABILITY,
                parentId = loanParentId,
                initialBalance = 0.0,
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
                nameEn = "Bonus & Incentives",
                nameBn = "বোনাস ও ইনসেন্টিভ",
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
                nameEn = "Freelance / Tech Projects",
                nameBn = "ফ্রিল্যান্স / টেক প্রজেক্ট",
                type = CategoryType.INCOME,
                parentId = catBusinessId,
                iconName = "Code",
                colorHex = "#3B82F6"
            )
        )

        val catInvestmentId = categoryDao.insertCategory(
            Category(
                nameEn = "Investments & Profit",
                nameBn = "বিনিয়োগ ও মুনাফা",
                type = CategoryType.INCOME,
                iconName = "TrendingUp",
                colorHex = "#8B5CF6"
            )
        )
        val subDividendId = categoryDao.insertCategory(
            Category(
                nameEn = "Dividends & Capital Gains",
                nameBn = "লভ্যাংশ ও মূলধনি লাভ",
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
                budgetLimit = 15000.0
            )
        )
        val subGroceriesId = categoryDao.insertCategory(
            Category(
                nameEn = "Bazar & Raw Groceries",
                nameBn = "কাঁচাবাজার ও নিত্যপ্রয়োজনীয় দ্রব্য",
                type = CategoryType.EXPENSE,
                parentId = catFoodId,
                iconName = "ShoppingCart",
                colorHex = "#EF4444"
            )
        )
        val subDiningId = categoryDao.insertCategory(
            Category(
                nameEn = "Dining Out & Restaurants",
                nameBn = "বাইরে খাওয়া ও রেস্তোরাঁ",
                type = CategoryType.EXPENSE,
                parentId = catFoodId,
                iconName = "DinnerDining",
                colorHex = "#F87171"
            )
        )
        val subSnacksId = categoryDao.insertCategory(
            Category(
                nameEn = "Tea, Snacks & Coffee",
                nameBn = "চা, নাস্তা ও কফি",
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
                budgetLimit = 20000.0
            )
        )
        val subRentId = categoryDao.insertCategory(
            Category(
                nameEn = "House / Apartment Rent",
                nameBn = "বাড়ি / ফ্ল্যাট ভাড়া",
                type = CategoryType.EXPENSE,
                parentId = catHousingId,
                iconName = "Apartment",
                colorHex = "#F59E0B"
            )
        )
        val subElectricityId = categoryDao.insertCategory(
            Category(
                nameEn = "Electricity & Power Bill",
                nameBn = "বিদ্যুৎ ও বিদ্যুৎ বিল",
                type = CategoryType.EXPENSE,
                parentId = catHousingId,
                iconName = "ElectricBolt",
                colorHex = "#FBBF24"
            )
        )
        val subInternetId = categoryDao.insertCategory(
            Category(
                nameEn = "WiFi & Mobile Internet",
                nameBn = "ওয়াইফাই ও মোবাইল ইন্টারনেট",
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
                budgetLimit = 5000.0
            )
        )
        val subBusMetroId = categoryDao.insertCategory(
            Category(
                nameEn = "Bus, Metro & Train Fare",
                nameBn = "বাস, মেট্রো ও ট্রেন ভাড়া",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "Train",
                colorHex = "#06B6D4"
            )
        )
        val subFuelId = categoryDao.insertCategory(
            Category(
                nameEn = "Fuel & Maintenance",
                nameBn = "জ্বালানি ও সার্ভিসিং",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "LocalGasStation",
                colorHex = "#22D3EE"
            )
        )
        val subRideshareId = categoryDao.insertCategory(
            Category(
                nameEn = "Uber, Pathao & CNG",
                nameBn = "উবার, পাঠাও ও সিএনজি",
                type = CategoryType.EXPENSE,
                parentId = catTransportId,
                iconName = "TwoWheeler",
                colorHex = "#67E8F9"
            )
        )

        val catHealthId = categoryDao.insertCategory(
            Category(
                nameEn = "Health & Medical",
                nameBn = "স্বাস্থ্য ও চিকিৎসা",
                type = CategoryType.EXPENSE,
                iconName = "LocalHospital",
                colorHex = "#EC4899",
                budgetLimit = 4000.0
            )
        )
        val subMedicineId = categoryDao.insertCategory(
            Category(
                nameEn = "Medicines & Pharmacy",
                nameBn = "ওষুধ ও ফার্মেসি",
                type = CategoryType.EXPENSE,
                parentId = catHealthId,
                iconName = "Medication",
                colorHex = "#EC4899"
            )
        )

        val catLifestyleId = categoryDao.insertCategory(
            Category(
                nameEn = "Shopping & Lifestyle",
                nameBn = "কেনাকাটা ও জীবনযাত্রা",
                type = CategoryType.EXPENSE,
                iconName = "ShoppingBag",
                colorHex = "#8B5CF6",
                budgetLimit = 6000.0
            )
        )
        val subClothesId = categoryDao.insertCategory(
            Category(
                nameEn = "Clothing & Accessories",
                nameBn = "পোশাক ও অনুষঙ্গ",
                type = CategoryType.EXPENSE,
                parentId = catLifestyleId,
                iconName = "Checkroom",
                colorHex = "#8B5CF6"
            )
        )

        // Seed "Others" Groups and Sub-Categories for Expense and Income
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
                nameEn = "Others",
                nameBn = "অন্যান্য",
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
                nameEn = "Others",
                nameBn = "অন্যান্য",
                type = CategoryType.INCOME,
                parentId = catOthersIncomeId,
                iconName = "MoreHoriz",
                colorHex = "#9E9E9E",
                isSystem = true
            )
        )

        // 5. Seed Initial Double-Entry Journal Transactions
        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // Transaction 1: Income (Salary credited to Bank Account)
        transactionDao.insertTransaction(
            Transaction(
                type = TransactionType.INCOME,
                amount = 55000.0,
                dateEpochMs = now - (oneDay * 2),
                debitAccountId = bankPrimaryId,
                creditAccountId = null,
                categoryId = catSalaryId,
                subCategoryId = subSalaryBaseId,
                note = "Monthly salary deposit / মাসিক বেতন জমা",
                payeeOrPayer = "Employer / কোম্পানি"
            )
        )

        // Transaction 2: Transfer (Transfer from Bank to bKash)
        transactionDao.insertTransaction(
            Transaction(
                type = TransactionType.TRANSFER,
                amount = 3000.0,
                dateEpochMs = now - (oneDay * 2) + 3600000L,
                debitAccountId = bkashId,
                creditAccountId = bankPrimaryId,
                note = "Add money to bKash / বিকাশ এ ব্যাংক থেকে ট্রান্সফার"
            )
        )

        // Transaction 3: Expense (Bazar / Groceries paid via Cash)
        transactionDao.insertTransaction(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1450.0,
                dateEpochMs = now - oneDay,
                debitAccountId = null,
                creditAccountId = cashWalletId,
                categoryId = catFoodId,
                subCategoryId = subGroceriesId,
                note = "Fresh vegetables, fish & eggs / কাঁচাবাজার ও মাছ",
                payeeOrPayer = "Local Market / বাজার"
            )
        )

        // Transaction 4: Expense (Internet bill paid via bKash)
        transactionDao.insertTransaction(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = 1000.0,
                dateEpochMs = now - (oneDay / 2),
                debitAccountId = null,
                creditAccountId = bkashId,
                categoryId = catHousingId,
                subCategoryId = subInternetId,
                note = "Monthly broadband fee / মাসিক ইন্টারনেট ফি",
                payeeOrPayer = "ISP Broadband"
            )
        )
    }
}
