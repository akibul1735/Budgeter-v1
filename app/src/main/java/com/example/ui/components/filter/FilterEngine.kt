package com.example.ui.components.filter

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.BuildConfig
import com.example.data.model.LanguageMode
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.util.Locale

/**
 * FilterEngine: Core filtering, sorting, counting, and state transformation logic.
 */
object FilterEngine {

    /**
     * Applies all active filter fields in [spec] sequentially to [items] with AND logic,
     * ensuring SortField is applied strictly after all filter predicates regardless of field order.
     */
    fun <T> applyFilters(spec: FilterSpec<T>, items: List<T>, state: FilterState): List<T> {
        var result = items

        // 1. Filtering pass (Evaluate all non-sort fields)
        for (field in spec.fields) {
            if (field is FilterField.SortField) continue
            val value = state[field.id] ?: continue

            when (field) {
                is FilterField.SelectField<T> -> {
                    val selectedIds = when (value) {
                        is FilterValue.Select -> value.selectedIds
                        is FilterValue.SingleSelect -> value.selectedId?.let { setOf(it) } ?: emptySet()
                        else -> emptySet()
                    }
                    if (selectedIds.isNotEmpty() && field.predicate != null) {
                        result = result.filter { item -> field.predicate.invoke(item, selectedIds) }
                    }
                }

                is FilterField.RangeField<T> -> {
                    if (value is FilterValue.Range && (value.min != null || value.max != null) && field.predicate != null) {
                        result = result.filter { item -> field.predicate.invoke(item, value.min, value.max) }
                    }
                }

                is FilterField.DateField<T> -> {
                    if (value is FilterValue.Date && field.predicate != null) {
                        val (startMs, endMs) = resolveDateBounds(field, value)
                        if (startMs != null && endMs != null) {
                            result = result.filter { item -> field.predicate.invoke(item, startMs, endMs) }
                        }
                    }
                }

                is FilterField.ToggleGroupField<T> -> {
                    if (value is FilterValue.ToggleGroup && value.activeIds.isNotEmpty()) {
                        for (condId in value.activeIds) {
                            val cond = field.conditions.firstOrNull { it.id == condId }
                            if (cond?.predicate != null) {
                                result = result.filter { item -> cond.predicate.invoke(item) }
                            } else if (BuildConfig.DEBUG && cond != null && cond.predicate == null) {
                                Log.w("FilterEngine", "ToggleGroup condition '${condId}' in field '${field.id}' has a null predicate.")
                            }
                        }
                    }
                }

                is FilterField.CustomField<T> -> {
                    if (field.predicate != null) {
                        result = result.filter { item -> field.predicate.invoke(item, value) }
                    }
                }

                is FilterField.BooleanField<T> -> {
                    if (value is FilterValue.BooleanVal && field.predicate != null) {
                        result = result.filter { item -> field.predicate.invoke(item, value.value) }
                    }
                }

                is FilterField.SearchField<T> -> {
                    val q = (value as? FilterValue.Search)?.query?.trim() ?: ""
                    if (q.isNotEmpty() && field.predicate != null) {
                        result = result.filter { item -> field.predicate.invoke(item, q) }
                    }
                }

                is FilterField.SortField<T> -> {
                    // Handled in sorting pass below
                }
            }
        }

        // 2. Sorting pass (Executed after all filter predicates are complete)
        for (field in spec.fields) {
            if (field is FilterField.SortField<T>) {
                val value = state[field.id] as? FilterValue.Sort ?: continue
                if (value.sortId != null) {
                    val option = field.options.firstOrNull { it.id == value.sortId }
                    if (option?.comparator != null) {
                        result = result.sortedWith(option.comparator)
                    }
                }
            }
        }

        return result
    }

    /**
     * Resolves date bounds (start ms, end ms) for a DateField and Date value.
     */
    private fun <T> resolveDateBounds(field: FilterField.DateField<T>, value: FilterValue.Date): Pair<Long?, Long?> {
        if (value.startMs != null && value.endMs != null) {
            return Pair(value.startMs, value.endMs)
        }
        val preset = field.presets.firstOrNull { it.id == value.presetId }
        return if (preset != null) {
            val range = preset.calculateRange()
            Pair(range.first, range.second)
        } else {
            Pair(value.startMs, value.endMs)
        }
    }

    /**
     * Counts items matching all active filter rules.
     */
    fun <T> countMatching(spec: FilterSpec<T>, items: List<T>, state: FilterState): Int {
        return applyFilters(spec, items, state).size
    }

