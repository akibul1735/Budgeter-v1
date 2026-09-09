package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CustomAppIcon

@Composable
fun AppCoinEmblem(
    icon: CustomAppIcon,
    size: Dp = 40.dp,
    elevation: Dp = 2.dp,
    modifier: Modifier = Modifier,
    testTag: String = "app_coin_emblem"
) {
    val primary = Color(icon.primaryColor)
    val secondary = Color(icon.secondaryColor)
    val symbolColor = Color(icon.takaSymbolColor)

    Surface(
        modifier = modifier
            .size(size)
            .shadow(elevation, CircleShape)
            .clip(CircleShape)
            .testTag(testTag),
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(primary, secondary),
                        radius = Float.POSITIVE_INFINITY
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Detailed Coin Engraving: Outer and Inner Ridges with 4 Accent Dots
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)

                // Outer border rim
                drawCircle(
                    color = symbolColor.copy(alpha = 0.25f),
                    radius = radius - 1.5f,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Inner fine ring
                drawCircle(
                    color = symbolColor.copy(alpha = 0.18f),
                    radius = radius * 0.82f,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 4 compass accent markers (North, South, East, West)
                val dotRadius = radius * 0.05f
                val dotDistance = radius * 0.82f
                drawCircle(symbolColor.copy(alpha = 0.4f), dotRadius, Offset(center.x, center.y - dotDistance))
                drawCircle(symbolColor.copy(alpha = 0.4f), dotRadius, Offset(center.x, center.y + dotDistance))
                drawCircle(symbolColor.copy(alpha = 0.4f), dotRadius, Offset(center.x - dotDistance, center.y))
                drawCircle(symbolColor.copy(alpha = 0.4f), dotRadius, Offset(center.x + dotDistance, center.y))
            }

            // Central Bangladeshi Taka Symbol (৳)
            val fontSize = (size.value * 0.52f).sp
            Text(
                text = "৳",
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = symbolColor
            )
        }
    }
}
