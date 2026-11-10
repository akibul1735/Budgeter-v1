package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountInclusionViewMode

/**
 * Modern M3 segmented toggle bar for switching between:
 * 1. "Normal view": Standard view where calculations and adjustments are configured and applied.
 * 2. "All Active view": Displays all active accounts and raw unadjusted balances.
 * 3. "All Included view": Displays accounts included in calculation with effective balances.
 */
@Composable
fun AccountInclusionToggleBar(
    selectedMode: AccountInclusionViewMode,
    onModeSelected: (AccountInclusionViewMode) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    normalCount: Int? = null,
    activeCount: Int? = null,
    includedCount: Int? = null
) {
    val isBn = languageMode == LanguageMode.BANGLA

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("account_inclusion_toggle_bar"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: Normal View (Default adjustable view)
            ToggleSegment(
                isSelected = selectedMode == AccountInclusionViewMode.NORMAL,
                title = if (isBn) "স্বাভাবিক" else "Normal",
                icon = Icons.Default.Tune,
                badgeCount = normalCount ?: activeCount,
                activeColor = SolidPrimary,
                testTag = "toggle_normal_view",
                onClick = { onModeSelected(AccountInclusionViewMode.NORMAL) },
                modifier = Modifier.weight(1f)
            )

            // Option 2: All Active view
            ToggleSegment(
                isSelected = selectedMode == AccountInclusionViewMode.ALL_ACTIVE,
                title = if (isBn) "সকল সক্রিয়" else "All Active",
                icon = Icons.Default.CheckCircle,
                badgeCount = activeCount,
                activeColor = SolidIncome,
                testTag = "toggle_all_active_view",
                onClick = { onModeSelected(AccountInclusionViewMode.ALL_ACTIVE) },
                modifier = Modifier.weight(1f)
            )

            // Option 3: All Included view
            ToggleSegment(
                isSelected = selectedMode == AccountInclusionViewMode.ALL_INCLUDED,
                title = if (isBn) "সকল অন্তর্ভুক্ত" else "All Included",
                icon = Icons.Default.Rule,
                badgeCount = includedCount,
                activeColor = MaterialTheme.colorScheme.tertiary,
                testTag = "toggle_all_included_view",
                onClick = { onModeSelected(AccountInclusionViewMode.ALL_INCLUDED) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToggleSegment(
    isSelected: Boolean,
    title: String,
    icon: ImageVector,
    badgeCount: Int?,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color.Transparent,
        animationSpec = tween(180),
        label = "segment_bg_anim"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "segment_content_anim"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(9.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}
