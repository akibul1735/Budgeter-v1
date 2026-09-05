package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlin.math.abs

/**
 * State manager for auto-hiding the top header (with menu button, title, search, filters)
 * on downward scroll and revealing it on upward scroll.
 */
class HeaderScrollState(
    initialVisible: Boolean = true
) {
    var isVisible by mutableStateOf(initialVisible)
    private var accumulatedDelta = 0f
    // Distance in pixels of continuous scroll needed to trigger hide or show
    private val scrollThreshold = 18f

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y

            // Ignore tiny touch jitter
            if (abs(delta) < 1.5f) {
                return Offset.Zero
            }

            if (delta < 0f) {
                // Scrolling down (finger moves upward)
                if (accumulatedDelta > 0f) {
                    accumulatedDelta = 0f
                }
                accumulatedDelta += delta
                if (accumulatedDelta < -scrollThreshold && isVisible) {
                    isVisible = false
                    accumulatedDelta = 0f
                }
            } else if (delta > 0f) {
                // Scrolling up (finger moves downward)
                if (accumulatedDelta < 0f) {
                    accumulatedDelta = 0f
                }
                accumulatedDelta += delta
                if (accumulatedDelta > scrollThreshold && !isVisible) {
                    isVisible = true
                    accumulatedDelta = 0f
                }
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            // When user reaches the top and continues pulling down, guarantee visibility
            if (available.y > 0f && !isVisible) {
                isVisible = true
                accumulatedDelta = 0f
            }
            return Offset.Zero
        }
    }

    fun show() {
        isVisible = true
        accumulatedDelta = 0f
    }

    fun hide() {
        isVisible = false
        accumulatedDelta = 0f
    }
}

val LocalHeaderScrollState = compositionLocalOf { HeaderScrollState(true) }

@Composable
fun rememberHeaderScrollState(): HeaderScrollState {
    return remember { HeaderScrollState(initialVisible = true) }
}

@Composable
fun AutoHidingHeaderContainer(
    modifier: Modifier = Modifier,
    headerScrollState: HeaderScrollState = LocalHeaderScrollState.current,
    forceVisible: Boolean = false,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = forceVisible || headerScrollState.isVisible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = 150)),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 150)),
        modifier = modifier
    ) {
        content()
    }
}
