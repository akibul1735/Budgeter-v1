package com.example.ui.components.filter

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.CompactFilterChipItem
import com.example.ui.components.DropdownItem
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterGroup
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterItem
import com.example.ui.components.UnifiedFilterSection
import com.example.ui.components.UnifiedGroupedMultiSelectDropdown
import com.example.ui.components.UnifiedMultiSelectDropdown
import com.example.ui.components.UnifiedSingleSelectDropdown
import com.example.ui.theme.SolidPrimary
import com.example.util.LanguageHelper

/**
 * Universal Filter Dialog built with the unified filter framework and M3 components.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> UnifiedFilterDialog(
    spec: FilterSpec<T>,
    initialState: FilterState,
    items: List<T>? = null,
    countProvider: ((FilterState) -> Int)? = null,
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA

    // Draft local state
    var draftState by remember { mutableStateOf(initialState) }

    val activeRuleCount = remember(draftState, spec) {
        spec.activeFilterCount(draftState)
    }

    // Result count calculation
    val resultMatchCount = remember(draftState, items, countProvider) {
        if (countProvider != null) {
            countProvider.invoke(draftState)
        } else if (items != null) {
            spec.count(items, draftState)
        } else {
            null
        }
    }

    val dialogTitle = if (isBangla) spec.titleBn else spec.titleEn

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "unified_filter_dialog_${spec.key}"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UnifiedFilterHeader(
                title = dialogTitle,
                activeCount = activeRuleCount,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                    Toast.makeText(
                        context,
                        if (isBangla) "সব ফিল্টার রিসেট করা হয়েছে" else "All filters reset",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismiss = onDismiss
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                for (field in spec.fields) {
                    val fieldTitle = if (isBangla) field.titleBn else field.titleEn
                    val isFieldActive = remember(draftState, field) {
                        when (val v = draftState[field.id]) {
                            is FilterValue.Select -> v.selectedIds.isNotEmpty()
                            is FilterValue.SingleSelect -> v.selectedId != null
                            is FilterValue.Range -> v.min != null || v.max != null
                            is FilterValue.Date -> (v.presetId != null && v.presetId != (field as? FilterField.DateField<*>)?.defaultPresetId) || v.startMs != null || v.endMs != null
                            is FilterValue.ToggleGroup -> v.activeIds.isNotEmpty()
                            is FilterValue.Sort -> v.sortId != null && v.sortId != (field as? FilterField.SortField<*>)?.defaultSortId
                            is FilterValue.Search -> v.query.isNotBlank()
                            is FilterValue.BooleanVal -> v.value != (field as? FilterField.BooleanField<*>)?.defaultValue
                            is FilterValue.Custom -> v.rawJson.isNotBlank()
                            null -> false
                        }
                    }

                    UnifiedFilterSection(
                        title = fieldTitle,
                        icon = field.icon,
                        trailing = {
                            if (isFieldActive) {
                                TextButton(
                                    onClick = { draftState = draftState - field.id },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "মুছুন" else "Clear",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    ) {
                        when (field) {
                            is FilterField.SelectField<T> -> {
                                RenderSelectField(
                                    field = field,
                                    currentValue = draftState[field.id],
                                    onUpdate = { draftState = draftState + (field.id to it) },
                                    languageMode = languageMode
                                )
                            }

                            is FilterField.RangeField<T> -> {
                                RenderRangeField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.Range,
                                    onUpdate = { draftState = draftState + (field.id to it) },
                                    languageMode = languageMode,
                                    accentColor = spec.accentColor
                                )
                            }

                            is FilterField.DateField<T> -> {
                                RenderDateField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.Date,
                                    onUpdate = { draftState = draftState + (field.id to it) },
                                    languageMode = languageMode
                                )
                            }

                            is FilterField.ToggleGroupField<T> -> {
                                RenderToggleGroupField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.ToggleGroup,
                                    onToggle = { condId ->
                                        draftState = spec.toggleCondition(draftState, field.id, condId)
                                    },
                                    languageMode = languageMode,
                                    accentColor = spec.accentColor
                                )
                            }

                            is FilterField.SortField<T> -> {
                                RenderSortField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.Sort,
                                    onUpdate = { draftState = draftState + (field.id to it) },
                                    languageMode = languageMode
                                )
                            }

                            is FilterField.SearchField<T> -> {
                                RenderSearchField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.Search,
                                    onUpdate = { draftState = if (it.query.isBlank()) draftState - field.id else draftState + (field.id to it) },
                                    languageMode = languageMode
                                )
                            }

                            is FilterField.BooleanField<T> -> {
                                RenderBooleanField(
                                    field = field,
                                    currentValue = draftState[field.id] as? FilterValue.BooleanVal,
                                    onUpdate = { draftState = draftState + (field.id to it) },
                                    languageMode = languageMode,
                                    accentColor = spec.accentColor
                                )
                            }

                            is FilterField.CustomField<T> -> {
                                field.content(
                                    draftState,
                                    { newValue -> draftState = draftState + (field.id to newValue) },
                                    languageMode,
                                    spec.accentColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
            }

            // Footer (Distinguishing active rule count from matching result count)
            UnifiedFilterFooter(
                activeRulesCount = activeRuleCount,
                matchCount = resultMatchCount,
                languageMode = languageMode,
                onReset = {
                    draftState = emptyMap()
                },
                onDismiss = onDismiss,
                onApply = {
                    onApply(draftState)
                }
            )
        }
    }
}

@Composable
private fun <T> RenderSelectField(
    field: FilterField.SelectField<T>,
    currentValue: FilterValue?,
    onUpdate: (FilterValue) -> Unit,
    languageMode: LanguageMode
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val allItems = field.itemsProvider?.invoke() ?: field.items

    if (field.isHierarchical) {
        val groups = remember(allItems, isBangla) {
            allItems.groupBy { it.groupKey ?: "default" }.map { (key, list) ->
                val first = list.first()
                UnifiedFilterGroup(
                    groupKey = key,
                    groupTitle = if (isBangla) first.groupTitleBn ?: first.titleBn else first.groupTitleEn ?: first.titleEn,
                    groupSubtitle = null,
                    icon = first.icon,
                    color = first.color,
                    items = list.map { item ->
                        UnifiedFilterItem(
                            id = item.id,
                            title = if (isBangla) item.titleBn else item.titleEn,
                            subtitle = if (isBangla) item.subtitleBn else item.subtitleEn,
                            color = item.color,
                            icon = item.icon
                        )
                    }
                )
            }
        }

        val selectedIds = (currentValue as? FilterValue.Select)?.selectedIds ?: emptySet()

        UnifiedGroupedMultiSelectDropdown(
            label = if (isBangla) field.titleBn else field.titleEn,
            groups = groups,
            selectedIds = selectedIds,
            onSelectionChanged = { newSet ->
                if (newSet.isEmpty()) onUpdate(FilterValue.Select(emptySet()))
                else onUpdate(FilterValue.Select(newSet))
            },
            placeholder = if (isBangla) "(সকল)" else "(All)",
            leadingIcon = field.icon,
            languageMode = languageMode
        )
    } else if (field.isMultiSelect) {
        val dropdownItems = remember(allItems, isBangla) {
            allItems.map { item ->
                DropdownItem(
                    id = item.id,
                    title = if (isBangla) item.titleBn else item.titleEn,
                    subtitle = if (isBangla) item.subtitleBn else item.subtitleEn,
                    icon = item.icon,
                    color = item.color
                )
            }
        }
        val selectedIds = (currentValue as? FilterValue.Select)?.selectedIds ?: emptySet()

        UnifiedMultiSelectDropdown(
            label = if (isBangla) field.titleBn else field.titleEn,
            items = dropdownItems,
            selectedIds = selectedIds,
            onSelectionChanged = { onUpdate(FilterValue.Select(it)) },
            placeholder = if (isBangla) "(সকল)" else "(All)",
            leadingIcon = field.icon,
            languageMode = languageMode
        )
    } else {
        val dropdownItems = remember(allItems, isBangla) {
            allItems.map { item ->
                DropdownItem(
                    id = item.id,
                    title = if (isBangla) item.titleBn else item.titleEn,
                    subtitle = if (isBangla) item.subtitleBn else item.subtitleEn,
                    icon = item.icon,
                    color = item.color
                )
            }
        }
        val selectedId = (currentValue as? FilterValue.SingleSelect)?.selectedId ?: ""

        UnifiedSingleSelectDropdown(
            label = if (isBangla) field.titleBn else field.titleEn,
            items = dropdownItems,
            selectedId = selectedId,
            onSelectionChanged = { onUpdate(FilterValue.SingleSelect(it)) },
            placeholder = if (isBangla) "(কোনটি নয়)" else "(None)",
            leadingIcon = field.icon,
            languageMode = languageMode
        )
    }
}

@Composable
private fun <T> RenderRangeField(
    field: FilterField.RangeField<T>,
    currentValue: FilterValue.Range?,
    onUpdate: (FilterValue) -> Unit,
    languageMode: LanguageMode,
    accentColor: Color
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val minVal = currentValue?.min
    val maxVal = currentValue?.max

    var minText by remember(minVal) { mutableStateOf(minVal?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var maxText by remember(maxVal) { mutableStateOf(maxVal?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = minText,
            onValueChange = { input ->
                minText = input
                val parsed = input.toDoubleOrNull()
                onUpdate(FilterValue.Range(min = parsed, max = currentValue?.max))
            },
            label = { Text(if (isBangla) field.minHintBn else field.minHintEn, fontSize = 11.sp) },
            prefix = { Text(field.prefix, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor) },
            trailingIcon = {
                if (minText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            minText = ""
                            onUpdate(FilterValue.Range(min = null, max = currentValue?.max))
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = maxText,
            onValueChange = { input ->
                maxText = input
                val parsed = input.toDoubleOrNull()
                onUpdate(FilterValue.Range(min = currentValue?.min, max = parsed))
            },
            label = { Text(if (isBangla) field.maxHintBn else field.maxHintEn, fontSize = 11.sp) },
            prefix = { Text(field.prefix, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor) },
            trailingIcon = {
                if (maxText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            maxText = ""
                            onUpdate(FilterValue.Range(min = currentValue?.min, max = null))
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> RenderDateField(
    field: FilterField.DateField<T>,
    currentValue: FilterValue.Date?,
    onUpdate: (FilterValue) -> Unit,
    languageMode: LanguageMode
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val activePresetId = currentValue?.presetId ?: field.defaultPresetId

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        field.presets.forEach { preset ->
            val isSelected = activePresetId == preset.id
            CompactFilterChipItem(
                selected = isSelected,
                onClick = {
                    val range = preset.calculateRange()
                    onUpdate(FilterValue.Date(presetId = preset.id, startMs = range.first, endMs = range.second))
                },
                label = if (isBangla) preset.titleBn else preset.titleEn
            )
        }
    }
}

@Composable
private fun <T> RenderToggleGroupField(
    field: FilterField.ToggleGroupField<T>,
    currentValue: FilterValue.ToggleGroup?,
    onToggle: (String) -> Unit,
    languageMode: LanguageMode,
    accentColor: Color
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val activeIds = currentValue?.activeIds ?: emptySet()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            field.conditions.forEachIndexed { index, cond ->
                val isActive = activeIds.contains(cond.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggle(cond.id) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cond.icon != null) {
                            Icon(
                                imageVector = cond.icon,
                                contentDescription = null,
                                tint = if (isActive) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isBangla) cond.titleBn else cond.titleEn,
                                fontSize = 12.5.sp,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val sub = if (isBangla) cond.subtitleBn else cond.subtitleEn
                            if (!sub.isNullOrBlank()) {
                                Text(
                                    text = sub,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggle(cond.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        ),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                if (index < field.conditions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))
                }
            }
        }
    }
}

@Composable
private fun <T> RenderSortField(
    field: FilterField.SortField<T>,
    currentValue: FilterValue.Sort?,
    onUpdate: (FilterValue) -> Unit,
    languageMode: LanguageMode
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val activeSortId = currentValue?.sortId ?: field.defaultSortId

    val dropdownItems = remember(field.options, isBangla) {
        field.options.map { opt ->
            DropdownItem(
                id = opt.id,
                title = if (isBangla) opt.titleBn else opt.titleEn
            )
        }
    }

    UnifiedSingleSelectDropdown(
        label = if (isBangla) field.titleBn else field.titleEn,
        items = dropdownItems,
        selectedId = activeSortId ?: "",
        onSelectionChanged = { onUpdate(FilterValue.Sort(it)) },
        leadingIcon = field.icon,
        languageMode = languageMode
    )
}

@Composable
private fun <T> RenderSearchField(
    field: FilterField.SearchField<T>,
    currentValue: FilterValue.Search?,
    onUpdate: (FilterValue.Search) -> Unit,
    languageMode: LanguageMode
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val text = currentValue?.query ?: ""
    OutlinedTextField(
        value = text,
        onValueChange = { onUpdate(FilterValue.Search(it)) },
        placeholder = {
            Text(
                text = if (isBangla) field.hintBn else field.hintEn,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingIcon = {
            Icon(
                imageVector = field.icon ?: Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onUpdate(FilterValue.Search("")) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("filter_search_field_${field.id}")
    )
}

@Composable
private fun <T> RenderBooleanField(
    field: FilterField.BooleanField<T>,
    currentValue: FilterValue.BooleanVal?,
    onUpdate: (FilterValue.BooleanVal) -> Unit,
    languageMode: LanguageMode,
    accentColor: Color
) {
    val isChecked = currentValue?.value ?: field.defaultValue
    val isBangla = languageMode == LanguageMode.BANGLA
    val subtitle = if (isBangla) field.subtitleBn else field.subtitleEn

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isChecked != field.defaultValue) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isChecked != field.defaultValue) accentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onUpdate(FilterValue.BooleanVal(!isChecked)) }
            .testTag("filter_boolean_${field.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) field.titleBn else field.titleEn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isChecked,
                onCheckedChange = { onUpdate(FilterValue.BooleanVal(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                )
            )
        }
    }
}

/**
 * Unified Active Filter Bar component displaying applied filter chips with individual remove and clear-all actions.
 */
@Composable
fun <T> UnifiedActiveFilterBar(
    spec: FilterSpec<T>,
    state: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onOpenFilterDialog: () -> Unit,
    accentColor: Color = spec.accentColor,
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!spec.isActive(state)) return

    val isBangla = languageMode == LanguageMode.BANGLA
    val activeChips = remember(spec, state, languageMode) {
        spec.activeChips(state, languageMode)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.30f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Bar with Section Badge, Active count & Clear All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenFilterDialog() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${if (isBangla) spec.titleBn else spec.titleEn} (${activeChips.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = if (isBangla) "সক্রিয় ফিল্টার নিয়ম" else "Active Filter Rules Applied",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    onClick = {
                        if (onClearAll != null) onClearAll()
                        else onFilterChange(emptyMap())
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear All Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Horizontal scrolling chip strip with individual close (X) buttons
            if (activeChips.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeChips.forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 7.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (chip.icon != null) {
                                    Icon(
                                        imageVector = chip.icon,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                                Text(
                                    text = chip.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val newState = spec.removeActiveChip(state, chip.id)
                                            onFilterChange(newState)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
