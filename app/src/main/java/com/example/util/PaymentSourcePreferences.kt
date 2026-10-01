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
    val note: String = "",
    val isActive: Boolean = true
)

data class AccountLink(
    val id: String = java.util.UUID.randomUUID().toString(),
    val otherAccountId: Long,
    val paymentSourceAccountId: Long,
    val relationNote: String = "",
    val isActive: Boolean = true
)

data class PaymentSourceConfig(
    val selectedSourceAccountIds: Set<Long> = emptySet(),
    val hasCustomizedSelection: Boolean = false,
    val accountObligations: List<AccountObligation> = emptyList(),
    val accountLinks: List<AccountLink> = emptyList(),
    val inactiveCategoryIds: Set<Long> = emptySet(),
    val inactiveOtherAccountIds: Set<Long> = emptySet()
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
        val inactiveCatSet = prefs.getStringSet(KEY_INACTIVE_CATEGORIES, emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        val inactiveOtherSet = prefs.getStringSet(KEY_INACTIVE_OTHER_ACCOUNTS, emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        
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
                        note = obj.optString("note", ""),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
        } catch (_: Exception) {}

        val linksJson = prefs.getString(KEY_ACCOUNT_LINKS, "[]") ?: "[]"
        val linksList = mutableListOf<AccountLink>()
        try {
            val jsonArr = JSONArray(linksJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                linksList.add(
                    AccountLink(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        otherAccountId = obj.getLong("otherAccountId"),
                        paymentSourceAccountId = obj.getLong("paymentSourceAccountId"),
                        relationNote = obj.optString("relationNote", ""),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
        } catch (_: Exception) {}

        return PaymentSourceConfig(
            selectedSourceAccountIds = idsSet,
            hasCustomizedSelection = hasCustom,
            accountObligations = obligationsList,
            accountLinks = linksList,
            inactiveCategoryIds = inactiveCatSet,
            inactiveOtherAccountIds = inactiveOtherSet
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

    fun toggleCategoryActive(categoryId: Long, isActive: Boolean) {
        val currentSet = _config.value.inactiveCategoryIds.toMutableSet()
        if (isActive) {
            currentSet.remove(categoryId)
        } else {
            currentSet.add(categoryId)
        }
        prefs.edit()
            .putStringSet(KEY_INACTIVE_CATEGORIES, currentSet.map { it.toString() }.toSet())
            .apply()
        _config.value = _config.value.copy(inactiveCategoryIds = currentSet)
    }

    fun toggleMultipleCategoriesActive(categoryIds: Collection<Long>, isActive: Boolean) {
        val currentSet = _config.value.inactiveCategoryIds.toMutableSet()
        if (isActive) {
            currentSet.removeAll(categoryIds.toSet())
        } else {
            currentSet.addAll(categoryIds)
        }
        prefs.edit()
            .putStringSet(KEY_INACTIVE_CATEGORIES, currentSet.map { it.toString() }.toSet())
            .apply()
        _config.value = _config.value.copy(inactiveCategoryIds = currentSet)
    }

    fun toggleMultipleOtherAccountsActive(otherAccountIds: Collection<Long>, isActive: Boolean) {
        val currentSet = _config.value.inactiveOtherAccountIds.toMutableSet()
        if (isActive) {
            currentSet.removeAll(otherAccountIds.toSet())
        } else {
            currentSet.addAll(otherAccountIds)
        }
        prefs.edit()
            .putStringSet(KEY_INACTIVE_OTHER_ACCOUNTS, currentSet.map { it.toString() }.toSet())
            .apply()
        _config.value = _config.value.copy(inactiveOtherAccountIds = currentSet)
    }

    fun toggleOtherAccountActive(otherAccountId: Long, isActive: Boolean) {
        val currentSet = _config.value.inactiveOtherAccountIds.toMutableSet()
        if (isActive) {
            currentSet.remove(otherAccountId)
        } else {
            currentSet.add(otherAccountId)
        }
        prefs.edit()
            .putStringSet(KEY_INACTIVE_OTHER_ACCOUNTS, currentSet.map { it.toString() }.toSet())
            .apply()
        _config.value = _config.value.copy(inactiveOtherAccountIds = currentSet)
    }

    fun toggleAccountObligationActive(obligationId: String, isActive: Boolean) {
        val currentList = _config.value.accountObligations.toMutableList()
        val index = currentList.indexOfFirst { it.id == obligationId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(isActive = isActive)
            persistObligations(currentList)
        }
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
            obj.put("isActive", ob.isActive)
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_OBLIGATIONS, jsonArr.toString()).apply()
        _config.value = _config.value.copy(accountObligations = list)
    }

    fun saveAccountLink(link: AccountLink) {
        val currentList = _config.value.accountLinks.toMutableList()
        val existingIndex = currentList.indexOfFirst { 
            it.id == link.id || (it.otherAccountId == link.otherAccountId && it.paymentSourceAccountId == link.paymentSourceAccountId)
        }
        if (existingIndex >= 0) {
            currentList[existingIndex] = link
        } else {
            currentList.add(link)
        }
        persistLinks(currentList)
    }

    fun deleteAccountLink(linkId: String) {
        val currentList = _config.value.accountLinks.filter { it.id != linkId }
        persistLinks(currentList)
    }

    fun saveLinksForOtherAccount(otherAccountId: Long, sourceAccountIds: List<Long>, note: String = "") {
        val existingOthers = _config.value.accountLinks.filter { it.otherAccountId != otherAccountId }.toMutableList()
        sourceAccountIds.forEach { srcId ->
            existingOthers.add(
                AccountLink(
                    otherAccountId = otherAccountId,
                    paymentSourceAccountId = srcId,
                    relationNote = note,
                    isActive = true
                )
            )
        }
        persistLinks(existingOthers)
    }

    private fun persistLinks(list: List<AccountLink>) {
        val jsonArr = JSONArray()
        for (lk in list) {
            val obj = JSONObject()
            obj.put("id", lk.id)
            obj.put("otherAccountId", lk.otherAccountId)
            obj.put("paymentSourceAccountId", lk.paymentSourceAccountId)
            obj.put("relationNote", lk.relationNote)
            obj.put("isActive", lk.isActive)
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_ACCOUNT_LINKS, jsonArr.toString()).apply()
        _config.value = _config.value.copy(accountLinks = list)
    }

    fun reload() {
        _config.value = loadConfig()
    }

    companion object {
        private const val KEY_HAS_CUSTOM = "has_custom_source_selection"
        private const val KEY_SELECTED_ACCOUNT_IDS = "selected_source_account_ids"
        private const val KEY_OBLIGATIONS = "account_obligations"
        private const val KEY_ACCOUNT_LINKS = "account_links"
        private const val KEY_INACTIVE_CATEGORIES = "inactive_categories"
        private const val KEY_INACTIVE_OTHER_ACCOUNTS = "inactive_other_accounts"

        @Volatile
        private var INSTANCE: PaymentSourcePreferences? = null

        fun getInstance(context: Context): PaymentSourcePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PaymentSourcePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
