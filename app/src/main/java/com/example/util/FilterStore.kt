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
        const val KEY_MIGRATED_LABELS = "migrated_labels_v1"
        const val KEY_MIGRATED_CATEGORIES = "migrated_categories_v1"

        val VALID_LABELS_DATE_PRESETS = setOf(
            "this_month", "last_month", "this_week", "today", "yesterday",
            "last_30_days", "last_90_days", "this_year", "last_year", "all_time"
        )
        const val DEFAULT_LABELS_DATE_PRESET = "this_month"

        val VALID_LABELS_SORT_ORDERS = setOf(
            "amount_desc", "amount_asc", "count_desc", "count_asc",
            "avg_desc", "avg_asc", "name_asc", "name_desc", "recent_date"
        )
        const val DEFAULT_LABELS_SORT_ORDER = "amount_desc"

        val VALID_LABELS_TAB_MODES = setOf(
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_EXPENSE,
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_ALL,
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_INCOME
        )
        const val DEFAULT_LABELS_TAB_MODE = com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_ALL

        val VALID_LABELS_CATEGORY_SEGMENTS = setOf(
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_HASHTAGS,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_NOTES,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_PAYEES,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_UNTAGGED,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_ALL
        )
        const val DEFAULT_LABELS_CATEGORY_SEGMENT = com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_HASHTAGS

        val VALID_CATEGORIES_TYPES = setOf(
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_ALL,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME
        )
        const val DEFAULT_CATEGORIES_TYPE = com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE

        val VALID_CATEGORIES_HIERARCHIES = setOf(
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ALL,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ONLY_GROUPS,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ONLY_CATEGORIES
        )
        const val DEFAULT_CATEGORIES_HIERARCHY = com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ALL

        val VALID_CATEGORIES_SORTS = setOf(
            "default",
            "budget_high_to_low",
            "budget_low_to_high",
            "most_used",
            "least_used",
            "name_az",
            "name_za"
        )
        const val DEFAULT_CATEGORIES_SORT = "default"

        fun mapLabelsDatePreset(preset: com.example.ui.dialogs.AggregatedDatePreset?): String {
            val id = preset?.name?.lowercase()
            return if (id != null && id in VALID_LABELS_DATE_PRESETS) id else DEFAULT_LABELS_DATE_PRESET
        }

        fun mapLabelsSortOrder(sort: com.example.ui.dialogs.AggregatedSortOrder?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_LABELS_SORT_ORDERS) id else DEFAULT_LABELS_SORT_ORDER
        }

        fun mapLabelsTabMode(mode: String?): String {
            val id = mode?.lowercase()
            return if (id != null && id in VALID_LABELS_TAB_MODES) id else DEFAULT_LABELS_TAB_MODE
        }

        fun mapLabelsCategorySegment(segment: String?): String {
            val id = segment?.lowercase()
            return if (id != null && id in VALID_LABELS_CATEGORY_SEGMENTS) id else DEFAULT_LABELS_CATEGORY_SEGMENT
        }

        fun mapCategoriesType(type: com.example.data.model.CategoryType?): String {
            return when (type) {
                com.example.data.model.CategoryType.EXPENSE -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE
                com.example.data.model.CategoryType.INCOME -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME
                null -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_ALL
            }
        }

        fun mapCategoriesHierarchy(hierarchy: com.example.ui.screens.CategoryViewHierarchyFilter?): String {
            val id = hierarchy?.name?.lowercase()
            return if (id != null && id in VALID_CATEGORIES_HIERARCHIES) id else DEFAULT_CATEGORIES_HIERARCHY
        }

        fun mapCategoriesSort(sort: com.example.ui.screens.CategorySortFilter?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_CATEGORIES_SORTS) id else DEFAULT_CATEGORIES_SORT
        }

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
     * Note: FilterValue.Search is stripped so search queries are never persisted.
     */
    fun saveFilterState(specKey: String, state: FilterState) {
        val nonSearchState = state.filter { (_, v) -> v !is FilterValue.Search }
        if (nonSearchState.isEmpty()) {
            prefs.edit().remove("filter_$specKey").apply()
            return
        }

        try {
            val rootObj = JSONObject()
            for ((fieldId, value) in nonSearchState) {
                if (value is FilterValue.Search) continue
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

                    is FilterValue.Search -> {
                        // Stripped: search queries are never persisted
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
            prefs.edit().putString("filter_$specKey", rootObj.toString()).commit()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Loads a FilterState from SharedPreferences for [specKey].
     * Note: Search values are never restored from storage so stale searches never reappear.
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

                    "search" -> null

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

                if (value != null && value !is FilterValue.Search) {
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
        prefs.edit().remove("filter_$specKey").commit()
    }

    /**
     * Clears all filter preferences and migration flags (used for deterministic testing).
     */
    fun clearAllForTesting() {
        prefs.edit().clear().commit()
    }

    /**
     * Clears migration flags (used for testing).
     */
    fun resetMigrationFlagsForTesting() {
        prefs.edit()
            .remove(KEY_MIGRATED_LABELS)
            .remove(KEY_MIGRATED_CATEGORIES)
            .remove("migrated_tab_filter_preferences_v1")
            .commit()
    }

    /**
     * One-time migration hook from TabFilterPreferences.
     * Uses separate per-screen flags (migrated_labels_v1, migrated_categories_v1).
     * Existing users who already have the old flag set still get their Labels and Categories
     * values migrated, as long as no filter_labels / filter_categories state exists yet.
     * Existing FilterStore state is never overwritten.
     * Search values are stripped so a stale search query is never persisted.
     */
    fun migrateFromTabFilterPreferences(tabFilterPrefs: TabFilterPreferences) {
        try {
            // 1. Migrate LabelsScreen saved preferences with per-screen flag
            if (!prefs.getBoolean(KEY_MIGRATED_LABELS, false)) {
                val labelsKey = "filter_${com.example.ui.components.filter.specs.LabelsFilterSpec.SPEC_KEY}"
                if (!prefs.contains(labelsKey)) {
                    val labelsState = mutableMapOf<String, FilterValue>()
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_SEGMENT] = FilterValue.ToggleGroup(
                        setOf(mapLabelsCategorySegment(tabFilterPrefs.labelsCategorySegment))
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_TYPE_MODE] = FilterValue.ToggleGroup(
                        setOf(mapLabelsTabMode(tabFilterPrefs.labelsTabMode))
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_DATE] = FilterValue.Date(
                        presetId = mapLabelsDatePreset(tabFilterPrefs.labelsDatePreset)
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_SORT] = FilterValue.Sort(
                        mapLabelsSortOrder(tabFilterPrefs.labelsSortOrder)
                    )
                    saveFilterState(com.example.ui.components.filter.specs.LabelsFilterSpec.SPEC_KEY, labelsState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_LABELS, true).commit()
            }

            // 2. Migrate CategoriesScreen saved preferences with per-screen flag
            if (!prefs.getBoolean(KEY_MIGRATED_CATEGORIES, false)) {
                val categoriesKey = "filter_${com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY}"
                if (!prefs.contains(categoriesKey)) {
                    val catState = mutableMapOf<String, FilterValue>()
                    val typeMode = mapCategoriesType(tabFilterPrefs.categoriesTypeFilter)
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE] = FilterValue.ToggleGroup(setOf(typeMode))
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_HIERARCHY] = FilterValue.ToggleGroup(
                        setOf(mapCategoriesHierarchy(tabFilterPrefs.categoriesHierarchyFilter))
                    )
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_SORT] = FilterValue.Sort(
                        mapCategoriesSort(tabFilterPrefs.categoriesSortFilter)
                    )
                    saveFilterState(com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY, catState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_CATEGORIES, true).commit()
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }
}
