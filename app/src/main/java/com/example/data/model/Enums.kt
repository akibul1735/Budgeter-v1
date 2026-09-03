package com.example.data.model

enum class AccountType(val defaultDebitIsPositive: Boolean) {
    ASSET(true),       // Normal balance: Debit (Assets increase with debit)
    LIABILITY(false),   // Normal balance: Credit (Liabilities increase with credit)
    EQUITY(false),      // Normal balance: Credit (Equity increases with credit)
    INCOME(false),      // Normal balance: Credit
    EXPENSE(true)       // Normal balance: Debit
}

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class TransactionType {
    EXPENSE,  // Debit: Expense Category/Subcategory, Credit: Asset/Liability Account
    INCOME,   // Debit: Asset Account, Credit: Income Category/Subcategory
    TRANSFER  // Debit: Destination Asset Account, Credit: Source Asset Account
}

enum class TransactionStatus(val titleEn: String, val titleBn: String) {
    NONE("None", "কোনটি নয়"),
    CLEARED("Cleared", "সম্পন্ন"),
    VOID("Void", "বাতিল"),
    RECONCILED("Reconciled", "মিলিত")
}

enum class LanguageMode {
    ENGLISH,
    BANGLA,
    BILINGUAL
}
