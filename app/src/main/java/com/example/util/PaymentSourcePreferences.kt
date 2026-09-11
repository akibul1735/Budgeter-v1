package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class AccountObligation(
    val id: String,
    val sourceAccountId: Long, // Payment source account paying or receiving
    val targetAccountId: Long, // The obligation account (e.g. Liability, Credit Card, Loan, Receivable)
    val amount: Double,
    val isExpense: Boolean = true, // true = paying off a payable/liability from source; false = receiving from debtor into source
    val note: String = ""
)

data class PaymentSourceConfig(
    val selectedSourceAccountIds: Set<Long> = emptySet(),
    val hasCustomizedSelection: Boolean = false,
    val accountObligations: List<AccountObligation> = emptyList()
) {
    fun isPaymentSource(accountId: Long, fallbackIsLeafAsset: Boolean): Boolean {
        return if (hasCustomizedSelection) {
            selectedSourceAccountIds.contains(accountId)
        } else {
            fallbackIsLeafAsset
        }
    }
}

class PaymentSourcePreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_payment_source_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<PaymentSourceConfig> = _config.asStateFlow()

    private fun loadConfig(): PaymentSourceConfig {
        val hasCustom = prefs.getBoolean(KEY_HAS_CUSTOM, false)
        val idsSet = prefs.getStringSet(KEY_SELECTED_ACCOUNT_IDS, emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        
        val obligationsJson = prefs.getString(KEY_OBLIGATIONS, "[]") ?: "[]"
        val obligationsList = mutableListOf<AccountObligation>()
        try {
            val jsonArr = JSONArray(obligationsJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                obligationsList.add(
                    AccountObligation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        sourceAccountId = obj.getLong("sourceAccountId"),
                        targetAccountId = obj.getLong("targetAccountId"),
                        amount = obj.getDouble("amount"),
                        isExpense = obj.optBoolean("isExpense", true),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (_: Exception) {}

        return PaymentSourceConfig(
            selectedSourceAccountIds = idsSet,
            hasCustomizedSelection = hasCustom,
            accountObligations = obligationsList
        )
    }

    fun setSelectedSourceAccountIds(ids: Set<Long>) {
        prefs.edit()
            .putBoolean(KEY_HAS_CUSTOM, true)
            .putStringSet(KEY_SELECTED_ACCOUNT_IDS, ids.map { it.toString() }.toSet())
            .apply()
        _config.value = _config.value.copy(
            selectedSourceAccountIds = ids,
            hasCustomizedSelection = true
        )
    }

    fun toggleSourceAccount(accountId: Long, isSource: Boolean, allCurrentIds: Set<Long>) {
        val updated = allCurrentIds.toMutableSet()
        if (isSource) {
            updated.add(accountId)
        } else {
            updated.remove(accountId)
        }
        setSelectedSourceAccountIds(updated)
    }

    fun saveAccountObligation(obligation: AccountObligation) {
        val currentList = _config.value.accountObligations.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == obligation.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = obligation
        } else {
            currentList.add(obligation)
        }
        persistObligations(currentList)
    }

    fun deleteAccountObligation(obligationId: String) {
        val currentList = _config.value.accountObligations.filter { it.id != obligationId }
        persistObligations(currentList)
    }

    private fun persistObligations(list: List<AccountObligation>) {
        val jsonArr = JSONArray()
        for (ob in list) {
            val obj = JSONObject()
            obj.put("id", ob.id)
            obj.put("sourceAccountId", ob.sourceAccountId)
            obj.put("targetAccountId", ob.targetAccountId)
            obj.put("amount", ob.amount)
            obj.put("isExpense", ob.isExpense)
            obj.put("note", ob.note)
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_OBLIGATIONS, jsonArr.toString()).apply()
        _config.value = _config.value.copy(accountObligations = list)
    }

    companion object {
        private const val KEY_HAS_CUSTOM = "has_custom_source_selection"
        private const val KEY_SELECTED_ACCOUNT_IDS = "selected_source_account_ids"
        private const val KEY_OBLIGATIONS = "account_obligations"

        @Volatile
        private var INSTANCE: PaymentSourcePreferences? = null

        fun getInstance(context: Context): PaymentSourcePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaymentSourcePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
