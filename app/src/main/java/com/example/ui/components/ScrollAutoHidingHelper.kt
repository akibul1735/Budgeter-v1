package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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

/**
 * State manager for auto-hiding the top header (with menu button, title, search, filters)
 * on downward scroll and revealing it on upward scroll.
 */
class HeaderScrollState(
    initialVisible: Boolean = true
) {
    var isVisible by mutableStateOf(initialVisible)

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            // When scrolling downwards (delta < 0), hide the top header
            if (delta < -8f && isVisible) {
                isVisible = false
            }
            // When scrolling upwards (delta > 0), reveal the top header
            else if (delta > 8f && !isVisible) {
                isVisible = true
            }
            return Offset.Zero
        }
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
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
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