    /**
     * Calculates the number of active filter rules currently set in [state].
     */
    fun <T> countActiveRules(spec: FilterSpec<T>, state: FilterState): Int {
        var count = 0
        for (field in spec.fields) {
            val value = state[field.id] ?: continue
            when (field) {
                is FilterField.SelectField<T> -> {
                    val ids = when (value) {
                        is FilterValue.Select -> value.selectedIds
                        is FilterValue.SingleSelect -> value.selectedId?.let { setOf(it) } ?: emptySet()
                        else -> emptySet()
                    }
                    if (ids.isNotEmpty()) count++
                }

                is FilterField.RangeField<T> -> {
                    if (value is FilterValue.Range && (value.min != null || value.max != null)) {
                        count++
                    }
                }

                is FilterField.DateField<T> -> {
                    if (value is FilterValue.Date) {
                        if (value.presetId != null && value.presetId != field.defaultPresetId) {
                            count++
                        } else if (value.startMs != null || value.endMs != null) {
                            count++
                        }
                    }
                }

                is FilterField.ToggleGroupField<T> -> {
                    if (value is FilterValue.ToggleGroup) {
                        count += value.activeIds.size
                    }
                }

                is FilterField.SortField<T> -> {
                    if (value is FilterValue.Sort && value.sortId != null && value.sortId != field.defaultSortId) {
                        count++
                    }
                }

                is FilterField.CustomField<T> -> {
                    when (value) {
                        is FilterValue.BooleanVal -> if (value.value) count++
                        is FilterValue.Custom -> if (value.rawJson.isNotBlank()) count++
                        else -> {}
                    }
                }

                is FilterField.BooleanField<T> -> {
                    if (value is FilterValue.BooleanVal && value.value != field.defaultValue) {
                        count++
                    }
                }

                is FilterField.SearchField<T> -> {
                    val q = (value as? FilterValue.Search)?.query?.trim() ?: ""
                    if (q.isNotEmpty()) count++
                }
            }
        }
        return count
    }

    /**
     * Checks whether any filter rule is active in [state].
     */
    fun <T> isFilterActive(spec: FilterSpec<T>, state: FilterState): Boolean {
        return countActiveRules(spec, state) > 0
    }

    /**
     * Clears filter values from state, either for a single [fieldId] or all fields.
     */
    fun <T> clearFilter(spec: FilterSpec<T>, state: FilterState, fieldId: String? = null): FilterState {
        return if (fieldId != null) {
            state - fieldId
        } else {
            emptyMap()
        }
    }

    /**
     * Toggles a condition within a ToggleGroupField, enforcing mutual exclusivity
     * across conditions belonging to the same [group].
     */
    fun <T> toggleCondition(
        spec: FilterSpec<T>,
        state: FilterState,
        fieldId: String,
        conditionId: String
    ): FilterState {
        val field = spec.fields.firstOrNull { it.id == fieldId } as? FilterField.ToggleGroupField<T>
            ?: return state

        val currentVal = state[fieldId] as? FilterValue.ToggleGroup ?: FilterValue.ToggleGroup()
        val targetCondition = field.conditions.firstOrNull { it.id == conditionId } ?: return state

        val isCurrentlyActive = currentVal.activeIds.contains(conditionId)
        val newActiveIds = currentVal.activeIds.toMutableSet()

        if (isCurrentlyActive) {
            newActiveIds.remove(conditionId)
        } else {
            // Enforce mutual exclusivity for conditions sharing the same group
            if (targetCondition.group != null) {
                val conflictingIds = field.conditions
                    .filter { it.group == targetCondition.group && it.id != conditionId }
                    .map { it.id }
                newActiveIds.removeAll(conflictingIds.toSet())
            }
            newActiveIds.add(conditionId)
        }

        return if (newActiveIds.isEmpty()) {
            state - fieldId
        } else {
            state + (fieldId to FilterValue.ToggleGroup(newActiveIds))
        }
    }

