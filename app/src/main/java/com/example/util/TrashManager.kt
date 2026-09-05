package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class TrashItemType {
    TRANSACTION,
    ACCOUNT,
    CATEGORY
}

data class TrashedItem(
    val id: String,
    val type: TrashItemType,
    val title: String,
    val subtitle: String,
    val amount: Double? = null,
    val deletedAtEpochMs: Long = System.currentTimeMillis(),
    val rawJsonData: String = ""
)

class TrashManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_trash_prefs", Context.MODE_PRIVATE)

    private val _trashedItems = MutableStateFlow<List<TrashedItem>>(loadTrash())
    val trashedItems: StateFlow<List<TrashedItem>> = _trashedItems.asStateFlow()

    private fun loadTrash(): List<TrashedItem> {
        val jsonString = prefs.getString(KEY_TRASH_ITEMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<TrashedItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TrashedItem(
                        id = obj.getString("id"),
                        type = TrashItemType.valueOf(obj.getString("type")),
                        title = obj.getString("title"),
                        subtitle = obj.optString("subtitle", ""),
                        amount = if (obj.has("amount")) obj.getDouble("amount") else null,
                        deletedAtEpochMs = obj.optLong("deletedAt", System.currentTimeMillis()),
                        rawJsonData = obj.optString("rawJson", "")
                    )
                )
            }
            list.sortedByDescending { it.deletedAtEpochMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveTrash(items: List<TrashedItem>) {
        val array = JSONArray()
        items.take(100).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("type", item.type.name)
                put("title", item.title)
                put("subtitle", item.subtitle)
                item.amount?.let { put("amount", it) }
                put("deletedAt", item.deletedAtEpochMs)
                put("rawJson", item.rawJsonData)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_TRASH_ITEMS, array.toString()).apply()
        _trashedItems.value = items
    }

    fun addTransaction(tx: Transaction, accountName: String = "", categoryName: String = "") {
        val json = JSONObject().apply {
            put("id", tx.id)
            put("type", tx.type.name)
            put("amount", tx.amount)
            put("dateEpochMs", tx.dateEpochMs)
            put("note", tx.note)
            put("payeeOrPayer", tx.payeeOrPayer)
            put("debitAccountId", tx.debitAccountId)
            put("creditAccountId", tx.creditAccountId)
            put("categoryId", tx.categoryId)
        }.toString()

        val sub = buildString {
            if (accountName.isNotBlank()) append(accountName)
            if (categoryName.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(categoryName)
            }
            if (tx.note.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(tx.note)
            }
        }

        val item = TrashedItem(
            id = "tx_${tx.id}_${System.currentTimeMillis()}",
            type = TrashItemType.TRANSACTION,
            title = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else "${tx.type.name} Transaction",
            subtitle = sub,
            amount = tx.amount,
            deletedAtEpochMs = System.currentTimeMillis(),
            rawJsonData = json
        )
        val current = _trashedItems.value.toMutableList()
        current.add(0, item)
        saveTrash(current)
    }

    fun addAccount(account: Account) {
        val json = JSONObject().apply {
            put("id", account.id)
            put("nameEn", account.nameEn)
            put("nameBn", account.nameBn)
            put("type", account.type.name)
            put("parentId", account.parentId)
        }.toString()

        val item = TrashedItem(
            id = "acc_${account.id}_${System.currentTimeMillis()}",
            type = TrashItemType.ACCOUNT,
            title = account.nameEn,
            subtitle = "Account (${account.type.name})",
            deletedAtEpochMs = System.currentTimeMillis(),
            rawJsonData = json
        )
        val current = _trashedItems.value.toMutableList()
        current.add(0, item)
        saveTrash(current)
    }

    fun addCategory(category: Category) {
        val json = JSONObject().apply {
            put("id", category.id)
            put("nameEn", category.nameEn)
            put("nameBn", category.nameBn)
            put("type", category.type.name)
            put("parentId", category.parentId)
        }.toString()

        val item = TrashedItem(
            id = "cat_${category.id}_${System.currentTimeMillis()}",
            type = TrashItemType.CATEGORY,
            title = category.nameEn,
            subtitle = "Category (${category.type.name})",
            deletedAtEpochMs = System.currentTimeMillis(),
            rawJsonData = json
        )
        val current = _trashedItems.value.toMutableList()
        current.add(0, item)
        saveTrash(current)
    }

    fun removeItem(id: String) {
        val current = _trashedItems.value.toMutableList()
        current.removeAll { it.id == id }
        saveTrash(current)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_TRASH_ITEMS).apply()
        _trashedItems.value = emptyList()
    }

    companion object {
        private const val KEY_TRASH_ITEMS = "key_trashed_items_list"

        @Volatile
        private var instance: TrashManager? = null

        fun getInstance(context: Context): TrashManager {
            return instance ?: synchronized(this) {
                instance ?: TrashManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
