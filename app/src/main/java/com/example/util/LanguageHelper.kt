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
        val formattedNum = formatNumber(amount, mode)
        val symbol = when (mode) {
            LanguageMode.BANGLA -> "৳"
            LanguageMode.ENGLISH -> currencySymbol
            LanguageMode.BILINGUAL -> "৳"
        }
        return "$symbol$formattedNum"
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
        "split" to Trans("Split", "বিভাজন"),
        "status" to Trans("Status", "অবস্থা"),
        "label" to Trans("Label", "লেবেল"),
        "schedule" to Trans("Schedule", "সময়সূচি"),
        "cleared" to Trans("Cleared", "সম্পন্ন"),
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
        "bilingual" to Trans("Bilingual (Both)", "উভয় ভাষা (Bilingual)")
    )
}
