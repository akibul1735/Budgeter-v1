package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.screens.BudgetFilterOption
import com.example.ui.screens.BudgetSortOption
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
     * One-time migration hook that reads values from TabFilterPreferences and stores them
     * in the unified FilterStore format without overwriting any existing saved state.
     */
    fun migrateFromTabFilterPreferences(tabFilterPrefs: TabFilterPreferences) {
        if (prefs.getBoolean(KEY_MIGRATED, false)) return

        try {
            // 1. Expense Tab Migration
            if (!prefs.contains("filter_budget_maker_expense")) {
                val expenseConditions = mutableSetOf<String>()
                tabFilterPrefs.budgetExpenseQuickFilters.forEach { opt ->
                    when (opt) {
                        BudgetFilterOption.ONLY_REMAINING -> expenseConditions.add("onlyUnderBudgetRemaining")
                        BudgetFilterOption.BUDGETED_ONLY -> expenseConditions.add("onlyWithBudgetOrTarget")
                        BudgetFilterOption.UNBUDGETED -> expenseConditions.add("onlyWithoutBudgetOrTarget")
                        BudgetFilterOption.ACTIVE_ONLY -> expenseConditions.add("onlyWithActualActivity")
                        BudgetFilterOption.ONLY_GROUPS -> expenseConditions.add("hideEmptyGroups")
                        else -> {}
                    }
                }
                val expenseMap = mutableMapOf<String, FilterValue>()
                if (expenseConditions.isNotEmpty()) {
                    expenseMap["conditions"] = FilterValue.ToggleGroup(expenseConditions)
                }
                if (tabFilterPrefs.budgetExpenseSort != BudgetSortOption.DEFAULT) {
                    expenseMap["sort"] = FilterValue.Sort(tabFilterPrefs.budgetExpenseSort.name)
                }
                if (tabFilterPrefs.budgetSearchQuery.isNotBlank()) {
                    expenseMap["search"] = FilterValue.Custom(tabFilterPrefs.budgetSearchQuery)
                }
                if (expenseMap.isNotEmpty()) {
                    saveFilterState("budget_maker_expense", expenseMap)
                }
            }

            // 2. Income Tab Migration
            if (!prefs.contains("filter_budget_maker_income")) {
                val incomeConditions = mutableSetOf<String>()
                tabFilterPrefs.budgetIncomeQuickFilters.forEach { opt ->
                    when (opt) {
                        BudgetFilterOption.ONLY_REMAINING -> incomeConditions.add("onlyTargetPending")
                        BudgetFilterOption.BUDGETED_ONLY -> incomeConditions.add("onlyWithBudgetOrTarget")
                        BudgetFilterOption.UNBUDGETED -> incomeConditions.add("onlyWithoutBudgetOrTarget")
                        BudgetFilterOption.ACTIVE_ONLY -> incomeConditions.add("onlyWithActualActivity")
                        BudgetFilterOption.ONLY_GROUPS -> incomeConditions.add("hideEmptyGroups")
                        else -> {}
                    }
                }
                val incomeMap = mutableMapOf<String, FilterValue>()
                if (incomeConditions.isNotEmpty()) {
                    incomeMap["conditions"] = FilterValue.ToggleGroup(incomeConditions)
                }
                if (tabFilterPrefs.budgetIncomeSort != BudgetSortOption.DEFAULT) {
                    incomeMap["sort"] = FilterValue.Sort(tabFilterPrefs.budgetIncomeSort.name)
                }
                if (tabFilterPrefs.budgetSearchQuery.isNotBlank()) {
                    incomeMap["search"] = FilterValue.Custom(tabFilterPrefs.budgetSearchQuery)
                }
                if (incomeMap.isNotEmpty()) {
                    saveFilterState("budget_maker_income", incomeMap)
                }
            }

            // 3. Asset Tab Migration
            if (!prefs.contains("filter_budget_maker_asset")) {
                val assetConditions = mutableSetOf<String>()
                tabFilterPrefs.budgetAssetQuickFilters.forEach { opt ->
                    when (opt) {
                        BudgetFilterOption.ACTIVE_ONLY -> assetConditions.add("onlyPositiveBalance")
                        BudgetFilterOption.BUDGETED_ONLY -> assetConditions.add("onlyWithBudgetOrTarget")
                        BudgetFilterOption.UNBUDGETED -> assetConditions.add("onlyWithoutBudgetOrTarget")
                        BudgetFilterOption.ONLY_GROUPS -> assetConditions.add("hideEmptyGroups")
                        else -> {}
                    }
                }
                val assetMap = mutableMapOf<String, FilterValue>()
                if (assetConditions.isNotEmpty()) {
                    assetMap["conditions"] = FilterValue.ToggleGroup(assetConditions)
                }
                if (tabFilterPrefs.budgetAssetSort != BudgetSortOption.DEFAULT) {
                    assetMap["sort"] = FilterValue.Sort(tabFilterPrefs.budgetAssetSort.name)
                }
                if (tabFilterPrefs.budgetSearchQuery.isNotBlank()) {
                    assetMap["search"] = FilterValue.Custom(tabFilterPrefs.budgetSearchQuery)
                }
                if (assetMap.isNotEmpty()) {
                    saveFilterState("budget_maker_asset", assetMap)
                }
            }

            // 4. Liability Tab Migration
            if (!prefs.contains("filter_budget_maker_liability")) {
                val liabilityConditions = mutableSetOf<String>()
                tabFilterPrefs.budgetLiabilityQuickFilters.forEach { opt ->
                    when (opt) {
                        BudgetFilterOption.ACTIVE_ONLY -> liabilityConditions.add("onlyOutstandingDebt")
                        BudgetFilterOption.BUDGETED_ONLY -> liabilityConditions.add("onlyWithBudgetOrTarget")
                        BudgetFilterOption.UNBUDGETED -> liabilityConditions.add("onlyWithoutBudgetOrTarget")
                        BudgetFilterOption.ONLY_GROUPS -> liabilityConditions.add("hideEmptyGroups")
                        else -> {}
                    }
                }
                val liabilityMap = mutableMapOf<String, FilterValue>()
                if (liabilityConditions.isNotEmpty()) {
                    liabilityMap["conditions"] = FilterValue.ToggleGroup(liabilityConditions)
                }
                if (tabFilterPrefs.budgetLiabilitySort != BudgetSortOption.DEFAULT) {
                    liabilityMap["sort"] = FilterValue.Sort(tabFilterPrefs.budgetLiabilitySort.name)
                }
                if (tabFilterPrefs.budgetSearchQuery.isNotBlank()) {
                    liabilityMap["search"] = FilterValue.Custom(tabFilterPrefs.budgetSearchQuery)
                }
                if (liabilityMap.isNotEmpty()) {
                    saveFilterState("budget_maker_liability", liabilityMap)
                }
            }

            // Mark migration successful only after all steps complete without error
            prefs.edit().putBoolean(KEY_MIGRATED, true).apply()
        } catch (_: Exception) {
            // If migration fails, do not mark as migrated so it can retry safely
        }
    }
}
