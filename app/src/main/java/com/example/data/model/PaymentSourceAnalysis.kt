package com.example.data.model

import com.example.util.LanguageHelper

enum class RequirementCalculationBasis(val key: String, val titleEn: String, val titleBn: String, val descEn: String, val descBn: String) {
    BUDGET_AMOUNT(
        "budget_amount",
        "Budget Amount",
        "পূর্ণ বাজেট ভিত্তিক",
        "Uses the full month's budgeted expenses and expected income.",
        "সম্পূর্ণ মাসের নির্ধারিত বাজেট এবং প্রত্যাশিত আয়ের ভিত্তিতে হিসাব।"
    ),
    REMAINING_AMOUNT(
        "remaining_amount",
        "Remaining Amount",
        "অবশিষ্ট বাজেট ভিত্তিক",
        "Uses only remaining/unsettled amounts after completed transactions.",
        "সম্পন্ন লেনদেন বাদ দিয়ে অবশিষ্ট বাকি খরচ এবং বকেয়া আয়ের ভিত্তিতে হিসাব।"
    );

    fun getTitle(mode: LanguageMode): String = when (mode) {
        LanguageMode.ENGLISH -> titleEn
        LanguageMode.BANGLA -> titleBn
        LanguageMode.BILINGUAL -> "$titleEn / $titleBn"
    }

    fun getDescription(mode: LanguageMode): String = when (mode) {
        LanguageMode.ENGLISH -> descEn
        LanguageMode.BANGLA -> descBn
        LanguageMode.BILINGUAL -> "$descEn ($descBn)"
    }
}

data class AccountRequirementItem(
    val title: String,
    val amount: Double,
    val originalBudgetOrExpected: Double,
    val actualSpentOrReceived: Double,
    val remaining: Double,
    val isRecurring: Boolean = false,
    val isExpense: Boolean = true,
    val categoryId: Long? = null,
    val iconName: String = "Category",
    val colorHex: String = "#EF4444"
)

data class AccountRequirementAnalysis(
    val account: Account,
    val currentBalance: Double,
    val requiredExpenseAmount: Double,
    val expectedIncomeAmount: Double,
    val availableAmount: Double, // currentBalance + expectedIncomeAmount
    val shortfall: Double,       // max(0.0, requiredExpenseAmount - availableAmount)
    val surplus: Double,         // max(0.0, availableAmount - requiredExpenseAmount)
    val itemizedExpenses: List<AccountRequirementItem> = emptyList(),
    val itemizedIncomes: List<AccountRequirementItem> = emptyList()
) {
    val isShortfall: Boolean get() = shortfall > 0.001
    val isSurplus: Boolean get() = surplus > 0.001
    val isBalanced: Boolean get() = !isShortfall && !isSurplus

    val fundingCoverageRatio: Float
        get() = if (requiredExpenseAmount <= 0.0) 1.0f else (availableAmount / requiredExpenseAmount).toFloat().coerceIn(0f, 2f)

    fun getActionMessage(mode: LanguageMode): String {
        val accName = account.localizedName(mode)
        return when {
            isShortfall -> {
                val formatted = LanguageHelper.formatCurrency(shortfall, mode)
                when (mode) {
                    LanguageMode.ENGLISH -> "Need $formatted more in $accName"
                    LanguageMode.BANGLA -> "$accName-এ আরও $formatted প্রয়োজন"
                    LanguageMode.BILINGUAL -> "Need $formatted more in $accName ($accName-এ আরও $formatted প্রয়োজন)"
                }
            }
            isSurplus -> {
                val formatted = LanguageHelper.formatCurrency(surplus, mode)
                when (mode) {
                    LanguageMode.ENGLISH -> "$formatted surplus in $accName"
                    LanguageMode.BANGLA -> "$accName-এ $formatted উদ্বৃত্ত রয়েছে"
                    LanguageMode.BILINGUAL -> "$formatted surplus in $accName ($accName-এ $formatted উদ্বৃত্ত)"
                }
            }
            else -> {
                when (mode) {
                    LanguageMode.ENGLISH -> "Balanced: Exact funds available in $accName"
                    LanguageMode.BANGLA -> "ভারসাম্যপূর্ণ: $accName-এ প্রয়োজনীয় তহবিল মজুদ আছে"
                    LanguageMode.BILINGUAL -> "Balanced in $accName (ভারসাম্যপূর্ণ)"
                }
            }
        }
    }
}

