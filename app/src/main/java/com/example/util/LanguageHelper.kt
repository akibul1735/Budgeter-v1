package com.example.util

import com.example.data.model.LanguageMode
import java.text.DecimalFormat

object LanguageHelper {

    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatNumber(value: Double, mode: LanguageMode, includeDecimals: Boolean = true): String {
        val df = if (includeDecimals) DecimalFormat("#,##0.00") else DecimalFormat("#,##0")
        val formatted = df.format(value)
        return if (mode == LanguageMode.BANGLA) {
            toBanglaDigits(formatted)
        } else {
            formatted
        }
    }

    fun formatCurrency(amount: Double, mode: LanguageMode, currencySymbol: String = "৳"): String {
        val formattedNum = formatNumber(Math.abs(amount), mode)
        val symbol = when (mode) {
            LanguageMode.BANGLA -> "৳"
            LanguageMode.ENGLISH -> currencySymbol
            LanguageMode.BILINGUAL -> "৳"
        }
        return if (amount < 0) "-$symbol$formattedNum" else "$symbol$formattedNum"
    }

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            if (c in '0'..'9') {
                sb.append(bnDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            val idx = bnDigits.indexOf(c)
            if (idx != -1) {
                sb.append(idx)
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    // Core String Resource Dictionary
    fun getString(key: String, mode: LanguageMode): String {
        val entry = stringMap[key] ?: return key
        return when (mode) {
            LanguageMode.ENGLISH -> entry.en
            LanguageMode.BANGLA -> entry.bn
            LanguageMode.BILINGUAL -> "${entry.en} / ${entry.bn}"
        }
    }

    fun getLocalizedName(nameEn: String, nameBn: String, mode: LanguageMode): String {
        return when (mode) {
            LanguageMode.ENGLISH -> nameEn
            LanguageMode.BANGLA -> nameBn.ifEmpty { nameEn }
            LanguageMode.BILINGUAL -> if (nameBn.isNotEmpty()) "$nameEn / $nameBn" else nameEn
        }
    }

    data class Trans(val en: String, val bn: String)

    private val stringMap = mapOf(
        "app_name" to Trans("Budgeter", "বাজেটার"),
        "double_entry_bookkeeping" to Trans("Double-Entry Bookkeeping", "দ্বৈত দাখিলা হিসাব"),
        "dashboard" to Trans("Dashboard", "ড্যাশবোর্ড"),
        "accounts" to Trans("Accounts", "হিসাবসমূহ"),
        "categories" to Trans("Categories", "ক্যাটাগরি"),
        "ledger" to Trans("Ledger", "খতিয়ান"),
        "reports" to Trans("Reports", "প্রতিবেদন"),
        "net_worth" to Trans("Net Worth", "মোট সম্পদ"),
        "total_assets" to Trans("Total Assets", "মোট পরিসম্পদ"),
        "total_liabilities" to Trans("Total Liabilities", "মোট দায়"),
        "income" to Trans("Income", "আয়"),
        "expense" to Trans("Expense", "ব্যয়"),
        "transfer" to Trans("Transfer", "স্থানান্তর"),
        "expenses" to Trans("Expenses", "ব্যয়সমূহ"),
        "incomes" to Trans("Incomes", "আয়সমূহ"),
        "assets" to Trans("Assets", "সম্পদ"),
        "liabilities" to Trans("Liabilities", "দায় ও ঋণ"),
        "equity" to Trans("Equity", "মূলধন / ইকুইটি"),
        "sub_accounts" to Trans("Sub-Accounts", "উপ-হিসাবসমূহ"),
        "sub_categories" to Trans("Sub-Categories", "উপ-ক্যাটাগরি"),
        "add_transaction" to Trans("Add Transaction", "লেনদেন যোগ করুন"),
        "edit_transaction" to Trans("Edit Transaction", "লেনদেন সম্পাদনা"),
        "add_account" to Trans("Add Account", "হিসাব যোগ করুন"),
        "add_category" to Trans("Add Category", "ক্যাটাগরি যোগ করুন"),
        "add_sub_account" to Trans("Add Sub-Account", "উপ-হিসাব যোগ করুন"),
        "add_sub_category" to Trans("Add Sub-Category", "উপ-ক্যাটাগরি যোগ করুন"),
        "debit_account" to Trans("Debit Account (Inflow/Asset)", "ডেবিট হিসাব (বৃদ্ধি/সম্পদ)"),
        "credit_account" to Trans("Credit Account (Outflow/Payment)", "ক্রেডিট হিসাব (হ্রাস/পরিশোধ)"),
        "source_account" to Trans("From Account (Source)", "উৎস হিসাব (হতে)"),
        "destination_account" to Trans("To Account (Destination)", "গন্তব্য হিসাব (এ)"),
        "amount" to Trans("Amount", "পরিমাণ"),
        "date" to Trans("Date", "তারিখ"),
        "notes" to Trans("Notes / Memo", "নোট / বিবরণ"),
        "payee_payer" to Trans("Payee / Payer", "প্রাপক / প্রদানকারী"),
        "calculator" to Trans("Calculator", "ক্যালকুলেটর"),
        "quick_calc" to Trans("Quick Calculator", "দ্রুত ক্যালকুলেটর"),
        "done" to Trans("Done", "সম্পন্ন"),
        "cancel" to Trans("Cancel", "বাতিল"),
        "save" to Trans("Save", "সংরক্ষণ"),
        "delete" to Trans("Delete", "মুছুন"),
        "edit" to Trans("Edit", "সম্পাদনা"),
        "filter" to Trans("Filter", "ফিল্টার"),
        "search" to Trans("Search", "অনুসন্ধান"),
        "recent_transactions" to Trans("Recent Transactions", "সাম্প্রতিক লেনদেন"),
        "all_transactions" to Trans("All Transactions", "সকল লেনদেন"),
        "trial_balance" to Trans("Trial Balance", "রেওয়ামিল (Trial Balance)"),
        "balance_sheet" to Trans("Balance Sheet", "উদ্বৃত্ত পত্র (Balance Sheet)"),
        "income_statement" to Trans("Income & Expense Statement", "আয়-ব্যয় বিবরণী"),
        "debit" to Trans("Debit (Dr)", "ডেবিট (Dr)"),
        "credit" to Trans("Credit (Cr)", "ক্রেডিট (Cr)"),
        "balance" to Trans("Balance", "জের / ব্যালেন্স"),
        "balanced_ledger" to Trans("Double-Entry Balanced", "দ্বৈত দাখিলা সমন্বিত"),
        "unbalanced" to Trans("Unbalanced", "অসমন্বিত"),
        "today" to Trans("Today", "আজ"),
        "yesterday" to Trans("Yesterday", "গতকাল"),
        "this_month" to Trans("This Month", "চলতি মাস"),
        "all_time" to Trans("All Time", "সর্বকাল"),
        "select_category" to Trans("Select Category", "ক্যাটাগরি নির্বাচন করুন"),
        "select_account" to Trans("Select Account", "হিসাব নির্বাচন করুন"),
        "select_sub_category" to Trans("Select Sub-Category", "উপ-ক্যাটাগরি নির্বাচন"),
        "parent_account" to Trans("Parent Account", "মূল হিসাব"),
        "parent_category" to Trans("Parent Category", "মূল ক্যাটাগরি"),
        "name_en" to Trans("Name (English)", "নাম (ইংরেজি)"),
        "name_bn" to Trans("Name (Bangla)", "নাম (বাংলা)"),
        "initial_balance" to Trans("Opening Balance", "প্রারম্ভিক ব্যালেন্স"),
        "budget_limit" to Trans("Monthly Budget Limit", "মাসিক বাজেট সীমা"),
        "cashflow" to Trans("Cash Flow", "নগদ প্রবাহ"),
        "financial_summary" to Trans("Financial Summary", "আর্থিক সারসংক্ষেপ"),
        "no_transactions" to Trans("No transactions recorded yet.", "এখনো কোনো লেনদেন যুক্ত করা হয়নি।"),
        "no_accounts" to Trans("No accounts found.", "কোনো হিসাব পাওয়া যায়নি।"),
        "no_categories" to Trans("No categories found.", "কোনো ক্যাটাগরি পাওয়া যায়নি।"),
        "transactions" to Trans("Transactions", "লেনদেনসমূহ"),
        "main" to Trans("Main", "প্রধান"),
        "budget" to Trans("Budget", "বাজেট"),
        "budget_maker" to Trans("Budget Maker", "বাজেট মেকার"),
        "categories_and_budget" to Trans("Categories & Budget", "ক্যাটাগরি ও বাজেট"),
        "timeline" to Trans("Timeline", "টাইমলাইন"),
        "over" to Trans("over", "অতিরিক্ত"),
        "left_from" to Trans("left from", "অবশিষ্ট"),
        "frequency_weekly" to Trans("Weekly", "সাপ্তাহিক"),
        "frequency_bi_weekly" to Trans("Bi-weekly", "দ্বি-সাপ্তাহিক"),
        "frequency_monthly" to Trans("Monthly", "মাসিক"),
        "frequency_quarterly" to Trans("Quarterly", "ত্রৈমাসিক"),
        "frequency_yearly" to Trans("Yearly", "বাৎসরিক"),
        "prev_month" to Trans("Prev Month", "গত মাস"),
        "frequent" to Trans("Frequent", "প্রচলিত"),
        "average_3m" to Trans("3-Mo Avg", "৩ মাসের গড়"),
        "split" to Trans("Split", "বিভাজন"),
        "status" to Trans("Status", "অবস্থা"),
        "none" to Trans("None", "কোনটি নয়"),
        "label" to Trans("Label", "লেবেল"),
        "schedule" to Trans("Schedule", "সময়সূচি"),
        "cleared" to Trans("Cleared", "সম্পন্ন"),
        "void" to Trans("Void", "বাতিল"),
        "uncleared" to Trans("Uncleared", "অসম্পন্ন"),
        "reconciled" to Trans("Reconciled", "মিলকরণ"),
        "quick_add_consecutive" to Trans("Keep form open (+1)", "পরপর যুক্ত করুন (+১)"),
        "select_category_dialog" to Trans("Select Category", "ক্যাটাগরি নির্বাচন"),
        "select_account_dialog" to Trans("Select Account", "হিসাব নির্বাচন"),
        "export_csv" to Trans("Export CSV", "CSV এক্সপোর্ট"),
        "filter_transactions" to Trans("Filter Transactions", "লেনদেন ফিল্টার"),
        "all" to Trans("All", "সকল"),
        "autofill_settings" to Trans("Autofill Settings", "অটোফিল সেটিংস"),
        "autofill_desc" to Trans("Choose which fields auto-populate when selecting past transaction suggestions", "পূর্ববর্তী এন্ট্রির সাজেশন নির্বাচন করলে যা যা স্বয়ংক্রিয় পূরণ হবে"),
        "autofill_category" to Trans("Autofill Category & Sub-Category", "ক্যাটাগরি ও সাব-ক্যাটাগরি অটোফিল"),
        "autofill_account" to Trans("Autofill Account", "একাউন্ট / হিসাব অটোফিল"),
        "autofill_amount" to Trans("Autofill Amount", "টাকার পরিমাণ অটোফিল"),
        "autofill_notes" to Trans("Autofill Notes", "নোটস অটোফিল"),
        "autofill_labels" to Trans("Autofill Label / Tag", "লেবেল বা ট্যাগ অটোফিল"),
        "attachment" to Trans("Attachment", "সংযুক্তি"),
        "add_attachment" to Trans("Add Attachment", "সংযুক্তি যোগ করুন"),
        "remove_attachment" to Trans("Remove Attachment", "সংযুক্তি মুছুন"),
        "add_new_category" to Trans("+ Add Category", "+ নতুন ক্যাটাগরি"),
        "add_new_account" to Trans("+ Add Account", "+ নতুন একাউন্ট"),
        "add_new_label" to Trans("Add New Label", "নতুন লেবেল যুক্ত করুন"),
        "suggestions" to Trans("Suggestions", "পরামর্শসমূহ"),
        "language" to Trans("Language", "ভাষা"),
        "english" to Trans("English", "ইংরেজি"),
        "bangla" to Trans("বাংলা", "বাংলা"),
        "bilingual" to Trans("Bilingual (Both)", "উভয় ভাষা (Bilingual)"),
        "net_earnings" to Trans("Net Earnings", "নেট আয় ও লাভ"),
        "labels" to Trans("Labels", "লেবেলসমূহ"),
        "items_summary" to Trans("Items Summary", "আইটেম সামারি"),
        "reminders" to Trans("Reminders", "রিমাইন্ডার ও বিল"),
        "tab_customization" to Trans("Navigation Tabs", "ট্যাব কাস্টমাইজেশন"),
        "tab_position" to Trans("Tab Position", "ট্যাব অবস্থান"),
        "tab_position_top" to Trans("Top Bar", "উপরে"),
        "tab_position_bottom" to Trans("Bottom Bar", "নিচে"),
        "account_calculation" to Trans("Account Calculation", "অ্যাকাউন্ট হিসাব গণনা"),
        "include_in_calculation" to Trans("Include in Calculation", "হিসাবে অন্তর্ভুক্ত করুন"),
        "exclude_from_calculation" to Trans("Exclude from Calculation", "হিসাব থেকে বাদ দিন"),
        "included" to Trans("Included", "যুক্ত"),
        "excluded" to Trans("Excluded", "বাদ"),
        "adjust_calculation" to Trans("Adjust Amount", "অ্যামাউন্ট অ্যাডজাস্ট"),
        "effective_amount" to Trans("Effective Amount", "কার্যকর অ্যামাউন্ট"),
        "actual_balance" to Trans("Original Balance", "মূল ব্যালেন্স"),
        "adjustment_amount" to Trans("Adjustment", "অ্যাডজাস্টমেন্ট"),
        "calculated_net_worth" to Trans("Calculated Net Worth", "কার্যকর মোট হিসাব (Net Worth)"),
        "actual_net_worth" to Trans("Original Net Worth", "মূল মোট হিসাব"),
        "calculated_assets" to Trans("Calculated Assets", "কার্যকর সম্পদ"),
        "calculated_liabilities" to Trans("Calculated Liabilities", "কার্যকর দায়"),
        "calc_adjust_note" to Trans("Adjusted amount only applies to calculation; original account balance will not change.", "অ্যাডজাস্টকৃত অ্যামাউন্ট শুধু মোট হিসাবের জন্য প্রযোজ্য হবে; মূল অ্যাকাউন্ট ব্যালেন্স পরিবর্তন হবে না।"),
        "reset_calculation" to Trans("Reset All", "সব রিসেট"),
        "target_calc_balance" to Trans("Target Calculation Amount", "কাঙ্ক্ষিত কার্যকর পরিমাণ"),
        "quick_adjust" to Trans("Quick Adjustment", "দ্রুত অ্যাডজাস্ট"),
        "calculation_adjusted" to Trans("Adjusted for Calc", "হিসাবে অ্যাডজাস্টকৃত"),
        "expendable" to Trans("Expendable", "ব্যয়যোগ্য অর্থ"),
        "expected_expendable" to Trans("Expected Expendable", "প্রত্যাশিত ব্যয়যোগ্য অর্থ"),
        "budget_adjustment" to Trans("Budget Adjustment", "বাজেট সমন্বয়"),
        "previous_budget" to Trans("Previous Budget", "পূর্ববর্তী বাজেট"),
        "adjusted_budget" to Trans("Adjusted Budget", "সমন্বয়কৃত বাজেট"),
        "adjustment_history" to Trans("Adjustment History", "সমন্বয় ইতিহাস"),
        "remaining_expenses" to Trans("Remaining Expenses", "অবশিষ্ট ব্যয়"),
        "additional_cost" to Trans("Additional / Over Budget", "অতিরিক্ত খরচ"),
        "potential_income" to Trans("Potential Income", "সম্ভাব্য আয়"),
        "liabilities_change" to Trans("Liabilities Change", "দায় পরিবর্তন"),
        "increase" to Trans("Increase", "বৃদ্ধি"),
        "decrease" to Trans("Decrease", "হ্রাস"),
        "reset_to_previous" to Trans("Reset to Previous", "পূর্বের বাজেটে ফেরত যান"),
        "expendable_breakdown" to Trans("Expendable Breakdown", "ব্যয়যোগ্য অর্থের বিশ্লেষণ"),
        "financial_overview" to Trans("Financial Overview", "আর্থিক পর্যালোচনা"),
        "current_assets" to Trans("Current Assets", "বর্তমান সম্পদ"),
        "committed_expenses" to Trans("Committed Expenses", "নির্ধারিত ব্যয়"),
        "adjust_budget" to Trans("Adjust Budget", "বাজেট সমন্বয় করুন"),
        "daily_summary" to Trans("Daily Summary", "দৈনিক সারসংক্ষেপ"),
        "budget_summary" to Trans("Budget Summary", "বাজেট সারসংক্ষেপ"),
        "favorite_accounts" to Trans("Favorite Accounts", "পছন্দের হিসাবসমূহ"),
        "calendar_view" to Trans("Calendar View", "ক্যালেন্ডার ভিউ"),
        "calendar_summary" to Trans("Calendar Summary", "ক্যালেন্ডার সারসংক্ষেপ"),
        "customize_cards" to Trans("Customize Dashboard", "ড্যাশবোর্ড কাস্টমাইজ"),
        "customize_cards_subtitle" to Trans("Toggle cards visibility and change order", "কার্ড চালু/বন্ধ করুন ও ক্রম সাজান"),
        "daily_summary_settings" to Trans("Daily Summary Settings", "দৈনিক সারসংক্ষেপ সেটিংস"),
        "budget_chart_settings" to Trans("Budget Chart Settings", "বাজেট চার্ট সেটিংস"),
        "calendar_settings" to Trans("Calendar Settings", "ক্যালেন্ডার সেটিংস"),
        "display_mode" to Trans("Display Mode", "প্রদর্শন মোড"),
        "time_period" to Trans("Time Period", "সময়কাল"),
        "show_values_on_bars" to Trans("Show Values on Bars", "বারের উপর মান প্রদর্শন"),
        "show_period_averages" to Trans("Show Period Averages", "গড় পরিমাণ প্রদর্শন"),
        "chart_type" to Trans("Chart Type", "চার্টের ধরন"),
        "categories_filter" to Trans("Categories Filter", "ক্যাটাগরি ফিল্টার"),
        "show_slice_percentages" to Trans("Show Percentages on Chart", "চার্টে শতকরা হার দেখান"),
        "show_today_pace" to Trans("Show Today Pace Marker", "আজকের দিন নির্দেশক দেখান"),
        "display_style" to Trans("Display Style", "প্রদর্শনের ধরণ"),
        "show_income_badges" to Trans("Show Income", "আয় প্রদর্শন করুন"),
        "show_expense_badges" to Trans("Show Expense", "ব্যয় প্রদর্শন করুন"),
        "select_favorite_accounts" to Trans("Select Favorite Accounts", "পছন্দের হিসাব নির্বাচন"),
        "selected" to Trans("Selected", "নির্বাচিত"),
        "search_accounts" to Trans("Search accounts...", "হিসাব অনুসন্ধান..."),
        "select_all" to Trans("Select All", "সব নির্বাচন"),
        "clear_all" to Trans("Clear All", "সব মুছুন"),
        "select_accounts" to Trans("Select Accounts", "হিসাব নির্বাচন করুন"),
        "no_favorite_accounts" to Trans("No favorite accounts selected yet", "কোনো পছন্দের হিসাব নির্বাচিত নেই"),
        "reset_defaults" to Trans("Reset to Defaults", "পূর্বাবস্থায় ফেরান"),
        "apply" to Trans("Apply", "প্রয়োগ করুন"),
        "payment_source" to Trans("Payment Source", "পেমেন্ট সোর্স"),
        "payment_source_analysis" to Trans("Payment Source Requirement Analysis", "পেমেন্ট সোর্স প্রয়োজনীয়তা বিশ্লেষণ"),
        "payment_source_subtitle" to Trans("Account-based fund requirement & transfer insights", "অ্যাকাউন্টভিত্তিক তহবিল প্রয়োজনীয়তা ও স্থানান্তর বিশ্লেষণ"),
        "calculation_basis" to Trans("Calculation Basis", "গণনার ভিত্তি"),
        "budget_amount_basis" to Trans("Budget Amount", "পূর্ণ বাজেট ভিত্তিক"),
        "remaining_amount_basis" to Trans("Remaining Amount", "অবশিষ্ট বাজেট ভিত্তিক"),
        "required_amount" to Trans("Required Amount", "প্রয়োজনীয় অর্থ"),
        "available_amount" to Trans("Available Amount", "উপলব্ধ অর্থ"),
        "shortfall" to Trans("Shortfall", "ঘাটতি"),
        "surplus" to Trans("Surplus", "উদ্বৃত্ত"),
        "fund_allocation_insight" to Trans("Fund Allocation Insight", "তহবিল বণ্টন ও স্থানান্তর পরামর্শ"),
        "fund_allocation_subtitle" to Trans("Smart transfer suggestions to cover account shortages", "অ্যাকাউন্টের ঘাটতি মেটাতে স্মার্ট স্থানান্তর পরামর্শ"),
        "execute_transfer" to Trans("Execute Transfer", "স্থানান্তর করুন"),
        "move_funds" to Trans("Move Funds", "তহবিল স্থানান্তর"),
        "monthly_fund_summary" to Trans("Monthly Fund Summary", "মাসিক তহবিল সারসংক্ষেপ"),
        "accounts_need_funding" to Trans("Accounts Need Funding", "তহবিল প্রয়োজন এমন হিসাব"),
        "accounts_with_surplus" to Trans("Accounts with Surplus", "উদ্বৃত্ত হিসাব"),
        "all_funded" to Trans("All Accounts Fully Funded", "সকল হিসাবে পর্যাপ্ত তহবিল আছে"),
        "expand_breakdown" to Trans("View Itemized Breakdown", "বিস্তারিত বিবরণ দেখুন"),
        "hide_breakdown" to Trans("Hide Breakdown", "বিবরণ লুকান"),
        "itemized_expenses" to Trans("Assigned Expenses", "নির্ধারিত ব্যয়সমূহ"),
        "itemized_incomes" to Trans("Expected Incomes", "প্রত্যাশিত আয়সমূহ"),
        "current_balance" to Trans("Current Balance", "বর্তমান ব্যালেন্স"),
        "filter_all" to Trans("All", "সকল"),
        "filter_shortfall" to Trans("Shortfall Only", "শুধু ঘাটতি"),
        "filter_surplus" to Trans("Surplus Only", "শুধু উদ্বৃত্ত"),
        "filter_balanced" to Trans("Balanced", "ভারসাম্যপূর্ণ"),
        "no_accounts_match" to Trans("No accounts match the selected filter", "ফিল্টারের সাথে কোনো হিসাব মেলেনি"),
        "transfer_money" to Trans("Transfer Money", "টাকা স্থানান্তর"),
        "move" to Trans("Move", "স্থানান্তর"),
        "revert_expense_hint" to Trans("Revert / Decrease Expense (Refund)", "খরচ হ্রাস / রিভার্ট (রিফান্ড)"),
        "revert_income_hint" to Trans("Revert / Decrease Income (Deduction)", "আয় হ্রাস / রিভার্ট (কর্তন)"),
        "normal_expense_hint" to Trans("Normal Expense (−)", "স্বাভাবিক খরচ (−)"),
        "normal_income_hint" to Trans("Normal Income (+)", "স্বাভাবিক আয় (+)")
    )
}
