package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import kotlinx.coroutines.delay

private val EmeraldDark = Color(0xFF047857)
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldLight = Color(0xFF34D399)
private val GoldAccent = Color(0xFFF59E0B)
private val GoldLight = Color(0xFFFBBF24)

/**
 * High-fidelity App Open Loading Splash Screen.
 *
 * Features:
 * - Emerald circular emblem with golden crescent accent & white Bengali Taka symbol (৳).
 * - Smooth rhythmic breathing pulse and ambient radial shimmer glow.
 * - Dynamic loading status text cycling through workspace initialization states.
 * - Sleek slim progress bar with gradient sweep.
 * - Guarantees minimum display time (800ms) and smoothly transitions out immediately
 *   when initial data is ready.
 */
@Composable
fun AppSplashScreen(
    isDataReady: Boolean,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    minDisplayTimeMs: Long = 800L,
    onFinished: () -> Unit = {}
) {
    val isBn = languageMode == LanguageMode.BANGLA

    val statusMessages = remember(isBn) {
        if (isBn) {
            listOf(
                "ওয়ার্কস্পেস শুরু হচ্ছে...",
                "বাজেট লোড হচ্ছে...",
                "অ্যাকাউন্ট ও ক্যাটাগরি প্রস্তুত হচ্ছে...",
                "প্রস্তুত!"
            )
        } else {
            listOf(
                "Initializing workspace...",
                "Loading your budget...",
                "Preparing accounts & categories...",
                "Ready!"
            )
        }
    }

    var currentStatusIndex by remember { mutableIntStateOf(0) }
    var minTimeElapsed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    // Rhythmic breathing / pulse animation for the emblem
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfiniteTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Shimmer offset for the progress indicator
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    // Handle min display time and status message cycling
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        // Cycle status messages
        var idx = 0
        while (!minTimeElapsed || !isDataReady) {
            delay(260L)
            if (idx < statusMessages.size - 2) {
                idx++
                currentStatusIndex = idx
            }
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= minDisplayTimeMs) {
                minTimeElapsed = true
            }
        }
    }

    // Dismiss trigger when both min time has elapsed and initial data is ready
    LaunchedEffect(minTimeElapsed, isDataReady) {
        if (minTimeElapsed && isDataReady) {
            currentStatusIndex = statusMessages.lastIndex
            delay(120L) // brief flash of "Ready!"
            isVisible = false
            delay(400L) // await fadeOut animation
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(380, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_splash_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                // Subtle ambient background gradient
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.7f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.08f * glowAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    // Outer Glow & Emblem Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                    ) {
                        // Ambient Pulse Halo
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = EmeraldLight.copy(alpha = 0.22f * glowAlpha),
                                radius = size.minDimension / 2f
                            )
                        }

                        // Circular Emblem with Dual-tone split green & Golden crescent
                        EmblemIcon(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // App Title
                    Text(
                        text = if (isBn) "বাজেটার" else "Budgeter",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tagline
                    Text(
                        text = if (isBn) "স্মার্ট বাজেট ও ব্যক্তিগত অর্থ ট্র্যাকার" else "Smart Personal Finance & Budget",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Sleek Slim Gradient Progress Bar
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = size.width
                            val headX = (shimmerTranslate * barWidth).coerceIn(0f, barWidth)
                            val tailX = ((shimmerTranslate - 0.5f) * barWidth).coerceIn(0f, barWidth)
                            if (headX > tailX) {
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            EmeraldDark,
                                            EmeraldPrimary,
                                            GoldLight,
                                            EmeraldPrimary
                                        ),
                                        startX = tailX,
                                        endX = headX
                                    ),
                                    start = Offset(tailX, size.height / 2f),
                                    end = Offset(headX, size.height / 2f),
                                    strokeWidth = size.height,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Status Indicator Text
                    Text(
                        text = statusMessages.getOrElse(currentStatusIndex) { statusMessages[0] },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("splash_status_text")
                    )
                }

                // Bottom Branding / Version indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "v4.12 • Budgeter",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Custom drawing of the signature Budgeter launcher emblem:
 * Emerald green circular emblem with dual-tone green split, golden crescent accent, and Bengali Taka symbol (৳).
 */
@Composable
private fun EmblemIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(
                width = 0.8.dp,
                color = GoldAccent.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val d = size.minDimension
            val radius = d / 2f
            val centerPt = Offset(size.width / 2f, size.height / 2f)

            // 1. Dual-tone split background (Left: darker emerald, Right: vibrant emerald)
            // Left Half
            drawArc(
                color = EmeraldDark,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(centerPt.x - radius, centerPt.y - radius),
                size = Size(d, d)
            )
            // Right Half
            drawArc(
                color = EmeraldPrimary,
                startAngle = 270f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(centerPt.x - radius, centerPt.y - radius),
                size = Size(d, d)
            )

            // 2. Golden Crescent Accent along the top-right / right curve
            drawArc(
                color = GoldAccent,
                startAngle = -65f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(centerPt.x - radius + 3.dp.toPx(), centerPt.y - radius + 3.dp.toPx()),
                size = Size(d - 6.dp.toPx(), d - 6.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 3. Crisp white Bangladeshi Taka symbol (৳) centered
        Text(
            text = "৳",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-1.5).dp)
        )
    }
}
