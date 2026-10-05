package com.example.ui.components.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.util.LanguageHelper

/**
 * Generic quick-pill shortcuts built from the same FilterSpec.
 * Renders conditions from a ToggleGroupField as a horizontally scrolling pill strip.
 */
@Composable
fun <T> UnifiedQuickFilterPills(
    spec: FilterSpec<T>,
    fieldId: String,
    state: FilterState,
    onFilterChange: (FilterState) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    counts: Map<String, Int> = emptyMap(),
    alertConditionIds: Set<String> = emptySet(),
    onConditionSelected: ((String) -> Unit)? = null
) {
    val field = spec.fields.firstOrNull { it.id == fieldId } as? FilterField.ToggleGroupField<T> ?: return
    val activeIds = (state[fieldId] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
    val isBangla = languageMode == LanguageMode.BANGLA

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        field.conditions.forEach { condition ->
            val isSelected = activeIds.contains(condition.id)
            val count = counts[condition.id]
            val isAlert = alertConditionIds.contains(condition.id) && (count ?: 0) > 0

            val pillBgColor = when {
                isSelected -> spec.accentColor.copy(alpha = 0.16f)
                isAlert -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }
            val pillBorderColor = when {
                isSelected -> spec.accentColor
                isAlert -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
            val contentColor = when {
                isSelected -> spec.accentColor
                isAlert -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = pillBgColor,
                border = BorderStroke(if (isSelected) 1.2.dp else 0.8.dp, pillBorderColor),
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        val newState = spec.toggleCondition(state, fieldId, condition.id)
                        onFilterChange(newState)
                        onConditionSelected?.invoke(condition.id)
                    }
                    .defaultMinSize(minHeight = 36.dp)
                    .testTag("quick_pill_${condition.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (condition.icon != null) {
                        Icon(
                            imageVector = condition.icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Text(
                        text = if (isBangla) condition.titleBn else condition.titleEn,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )

                    if (count != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) spec.accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = LanguageHelper.formatNumber(count.toDouble(), languageMode, false),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generic sort dropdown button built from a SortField in FilterSpec.
 */
@Composable
fun <T> UnifiedSortDropdownButton(
    spec: FilterSpec<T>,
    fieldId: String,
    state: FilterState,
    onFilterChange: (FilterState) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    testTag: String = "quick_sort_btn"
) {
    val field = spec.fields.firstOrNull { it.id == fieldId } as? FilterField.SortField<T> ?: return
    val isBangla = languageMode == LanguageMode.BANGLA
    val activeSortId = (state[fieldId] as? FilterValue.Sort)?.sortId ?: field.defaultSortId
    val currentOption = field.options.firstOrNull { it.id == activeSortId }
    val isNonDefault = activeSortId != null && activeSortId != field.defaultSortId

    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isNonDefault) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, if (isNonDefault) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showMenu = true }
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = field.icon ?: Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = if (isNonDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = currentOption?.let { if (isBangla) it.titleBn else it.titleEn }
                        ?: if (isBangla) field.titleBn else field.titleEn,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNonDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            field.options.forEach { option ->
                val isSelected = option.id == activeSortId
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isBangla) option.titleBn else option.titleEn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = spec.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = {
                        val newState = state + (fieldId to FilterValue.Sort(option.id))
                        onFilterChange(newState)
                        showMenu = false
                    }
                )
            }
        }
    }
}

/**
 * Generic segmented toggle bar built from a ToggleGroupField with mutual exclusivity.
 * E.g. [ Expense | All | Income ] or [ All | Only Groups | Only Categories ]
 */
@Composable
fun <T> UnifiedSegmentedToggle(
    spec: FilterSpec<T>,
    fieldId: String,
    state: FilterState,
    onFilterChange: (FilterState) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    optionColors: Map<String, Color> = emptyMap(),
    testTagPrefix: String = "segmented_toggle"
) {
    val field = spec.fields.firstOrNull { it.id == fieldId } as? FilterField.ToggleGroupField<T> ?: return
    val activeIds = (state[fieldId] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
    val isBangla = languageMode == LanguageMode.BANGLA

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            field.conditions.forEach { condition ->
                val isSelected = activeIds.contains(condition.id)
                val tintColor = optionColors[condition.id] ?: spec.accentColor

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) tintColor.copy(alpha = 0.16f) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            val newState = spec.toggleCondition(state, fieldId, condition.id)
                            onFilterChange(newState)
                        }
                        .testTag("${testTagPrefix}_${condition.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (condition.icon != null) {
                            Icon(
                                imageVector = condition.icon,
                                contentDescription = null,
                                tint = if (isSelected) tintColor else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isBangla) condition.titleBn else condition.titleEn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) tintColor else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
