package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Remembers a boolean state that toggles to false when scrolling down and true when scrolling up.
 */
@Composable
fun rememberAutoScrollVisibility(): AutoScrollVisibilityState {
    var isVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -8f && isVisible) {
                    isVisible = false
                } else if (delta > 8f && !isVisible) {
                    isVisible = true
                }
                return Offset.Zero
            }
        }
    }

    return remember(isVisible, nestedScrollConnection) {
        AutoScrollVisibilityState(isVisible, nestedScrollConnection) { isVisible = it }
    }
}

class AutoScrollVisibilityState(
    val isVisible: Boolean,
    val nestedScrollConnection: NestedScrollConnection,
    val setVisible: (Boolean) -> Unit
)

@Composable
fun AutoHidingContainer(
    visibilityState: AutoScrollVisibilityState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visibilityState.isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
