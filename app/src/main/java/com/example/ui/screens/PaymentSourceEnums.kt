package com.example.ui.screens

enum class MainPaymentSourceTab {
    PAYMENT_SOURCES,
    ASSIGNED_ITEMS
}

enum class AssignedItemSectionFilter {
    ALL,
    OTHER_ACCOUNTS,
    EXPENSES,
    INCOMES
}

enum class AssignedItemStatusFilter {
    ALL,
    BUDGETED_ONLY,
    REMAINING_ONLY,
    MOST_FREQUENT,
    SPLIT_ONLY,
    UNASSIGNED_ONLY
}

enum class AccountStatusFilter {
    ALL,
    SHORTFALL_ONLY,
    SURPLUS_ONLY
}

enum class PaymentSourceSortOption(val titleEn: String, val titleBn: String) {
    DEFAULT("Default", "ডিফল্ট"),
    BALANCE_DESC("Highest Balance", "সর্বোচ্চ ব্যালেন্স"),
    BALANCE_ASC("Lowest Balance", "সর্বনিম্ন ব্যালেন্স"),
    REQUIRED_DESC("Highest Required", "সর্বোচ্চ প্রয়োজন"),
    SHORTFALL_DESC("Highest Shortfall", "সর্বোচ্চ ঘাটতি"),
    NAME_ASC("Name (A to Z)", "নাম (অ-হ / A-Z)")
}

enum class AssignedItemSortOption(val titleEn: String, val titleBn: String) {
    DEFAULT("Default", "ডিফল্ট"),
    BUDGET_DESC("Highest Budget / Target", "সর্বোচ্চ বাজেট / লক্ষ্য"),
    BUDGET_ASC("Lowest Budget / Target", "সর্বনিম্ন বাজেট / লক্ষ্য"),
    REMAINING_DESC("Highest Remaining", "সর্বোচ্চ অবশিষ্ট"),
    MOST_USED("Most Frequent", "সর্বাধিক ব্যবহৃত"),
    NAME_ASC("Name (A to Z)", "নাম (অ-হ / A-Z)")
}
