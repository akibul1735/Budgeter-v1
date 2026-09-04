package com.example.util

import android.content.Context
import android.content.SharedPreferences

class TransferFeePreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_transfer_fee_prefs", Context.MODE_PRIVATE)

    fun getFeeCategoryForPayee(payee: String): Long? {
        val trimmed = payee.trim().lowercase()
        if (trimmed.isNotEmpty() && prefs.contains("fee_cat_payee_$trimmed")) {
            return prefs.getLong("fee_cat_payee_$trimmed", -1L).takeIf { it > 0L }
        }
        val defaultCat = prefs.getLong("fee_cat_default", -1L)
        return if (defaultCat > 0L) defaultCat else null
    }

    fun setFeeCategoryForPayee(payee: String, categoryId: Long, subCategoryId: Long? = null) {
        val editor = prefs.edit()
        val trimmed = payee.trim().lowercase()
        if (trimmed.isNotEmpty()) {
            editor.putLong("fee_cat_payee_$trimmed", categoryId)
            if (subCategoryId != null) {
                editor.putLong("fee_subcat_payee_$trimmed", subCategoryId)
            }
        }
        editor.putLong("fee_cat_default", categoryId)
        if (subCategoryId != null) {
            editor.putLong("fee_subcat_default", subCategoryId)
        }
        editor.apply()
    }

    fun getFeeSubCategoryForPayee(payee: String): Long? {
        val trimmed = payee.trim().lowercase()
        if (trimmed.isNotEmpty() && prefs.contains("fee_subcat_payee_$trimmed")) {
            return prefs.getLong("fee_subcat_payee_$trimmed", -1L).takeIf { it > 0L }
        }
        val defaultSubCat = prefs.getLong("fee_subcat_default", -1L)
        return if (defaultSubCat > 0L) defaultSubCat else null
    }

    companion object {
        @Volatile
        private var instance: TransferFeePreferences? = null

        fun getInstance(context: Context): TransferFeePreferences {
            return instance ?: synchronized(this) {
                instance ?: TransferFeePreferences(context).also { instance = it }
            }
        }
    }
}