    /**
     * Removes a specific active chip by its chip ID.
     */
    fun <T> removeActiveChip(
        spec: FilterSpec<T>,
        state: FilterState,
        chipId: String
    ): FilterState {
        if (chipId.startsWith("toggle_")) {
            // toggle_{fieldId}_{conditionId}
            val parts = chipId.removePrefix("toggle_").split("_", limit = 2)
            if (parts.size == 2) {
                val fieldId = parts[0]
                val condId = parts[1]
                val current = state[fieldId] as? FilterValue.ToggleGroup ?: return state
                val remaining = current.activeIds - condId
                return if (remaining.isEmpty()) state - fieldId else state + (fieldId to FilterValue.ToggleGroup(remaining))
            }
        } else if (chipId.startsWith("field_")) {
            val fieldId = chipId.removePrefix("field_")
            return state - fieldId
        } else if (chipId.startsWith("sort_")) {
            val fieldId = chipId.removePrefix("sort_")
            return state - fieldId
        } else if (chipId.startsWith("bool_")) {
            val fieldId = chipId.removePrefix("bool_")
            val field = spec.fields.firstOrNull { it.id == fieldId } as? FilterField.BooleanField<*>
            val defVal = field?.defaultValue ?: false
            return state + (fieldId to FilterValue.BooleanVal(defVal))
        } else if (chipId.startsWith("custom_")) {
            val fieldId = chipId.removePrefix("custom_")
            return state - fieldId
        } else if (chipId.startsWith("search_")) {
            val fieldId = chipId.removePrefix("search_")
            return state - fieldId
        }
        return state
    }

    /**
     * Formats an amount keeping up to 2 decimals when needed.
     */
    private fun formatAmount(amount: Double, isBangla: Boolean, prefix: String): String {
        val formatted = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", amount).trimEnd('0').trimEnd('.')
        }
        val digits = if (isBangla) LanguageHelper.toBanglaDigits(formatted) else formatted
        return "$prefix$digits"
    }

    /**
     * Generates active filter chip descriptors for display in UnifiedActiveFilterBar.
     */
    fun <T> generateActiveChips(
        spec: FilterSpec<T>,
        state: FilterState,
        languageMode: LanguageMode
    ): List<UnifiedFilterChipItem> {
        val isBangla = languageMode == LanguageMode.BANGLA
        val chips = mutableListOf<UnifiedFilterChipItem>()

        for (field in spec.fields) {
            val value = state[field.id] ?: continue

            when (field) {
                is FilterField.SelectField<T> -> {
                    val allItems = field.itemsProvider?.invoke() ?: field.items
                    val selectedIds = when (value) {
                        is FilterValue.Select -> value.selectedIds
                        is FilterValue.SingleSelect -> value.selectedId?.let { setOf(it) } ?: emptySet()
                        else -> emptySet()
                    }
                    if (selectedIds.isNotEmpty()) {
                        val label = if (selectedIds.size == 1) {
                            val item = allItems.firstOrNull { it.id == selectedIds.first() }
                            item?.let { if (isBangla) it.titleBn else it.titleEn } ?: "${selectedIds.size}"
                        } else {
                            val fieldName = if (isBangla) field.titleBn else field.titleEn
                            if (isBangla) "$fieldName: ${LanguageHelper.toBanglaDigits(selectedIds.size.toString())}টি" else "$fieldName: ${selectedIds.size}"
                        }
                        chips.add(
                            UnifiedFilterChipItem(
                                id = "field_${field.id}",
                                fieldId = field.id,
                                label = label,
                                icon = field.icon,
                                color = spec.accentColor
                            )
                        )
                    }
                }

                is FilterField.RangeField<T> -> {
                    if (value is FilterValue.Range && (value.min != null || value.max != null)) {
                        val minStr = value.min?.let { formatAmount(it, isBangla, field.prefix) } ?: ""
                        val maxStr = value.max?.let { formatAmount(it, isBangla, field.prefix) } ?: ""
                        val label = when {
                            value.min != null && value.max != null -> "$minStr - $maxStr"
                            value.min != null -> "≥ $minStr"
                            else -> "≤ $maxStr"
                        }
                        chips.add(
                            UnifiedFilterChipItem(
                                id = "field_${field.id}",
                                fieldId = field.id,
                                label = label,
                                icon = field.icon,
                                color = spec.accentColor
                            )
                        )
                    }
                }

                is FilterField.DateField<T> -> {
                    if (value is FilterValue.Date) {
                        val preset = field.presets.firstOrNull { it.id == value.presetId }
                        if (preset != null && preset.id != field.defaultPresetId) {
                            chips.add(
                                UnifiedFilterChipItem(
                                    id = "field_${field.id}",
                                    fieldId = field.id,
                                    label = if (isBangla) preset.titleBn else preset.titleEn,
                                    icon = field.icon,
                                    color = spec.accentColor
                                )
                            )
                        } else if (value.startMs != null || value.endMs != null) {
                            // Custom Date Range chip
                            val startStr = value.startMs?.let { DateUtils.formatDate(it, languageMode) } ?: ""
                            val endStr = value.endMs?.let { DateUtils.formatDate(it, languageMode) } ?: ""
                            val label = when {
                                startStr.isNotEmpty() && endStr.isNotEmpty() -> "$startStr - $endStr"
                                startStr.isNotEmpty() -> "≥ $startStr"
                                else -> "≤ $endStr"
                            }
                            chips.add(
                                UnifiedFilterChipItem(
                                    id = "field_${field.id}",
                                    fieldId = field.id,
                                    label = label,
                                    icon = field.icon,
                                    color = spec.accentColor
                                )
                            )
                        }
                    }
                }

                is FilterField.ToggleGroupField<T> -> {
                    if (value is FilterValue.ToggleGroup) {
                        for (condId in value.activeIds) {
                            val cond = field.conditions.firstOrNull { it.id == condId }
                            if (cond != null) {
                                chips.add(
                                    UnifiedFilterChipItem(
                                        id = "toggle_${field.id}_$condId",
                                        fieldId = field.id,
                                        label = if (isBangla) cond.titleBn else cond.titleEn,
                                        icon = cond.icon ?: field.icon,
                                        color = spec.accentColor
                                    )
                                )
                            }
                        }
                    }
                }

                is FilterField.SortField<T> -> {
                    if (value is FilterValue.Sort && value.sortId != null && value.sortId != field.defaultSortId) {
                        val opt = field.options.firstOrNull { it.id == value.sortId }
                        if (opt != null) {
                            chips.add(
                                UnifiedFilterChipItem(
                                    id = "sort_${field.id}",
                                    fieldId = field.id,
                                    label = if (isBangla) opt.titleBn else opt.titleEn,
                                    icon = field.icon,
                                    color = spec.accentColor
                                )
                            )
                        }
                    }
                }

                is FilterField.CustomField<T> -> {
                    if (value is FilterValue.BooleanVal && value.value) {
                        chips.add(
                            UnifiedFilterChipItem(
                                id = "custom_${field.id}",
                                fieldId = field.id,
                                label = if (isBangla) field.titleBn else field.titleEn,
                                icon = field.icon,
                                color = spec.accentColor
                            )
                        )
                    }
                }

                is FilterField.BooleanField<T> -> {
                    if (value is FilterValue.BooleanVal && value.value != field.defaultValue) {
                        chips.add(
                            UnifiedFilterChipItem(
                                id = "bool_${field.id}",
                                fieldId = field.id,
                                label = if (isBangla) field.titleBn else field.titleEn,
                                icon = field.icon,
                                color = spec.accentColor
                            )
                        )
                    }
                }

                is FilterField.SearchField<T> -> {
                    val q = (value as? FilterValue.Search)?.query?.trim() ?: ""
                    if (q.isNotEmpty()) {
                        chips.add(
                            UnifiedFilterChipItem(
                                id = "search_${field.id}",
                                fieldId = field.id,
                                label = "\"$q\"",
                                icon = field.icon,
                                color = spec.accentColor
                            )
                        )
                    }
                }
            }
        }

        return chips
    }
}

