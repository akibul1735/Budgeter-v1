package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import org.json.JSONArray
import org.json.JSONObject

/**
 * FilterStore: Persistent JSON storage for FilterState per spec.key using SharedPreferences.
 * Includes automatic one-time migration hook from TabFilterPreferences.
 */
class FilterStore private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "unified_filter_store"
        private const val KEY_MIGRATED = "migrated_tab_filter_preferences_v1"

        @Volatile
        private var instance: FilterStore? = null

        fun getInstance(context: Context): FilterStore {
            return instance ?: synchronized(this) {
                instance ?: FilterStore(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Saves a FilterState to SharedPreferences as a JSON string for [specKey].
     */
    fun saveFilterState(specKey: String, state: FilterState) {
        if (state.isEmpty()) {
            prefs.edit().remove("filter_$specKey").apply()
            return
        }

        try {
            val rootObj = JSONObject()
            for ((fieldId, value) in state) {
                val fieldObj = JSONObject()
                when (value) {
                    is FilterValue.Select -> {
                        fieldObj.put("type", "select")
                        val arr = JSONArray()
                        value.selectedIds.forEach { arr.put(it) }
                        fieldObj.put("selectedIds", arr)
                    }

                    is FilterValue.SingleSelect -> {
                        fieldObj.put("type", "single_select")
                        if (value.selectedId != null) {
                            fieldObj.put("selectedId", value.selectedId)
                        }
                    }

                    is FilterValue.Range -> {
                        fieldObj.put("type", "range")
                        if (value.min != null) fieldObj.put("min", value.min)
                        if (value.max != null) fieldObj.put("max", value.max)
                    }

                    is FilterValue.Date -> {
                        fieldObj.put("type", "date")
                        if (value.presetId != null) fieldObj.put("presetId", value.presetId)
                        if (value.startMs != null) fieldObj.put("startMs", value.startMs)
                        if (value.endMs != null) fieldObj.put("endMs", value.endMs)
                    }

                    is FilterValue.ToggleGroup -> {
                        fieldObj.put("type", "toggle_group")
                        val arr = JSONArray()
                        value.activeIds.forEach { arr.put(it) }
                        fieldObj.put("activeIds", arr)
                    }

                    is FilterValue.Sort -> {
                        fieldObj.put("type", "sort")
                        if (value.sortId != null) fieldObj.put("sortId", value.sortId)
                    }

                    is FilterValue.BooleanVal -> {
                        fieldObj.put("type", "boolean")
                        fieldObj.put("value", value.value)
                    }

                    is FilterValue.Custom -> {
                        fieldObj.put("type", "custom")
                        fieldObj.put("rawJson", value.rawJson)
                    }
                }
                rootObj.put(fieldId, fieldObj)
            }
            prefs.edit().putString("filter_$specKey", rootObj.toString()).apply()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Loads a FilterState from SharedPreferences for [specKey].
     */
    fun loadFilterState(specKey: String): FilterState {
        val jsonStr = prefs.getString("filter_$specKey", null) ?: return emptyMap()
        val result = mutableMapOf<String, FilterValue>()

        try {
            val rootObj = JSONObject(jsonStr)
            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val fieldId = keys.next()
                val fieldObj = rootObj.getJSONObject(fieldId)
                val type = fieldObj.optString("type", "")

                val value: FilterValue? = when (type) {
                    "select" -> {
                        val arr = fieldObj.optJSONArray("selectedIds")
                        val set = mutableSetOf<String>()
                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                set.add(arr.getString(i))
                            }
                        }
                        FilterValue.Select(set)
                    }

                    "single_select" -> {
                        val id = if (fieldObj.has("selectedId")) fieldObj.getString("selectedId") else null
                        FilterValue.SingleSelect(id)
                    }

                    "range" -> {
                        val min = if (fieldObj.has("min")) fieldObj.getDouble("min") else null
                        val max = if (fieldObj.has("max")) fieldObj.getDouble("max") else null
                        FilterValue.Range(min = min, max = max)
                    }

                    "date" -> {
                        val presetId = if (fieldObj.has("presetId")) fieldObj.getString("presetId") else null
                        val startMs = if (fieldObj.has("startMs")) fieldObj.getLong("startMs") else null
                        val endMs = if (fieldObj.has("endMs")) fieldObj.getLong("endMs") else null
                        FilterValue.Date(presetId = presetId, startMs = startMs, endMs = endMs)
                    }

                    "toggle_group" -> {
                        val arr = fieldObj.optJSONArray("activeIds")
                        val set = mutableSetOf<String>()
                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                set.add(arr.getString(i))
                            }
                        }
                        FilterValue.ToggleGroup(set)
                    }

                    "sort" -> {
                        val sortId = if (fieldObj.has("sortId")) fieldObj.getString("sortId") else null
                        FilterValue.Sort(sortId)
                    }

                    "boolean" -> {
                        val b = fieldObj.optBoolean("value", false)
                        FilterValue.BooleanVal(b)
                    }

                    "custom" -> {
                        val raw = fieldObj.optString("rawJson", "")
                        FilterValue.Custom(raw)
                    }

                    else -> null
                }

                if (value != null) {
                    result[fieldId] = value
                }
            }
        } catch (_: Exception) {
            return emptyMap()
        }

        return result
    }

    /**
     * Clears saved state for [specKey].
     */
    fun clearFilterState(specKey: String) {
        prefs.edit().remove("filter_$specKey").apply()
    }

    /**
     * One-time migration hook from TabFilterPreferences.
     * Legacy quick filters, sort options, and search queries in TabFilterPreferences continue
     * to operate independently in BudgetScreen without being duplicated into FilterStore states.
     * Note: If any display option like hideEmptyGroups is ever migrated, it is placed in "display_options".
     */
    fun migrateFromTabFilterPreferences(tabFilterPrefs: TabFilterPreferences) {
        if (prefs.getBoolean(KEY_MIGRATED, false)) return

        try {
            // Quick filters, sort, and search in TabFilterPreferences operate independently
            // at the screen level. We set the migrated flag without copying them into
            // budget_maker_* FilterState to ensure they are never double-applied.
            prefs.edit().putBoolean(KEY_MIGRATED, true).apply()
        } catch (_: Exception) {
            // If migration fails, do not mark as migrated so it can retry safely
        }
    }
}
