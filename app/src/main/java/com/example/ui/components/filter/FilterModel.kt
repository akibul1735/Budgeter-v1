package com.example.ui.components.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.LanguageMode

/**
 * FilterValue: Strongly typed value representation for any filter field.
 * Serializable to JSON via FilterStore.
 */
sealed interface FilterValue {
    data class Select(val selectedIds: Set<String> = emptySet()) : FilterValue
    data class SingleSelect(val selectedId: String? = null) : FilterValue
    data class Range(val min: Double? = null, val max: Double? = null) : FilterValue
    data class Date(
        val presetId: String? = null,
        val startMs: Long? = null,
        val endMs: Long? = null
    ) : FilterValue
    data class ToggleGroup(val activeIds: Set<String> = emptySet()) : FilterValue
    data class Sort(val sortId: String? = null) : FilterValue
    data class Search(val query: String = "") : FilterValue
    data class BooleanVal(val value: Boolean = false) : FilterValue
    data class Custom(val rawJson: String = "") : FilterValue
}

/**
 * FilterState: Map of field ID to its current FilterValue.
 */
typealias FilterState = Map<String, FilterValue>

/**
 * Condition definition for ToggleGroupField.
 * [group] enables mutual exclusivity across conditions sharing the same group name.
 */
data class FilterCondition<T>(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String? = null,
    val subtitleBn: String? = null,
    val icon: ImageVector? = null,
    val group: String? = null,
    val predicate: ((T) -> Boolean)? = null
)

/**
 * Selectable item option for SelectField.
 */
data class SelectItemOption(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String? = null,
    val subtitleBn: String? = null,
    val icon: ImageVector? = null,
    val color: Color? = null,
    val groupKey: String? = null,
    val groupTitleEn: String? = null,
    val groupTitleBn: String? = null
)

/**
 * Date preset option for DateField.
 */
data class DatePresetOption(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val calculateRange: () -> Pair<Long, Long>
)

/**
 * Sort option item for SortField.
 */
data class SortOptionItem<T>(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val comparator: Comparator<T>? = null,
    val icon: ImageVector? = null
)

/**
 * FilterField: Abstract specification for a filterable section or control.
 */
sealed interface FilterField<T> {
    val id: String
    val titleEn: String
    val titleBn: String
    val subtitleEn: String? get() = null
    val subtitleBn: String? get() = null
    val icon: ImageVector? get() = null

    /**
     * Single or multi-select dropdown field (e.g., Categories, Accounts, Labels, Statuses).
     */
    data class SelectField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val isMultiSelect: Boolean = true,
        val items: List<SelectItemOption> = emptyList(),
        val itemsProvider: (() -> List<SelectItemOption>)? = null,
        val isHierarchical: Boolean = false,
        val predicate: ((T, Set<String>) -> Boolean)? = null
    ) : FilterField<T>

    /**
     * Numeric amount / value range field.
     */
    data class RangeField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val prefix: String = "৳",
        val minHintEn: String = "Min ৳",
        val minHintBn: String = "সর্বনিম্ন ৳",
        val maxHintEn: String = "Max ৳",
        val maxHintBn: String = "সর্বোচ্চ ৳",
        val predicate: ((T, Double?, Double?) -> Boolean)? = null
    ) : FilterField<T>

    /**
     * Date range field with presets and custom date selection.
     */
    data class DateField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val presets: List<DatePresetOption> = emptyList(),
        val defaultPresetId: String? = null,
        val allowCustom: Boolean = true,
        val predicate: ((T, Long, Long) -> Boolean)? = null
    ) : FilterField<T>

    /**
     * Group of toggle switches or chips with mutual-exclusivity groups.
     */
    data class ToggleGroupField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val conditions: List<FilterCondition<T>> = emptyList()
    ) : FilterField<T>

    /**
     * Sort order selector field.
     */
    data class SortField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val options: List<SortOptionItem<T>> = emptyList(),
        val defaultSortId: String? = null
    ) : FilterField<T>

    /**
     * Search text field for keyword and substring filtering.
     */
    data class SearchField<T>(
        override val id: String = "search",
        override val titleEn: String = "Search",
        override val titleBn: String = "অনুসন্ধান",
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val hintEn: String = "Search...",
        val hintBn: String = "অনুসন্ধান করুন...",
        val predicate: ((T, String) -> Boolean)? = null
    ) : FilterField<T>

    /**
     * Single boolean switch toggle field (e.g., Exclude Zero Amounts, Include Inactive Accounts).
     */
    data class BooleanField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val defaultValue: Boolean = false,
        val predicate: ((T, Boolean) -> Boolean)? = null
    ) : FilterField<T>

    /**
     * Custom slot for screen-specific UI controls with optional predicate.
     */
    data class CustomField<T>(
        override val id: String,
        override val titleEn: String,
        override val titleBn: String,
        override val subtitleEn: String? = null,
        override val subtitleBn: String? = null,
        override val icon: ImageVector? = null,
        val content: @Composable (state: FilterState, onUpdate: (FilterValue) -> Unit, languageMode: LanguageMode, accentColor: Color) -> Unit,
        val predicate: ((T, FilterValue) -> Boolean)? = null
    ) : FilterField<T>
}

/**
 * FilterSpec: Specification for a complete filter modal & engine configuration.
 */
data class FilterSpec<T>(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String? = null,
    val subtitleBn: String? = null,
    val icon: ImageVector? = null,
    val accentColor: Color = Color(0xFF10B981),
    val fields: List<FilterField<T>> = emptyList()
)

/**
 * Active filter chip descriptor for UnifiedActiveFilterBar.
 */
data class UnifiedFilterChipItem(
    val id: String,
    val fieldId: String,
    val label: String,
    val icon: ImageVector? = null,
    val color: Color? = null
)