// Convenience extension functions on FilterSpec
fun <T> FilterSpec<T>.apply(items: List<T>, state: FilterState): List<T> =
    FilterEngine.applyFilters(this, items, state)

fun <T> FilterSpec<T>.count(items: List<T>, state: FilterState): Int =
    FilterEngine.countMatching(this, items, state)

fun <T> FilterSpec<T>.activeFilterCount(state: FilterState): Int =
    FilterEngine.countActiveRules(this, state)

fun <T> FilterSpec<T>.isActive(state: FilterState): Boolean =
    FilterEngine.isFilterActive(this, state)

fun <T> FilterSpec<T>.clear(state: FilterState, fieldId: String? = null): FilterState =
    FilterEngine.clearFilter(this, state, fieldId)

fun <T> FilterSpec<T>.toggleCondition(state: FilterState, fieldId: String, conditionId: String): FilterState =
    FilterEngine.toggleCondition(this, state, fieldId, conditionId)

fun <T> FilterSpec<T>.removeActiveChip(state: FilterState, chipId: String): FilterState =
    FilterEngine.removeActiveChip(this, state, chipId)

fun <T> FilterSpec<T>.activeChips(state: FilterState, languageMode: LanguageMode): List<UnifiedFilterChipItem> =
    FilterEngine.generateActiveChips(this, state, languageMode)

/**
 * Convenience search query accessors for FilterState.
 */
fun FilterState.getSearchQuery(fieldId: String = "search"): String =
    (this[fieldId] as? FilterValue.Search)?.query ?: ""

fun FilterState.withSearchQuery(query: String, fieldId: String = "search"): FilterState =
    if (query.isBlank()) this - fieldId else this + (fieldId to FilterValue.Search(query))
