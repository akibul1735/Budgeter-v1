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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountInclusionViewMode

/**
 * Modern M3 segmented toggle bar for switching between:
 * 1. "All Active view": Displays all active accounts and unadjusted balances.
 * 2. "All Included view": Displays accounts included in calculation with effective balances.
 */
@Composable
fun AccountInclusionToggleBar(
    selectedMode: AccountInclusionViewMode,
    onModeSelected: (AccountInclusionViewMode) -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
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
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option 1: All Active view
            val isAllActive = selectedMode == AccountInclusionViewMode.ALL_ACTIVE
            val activeBgColor by animateColorAsState(
                targetValue = if (isAllActive) SolidIncome else Color.Transparent,
                animationSpec = tween(180),
                label = "active_bg_anim"
            )
            val activeContentColor by animateColorAsState(
                targetValue = if (isAllActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(180),
                label = "active_text_anim"
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onModeSelected(AccountInclusionViewMode.ALL_ACTIVE) }
                    .testTag("toggle_all_active_view"),
                shape = RoundedCornerShape(9.dp),
                color = activeBgColor,
                shadowElevation = if (isAllActive) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = activeContentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "সকল সক্রিয়" else "All Active",
                        fontSize = 12.sp,
                        fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Medium,
                        color = activeContentColor
                    )
                    if (activeCount != null && activeCount > 0) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAllActive) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$activeCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeContentColor
                            )
                        }
                    }
                }
            }

            // Option 2: All Included view
            val isAllIncluded = selectedMode == AccountInclusionViewMode.ALL_INCLUDED
            val includedBgColor by animateColorAsState(
                targetValue = if (isAllIncluded) SolidPrimary else Color.Transparent,
                animationSpec = tween(180),
                label = "included_bg_anim"
            )
            val includedContentColor by animateColorAsState(
                targetValue = if (isAllIncluded) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(180),
                label = "included_text_anim"
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onModeSelected(AccountInclusionViewMode.ALL_INCLUDED) }
                    .testTag("toggle_all_included_view"),
                shape = RoundedCornerShape(9.dp),
                color = includedBgColor,
                shadowElevation = if (isAllIncluded) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = null,
                        tint = includedContentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "সকল অন্তর্ভুক্ত" else "All Included",
                        fontSize = 12.sp,
                        fontWeight = if (isAllIncluded) FontWeight.Bold else FontWeight.Medium,
                        color = includedContentColor
                    )
                    if (includedCount != null && includedCount > 0) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAllIncluded) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$includedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = includedContentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
