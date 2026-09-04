package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.data.model.LanguageMode
import com.example.util.LanguageHelper

/**
 * Clickable amount display component that provides visual feedback and optional info icon indicator
 * inviting the user to tap to see calculation or related transactions.
 */
@Composable
fun ClickableAmountText(
    amount: Double,
    languageMode: LanguageMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current,
    showInfoIndicator: Boolean = false,
    maxLines: Int = 1
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
            Text(
                text = "$prefix${LanguageHelper.formatCurrency(amount, languageMode)}$suffix",
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                style = style,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
            if (showInfoIndicator) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Show Breakdown",
                    tint = color.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