data class FundAllocationSuggestion(
    val fromAccount: Account,
    val toAccount: Account,
    val transferAmount: Double,
    val reasonEn: String,
    val reasonBn: String
) {
    fun getReason(mode: LanguageMode): String = when (mode) {
        LanguageMode.ENGLISH -> reasonEn
        LanguageMode.BANGLA -> reasonBn
        LanguageMode.BILINGUAL -> "$reasonEn / $reasonBn"
    }
}

data class PaymentSourceAnalysisOverview(
    val year: Int,
    val month: Int,
    val calculationBasis: RequirementCalculationBasis,
    val totalRequired: Double,
    val totalAvailable: Double,
    val totalShortfall: Double,
    val totalSurplus: Double,
    val accountsNeedingFundsCount: Int,
    val accountsWithSurplusCount: Int,
    val accountAnalyses: List<AccountRequirementAnalysis>,
    val transferSuggestions: List<FundAllocationSuggestion>
) {
    val netStatus: Double get() = totalAvailable - totalRequired
    val isOverallSurplus: Boolean get() = netStatus >= 0.0

    fun getSummaryAnswer(mode: LanguageMode): String {
        val totalReqStr = LanguageHelper.formatCurrency(totalRequired, mode)
        val totalAvailStr = LanguageHelper.formatCurrency(totalAvailable, mode)
        val shortStr = LanguageHelper.formatCurrency(totalShortfall, mode)
        val surpStr = LanguageHelper.formatCurrency(totalSurplus, mode)

        return if (accountsNeedingFundsCount == 0) {
            when (mode) {
                LanguageMode.ENGLISH -> "All payment sources are fully funded. Total required: $totalReqStr, Available: $totalAvailStr (Surplus: $surpStr)."
                LanguageMode.BANGLA -> "সকল পেমেন্ট সোর্সে পর্যাপ্ত তহবিল আছে। মোট প্রয়োজন: $totalReqStr, মজুদ: $totalAvailStr (উদ্বৃত্ত: $surpStr)।"
                LanguageMode.BILINGUAL -> "All accounts fully funded. Req: $totalReqStr, Avail: $totalAvailStr (Surplus: $surpStr)."
            }
        } else if (transferSuggestions.isNotEmpty() && totalSurplus >= totalShortfall) {
            when (mode) {
                LanguageMode.ENGLISH -> "$accountsNeedingFundsCount account(s) need $shortStr more, which can be fully covered by moving funds from surplus accounts."
                LanguageMode.BANGLA -> "$accountsNeedingFundsCount টি হিসাবে আরও $shortStr প্রয়োজন, যা উদ্বৃত্ত হিসাব থেকে স্থানান্তর করে পূরণ করা সম্ভব।"
                LanguageMode.BILINGUAL -> "$accountsNeedingFundsCount account(s) need $shortStr, which can be covered by transfers."
            }
        } else {
            when (mode) {
                LanguageMode.ENGLISH -> "Total shortage of $shortStr across your accounts. Additional external income or deposits are required this month."
                LanguageMode.BANGLA -> "হিসাবগুলোতে মোট $shortStr ঘাটতি রয়েছে। এই মাসে অতিরিক্ত আয় বা তহবিল জমা করা প্রয়োজন।"
                LanguageMode.BILINGUAL -> "Total shortfall of $shortStr. External deposits or income needed."
            }
        }
    }
}
