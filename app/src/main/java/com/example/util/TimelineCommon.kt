package com.example.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary

enum class TimelineViewOption {
    ALL,
    GROUPS,
    ITEMS
}

@Composable
fun TimelineViewOptionsTabRow(
    selectedOption: TimelineViewOption,
    onOptionSelected: (TimelineViewOption) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        TimelineViewOption.ALL to if (languageMode == LanguageMode.BANGLA) "সব (All)" else "All",
        TimelineViewOption.GROUPS to if (languageMode == LanguageMode.BANGLA) "গ্রুপ (Groups)" else "Groups",
        TimelineViewOption.ITEMS to if (languageMode == LanguageMode.BANGLA) "আইটেম (Items)" else "Items"
    )

    val selectedIndex = options.indexOfFirst { it.first == selectedOption }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = SolidPrimary,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = SolidPrimary
                )
            }
        },
        divider = {}
    ) {
        options.forEachIndexed { index, pair ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onOptionSelected(pair.first) },
                text = {
                    Text(
                        text = pair.second,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}
