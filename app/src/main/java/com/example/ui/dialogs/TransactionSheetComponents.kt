package com.example.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.DisplayFormatPreferences
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun OptionRowItem(
    icon: @Composable () -> Unit,
    title: String,
    subTitle: String? = null,
    isTwoLine: Boolean = false,
    livePreview: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = if ((isTwoLine && subTitle != null) || livePreview != null) 7.5.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            icon()
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (isTwoLine && subTitle != null) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (livePreview != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    livePreview()
                }
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

internal fun parseItemColor(hex: String?, fallback: Color = Color(0xFFEA580C)): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

internal fun formatSmartCurrency(amount: Double, languageMode: LanguageMode): String {
    val isWhole = Math.abs(amount % 1.0) < 0.001
    val base = LanguageHelper.formatCurrency(amount, languageMode)
    return if (isWhole) {
        base.replace(".00", "").replace(".০০", "")
    } else {
        base
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun LiveImpactPill(
    currentAmount: Double,
    projectedAmount: Double,
    delta: Double,
    languageMode: LanguageMode,
    tintColor: Color,
    prefix: String? = null,
    hasActiveInput: Boolean = false,
    budgetLimit: Double? = null
) {
    val curFmt = formatSmartCurrency(currentAmount, languageMode)
    val projFmt = formatSmartCurrency(projectedAmount, languageMode)
    val isPositive = delta >= 0
    val deltaSign = if (isPositive) "+" else "−"
    val absDeltaFmt = formatSmartCurrency(Math.abs(delta), languageMode)
    val deltaFmt = "$deltaSign$absDeltaFmt"

    Column(modifier = Modifier.padding(top = 2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.5.dp)
        ) {
            if (!prefix.isNullOrBlank()) {
                Text(
                    text = "$prefix:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = curFmt,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )

            if (hasActiveInput) {
                // Delta indicator e.g. (+৳100) or (−৳100)
                Text(
                    text = "($deltaFmt)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = tintColor
                )

                Text(
                    text = "➔",
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                )

                // [New balance] framed in a small, clean, subtle card
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    )
                ) {
                    AnimatedContent(
                        targetState = projFmt,
                        transitionSpec = {
                            (slideInVertically { height -> height / 2 } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
                        },
                        label = "projAnim"
                    ) { targetProj ->
                        Text(
                            text = targetProj,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Mini Budget Progress Bar (if budget limit exists and > 0)
        if (budgetLimit != null && budgetLimit > 0.0) {
            val progress = (projectedAmount / budgetLimit).toFloat().coerceIn(0f, 1.25f)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "budgetProgress"
            )
            val budgetPercent = ((projectedAmount / budgetLimit) * 100).toInt()
            val barColor = when {
                progress > 1.0f -> SolidExpense
                progress > 0.80f -> Color(0xFFF59E0B)
                else -> SolidIncome
            }

            Spacer(modifier = Modifier.height(3.5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress.coerceAtMost(1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }

                Text(
                    text = "${formatSmartCurrency(projectedAmount, languageMode)} / ${formatSmartCurrency(budgetLimit, languageMode)} ($budgetPercent%)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor
                )
            }
        }
    }
}

