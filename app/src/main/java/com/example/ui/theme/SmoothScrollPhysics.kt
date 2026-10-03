package com.example.ui.theme

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import kotlin.math.abs

val LocalSmoothFlingBehavior = compositionLocalOf<FlingBehavior?> { null }

/**
 * High-performance, ultra-smooth fluid fling behavior.
 * Delivers natural kinetic momentum, gentle deceleration, and buttery-smooth easing
 * so content continues gliding naturally after releasing touch.
 */
class FluidFlingBehavior(
    private val decayAnimationSpec: DecayAnimationSpec<Float>,
    private val velocityMultiplier: Float = 1.0f
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        if (abs(initialVelocity) < 1f) return 0f

        var lastValue = 0f
        var velocityLeft = initialVelocity * velocityMultiplier
        val animationState = AnimationState(
            initialValue = 0f,
            initialVelocity = velocityLeft
        )

        try {
            animationState.animateDecay(decayAnimationSpec) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = velocity

                // If content hit edge and cannot scroll further, gracefully end fling
                if (abs(delta - consumed) > 0.5f) {
                    cancelAnimation()
                }
            }
        } catch (_: Exception) {
            // Safely swallow cancellation on gesture interrupt
        }

        return velocityLeft
    }
}

/**
 * Creates and remembers a fluid fling behavior for LazyLists, Grids, and scrollable containers.
 */
@Composable
fun rememberFluidFlingBehavior(): FlingBehavior {
    val splineDecay = rememberSplineBasedDecay<Float>()
    return remember(splineDecay) {
        FluidFlingBehavior(decayAnimationSpec = splineDecay)
    }
}

/**
 * Creates a fluid, responsive swipe fling behavior for HorizontalPager.
 * Uses gentle spring physics for smooth, natural page settling with momentum.
 */
@Composable
fun rememberFluidPagerFlingBehavior(
    pagerState: PagerState
): TargetedFlingBehavior {
    return PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        snapPositionalThreshold = 0.35f
    )
}
