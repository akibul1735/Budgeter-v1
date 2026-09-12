package com.example.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal to notify parent containers (such as MainAppContainer)
 * when a timeline view is currently active/displayed, allowing them to disable
 * horizontal tab swiping so users can scroll wide timeline tables without accidental tab switches.
 */
val LocalSetTimelineActive = compositionLocalOf<(Boolean) -> Unit> { {} }
