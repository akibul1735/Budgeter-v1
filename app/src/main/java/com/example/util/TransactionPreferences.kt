package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NotesDisplayMode {
    FULL_TEXT, // Display full note text
    ICON_ONLY, // Display compact note indicator icon
    HIDDEN     // Do not display notes in list
}

enum class LabelsDisplayMode {
    CHIP_BADGE, // Display #tag chip badge
    ICON_ONLY,  // Display label tag icon
    HIDDEN      // Do not display labels in list
}

enum class UnnamedPayeeMode {
    DEFAULT_OTHERS, // Default to "Others" / "অন্যান্য"
    CATEGORY_NAME,  // Fallback to Category / Subcategory Name
    ACCOUNT_NAME,   // Fallback to Account Name
    CUSTOM_NAME     // Use custom text
}

data class TransactionConfig(
    // 1. Quick Date Picker & Show Time
    val enableQuickDatePicker: Boolean = true,
    val showTimePicker: Boolean = true,
    val showKeyboardImmediately: Boolean = true,

    // 2. Display of Notes & Labels in Transaction Lists
    val notesDisplayMode: NotesDisplayMode = NotesDisplayMode.FULL_TEXT,
    val labelsDisplayMode: LabelsDisplayMode = LabelsDisplayMode.CHIP_BADGE,

    // 3. Default Account and Categories
    val defaultAccountId: Long? = null,
    val defaultIncomeCategoryId: Long? = null,
    val defaultExpenseCategoryId: Long? = null,

    // 4. Enable Icons
    val enableCategoryIcons: Boolean = true,
    val enableAccountIcons: Boolean = true,

    // 5. Save and add another (+1): which parameters will be cleared/kept
    val plusOneClearAmount: Boolean = true,
    val plusOneClearNote: Boolean = true,
    val plusOneClearPayee: Boolean = true,
    val plusOneClearAttachment: Boolean = true,
    val plusOneKeepCategory: Boolean = true,
    val plusOneKeepAccount: Boolean = true,
    val plusOneKeepDate: Boolean = true,

    // 6. Payee or Name for Unnamed Transactions
    val unnamedPayeeMode: UnnamedPayeeMode = UnnamedPayeeMode.DEFAULT_OTHERS,
    val customDefaultPayee: String = "Others",

    // 7. Other enhancements
    val autoFocusAmount: Boolean = true,
    val enableQuickAmountPresets: Boolean = true
)

class TransactionPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_transaction_setup_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<TransactionConfig> = _config.asStateFlow()

    private fun loadConfig(): TransactionConfig {
        return TransactionConfig(
            enableQuickDatePicker = prefs.getBoolean(KEY_ENABLE_QUICK_DATE_PICKER, true),
            showTimePicker = prefs.getBoolean(KEY_SHOW_TIME_PICKER, true),
            showKeyboardImmediately = prefs.getBoolean(KEY_SHOW_KEYBOARD_IMMEDIATELY, true),
            notesDisplayMode = try {
                NotesDisplayMode.valueOf(prefs.getString(KEY_NOTES_DISPLAY_MODE, NotesDisplayMode.FULL_TEXT.name) ?: NotesDisplayMode.FULL_TEXT.name)
            } catch (e: Exception) {
                NotesDisplayMode.FULL_TEXT
            },
            labelsDisplayMode = try {
                LabelsDisplayMode.valueOf(prefs.getString(KEY_LABELS_DISPLAY_MODE, LabelsDisplayMode.CHIP_BADGE.name) ?: LabelsDisplayMode.CHIP_BADGE.name)
            } catch (e: Exception) {
                LabelsDisplayMode.CHIP_BADGE
            },
            defaultAccountId = if (prefs.contains(KEY_DEFAULT_ACCOUNT_ID)) prefs.getLong(KEY_DEFAULT_ACCOUNT_ID, -1L).takeIf { it != -1L } else null,
            defaultIncomeCategoryId = if (prefs.contains(KEY_DEFAULT_INCOME_CAT_ID)) prefs.getLong(KEY_DEFAULT_INCOME_CAT_ID, -1L).takeIf { it != -1L } else null,
            defaultExpenseCategoryId = if (prefs.contains(KEY_DEFAULT_EXPENSE_CAT_ID)) prefs.getLong(KEY_DEFAULT_EXPENSE_CAT_ID, -1L).takeIf { it != -1L } else null,
            enableCategoryIcons = prefs.getBoolean(KEY_ENABLE_CATEGORY_ICONS, true),
            enableAccountIcons = prefs.getBoolean(KEY_ENABLE_ACCOUNT_ICONS, true),
            plusOneClearAmount = prefs.getBoolean(KEY_PLUS_ONE_CLEAR_AMOUNT, true),
            plusOneClearNote = prefs.getBoolean(KEY_PLUS_ONE_CLEAR_NOTE, true),
            plusOneClearPayee = prefs.getBoolean(KEY_PLUS_ONE_CLEAR_PAYEE, true),
            plusOneClearAttachment = prefs.getBoolean(KEY_PLUS_ONE_CLEAR_ATTACHMENT, true),
            plusOneKeepCategory = prefs.getBoolean(KEY_PLUS_ONE_KEEP_CATEGORY, true),
            plusOneKeepAccount = prefs.getBoolean(KEY_PLUS_ONE_KEEP_ACCOUNT, true),
            plusOneKeepDate = prefs.getBoolean(KEY_PLUS_ONE_KEEP_DATE, true),
            unnamedPayeeMode = try {
                UnnamedPayeeMode.valueOf(prefs.getString(KEY_UNNAMED_PAYEE_MODE, UnnamedPayeeMode.DEFAULT_OTHERS.name) ?: UnnamedPayeeMode.DEFAULT_OTHERS.name)
            } catch (e: Exception) {
                UnnamedPayeeMode.DEFAULT_OTHERS
            },
            customDefaultPayee = prefs.getString(KEY_CUSTOM_DEFAULT_PAYEE, "Others") ?: "Others",
            autoFocusAmount = prefs.getBoolean(KEY_AUTO_FOCUS_AMOUNT, true),
            enableQuickAmountPresets = prefs.getBoolean(KEY_ENABLE_QUICK_AMOUNT_PRESETS, true)
        )
    }

    fun updateConfig(update: (TransactionConfig) -> TransactionConfig) {
        val newConfig = update(_config.value)
        val editor = prefs.edit()
            .putBoolean(KEY_ENABLE_QUICK_DATE_PICKER, newConfig.enableQuickDatePicker)
            .putBoolean(KEY_SHOW_TIME_PICKER, newConfig.showTimePicker)
            .putBoolean(KEY_SHOW_KEYBOARD_IMMEDIATELY, newConfig.showKeyboardImmediately)
            .putString(KEY_NOTES_DISPLAY_MODE, newConfig.notesDisplayMode.name)
            .putString(KEY_LABELS_DISPLAY_MODE, newConfig.labelsDisplayMode.name)
            .putBoolean(KEY_ENABLE_CATEGORY_ICONS, newConfig.enableCategoryIcons)
            .putBoolean(KEY_ENABLE_ACCOUNT_ICONS, newConfig.enableAccountIcons)
            .putBoolean(KEY_PLUS_ONE_CLEAR_AMOUNT, newConfig.plusOneClearAmount)
            .putBoolean(KEY_PLUS_ONE_CLEAR_NOTE, newConfig.plusOneClearNote)
            .putBoolean(KEY_PLUS_ONE_CLEAR_PAYEE, newConfig.plusOneClearPayee)
            .putBoolean(KEY_PLUS_ONE_CLEAR_ATTACHMENT, newConfig.plusOneClearAttachment)
            .putBoolean(KEY_PLUS_ONE_KEEP_CATEGORY, newConfig.plusOneKeepCategory)
            .putBoolean(KEY_PLUS_ONE_KEEP_ACCOUNT, newConfig.plusOneKeepAccount)
            .putBoolean(KEY_PLUS_ONE_KEEP_DATE, newConfig.plusOneKeepDate)
            .putString(KEY_UNNAMED_PAYEE_MODE, newConfig.unnamedPayeeMode.name)
            .putString(KEY_CUSTOM_DEFAULT_PAYEE, newConfig.customDefaultPayee)
            .putBoolean(KEY_AUTO_FOCUS_AMOUNT, newConfig.autoFocusAmount)
            .putBoolean(KEY_ENABLE_QUICK_AMOUNT_PRESETS, newConfig.enableQuickAmountPresets)

        if (newConfig.defaultAccountId != null) {
            editor.putLong(KEY_DEFAULT_ACCOUNT_ID, newConfig.defaultAccountId)
        } else {
            editor.remove(KEY_DEFAULT_ACCOUNT_ID)
        }

        if (newConfig.defaultIncomeCategoryId != null) {
            editor.putLong(KEY_DEFAULT_INCOME_CAT_ID, newConfig.defaultIncomeCategoryId)
        } else {
            editor.remove(KEY_DEFAULT_INCOME_CAT_ID)
        }

        if (newConfig.defaultExpenseCategoryId != null) {
            editor.putLong(KEY_DEFAULT_EXPENSE_CAT_ID, newConfig.defaultExpenseCategoryId)
        } else {
            editor.remove(KEY_DEFAULT_EXPENSE_CAT_ID)
        }

        editor.apply()
        _config.value = newConfig
    }

    companion object {
        private const val KEY_ENABLE_QUICK_DATE_PICKER = "enable_quick_date_picker"
        private const val KEY_SHOW_TIME_PICKER = "show_time_picker"
        private const val KEY_SHOW_KEYBOARD_IMMEDIATELY = "show_keyboard_immediately"
        private const val KEY_NOTES_DISPLAY_MODE = "notes_display_mode"
        private const val KEY_LABELS_DISPLAY_MODE = "labels_display_mode"
        private const val KEY_DEFAULT_ACCOUNT_ID = "default_account_id"
        private const val KEY_DEFAULT_INCOME_CAT_ID = "default_income_cat_id"
        private const val KEY_DEFAULT_EXPENSE_CAT_ID = "default_expense_cat_id"
        private const val KEY_ENABLE_CATEGORY_ICONS = "enable_category_icons"
        private const val KEY_ENABLE_ACCOUNT_ICONS = "enable_account_icons"
        private const val KEY_PLUS_ONE_CLEAR_AMOUNT = "plus_one_clear_amount"
        private const val KEY_PLUS_ONE_CLEAR_NOTE = "plus_one_clear_note"
        private const val KEY_PLUS_ONE_CLEAR_PAYEE = "plus_one_clear_payee"
        private const val KEY_PLUS_ONE_CLEAR_ATTACHMENT = "plus_one_clear_attachment"
        private const val KEY_PLUS_ONE_KEEP_CATEGORY = "plus_one_keep_category"
        private const val KEY_PLUS_ONE_KEEP_ACCOUNT = "plus_one_keep_account"
        private const val KEY_PLUS_ONE_KEEP_DATE = "plus_one_keep_date"
        private const val KEY_UNNAMED_PAYEE_MODE = "unnamed_payee_mode"
        private const val KEY_CUSTOM_DEFAULT_PAYEE = "custom_default_payee"
        private const val KEY_AUTO_FOCUS_AMOUNT = "auto_focus_amount"
        private const val KEY_ENABLE_QUICK_AMOUNT_PRESETS = "enable_quick_amount_presets"

        @Volatile
        private var instance: TransactionPreferences? = null

        fun getInstance(context: Context): TransactionPreferences {
            return instance ?: synchronized(this) {
                instance ?: TransactionPreferences(context).also { instance = it }
            }
        }
    }
}
