package com.makemission.makeflow.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

// ── Expressive Easing Engine ─────────────────────────────────────────
object ExpressiveMotion {
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val StandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val StandardAccelerate = CubicBezierEasing(0.2f, 0.0f, 1.0f, 1.0f)

    const val StaggerDelayMillis = 18
    const val MaxStaggerIndex = 6
    val StaggerOffset = 8.dp
    val StandardMorph = tween<Float>(durationMillis = 500, easing = EmphasizedEasing)

    fun <T> bouncySpring(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)
}

// ── Motion Spec for Interactive Components ───────────────────────────
@Immutable
data class ExpressiveMotionSpec(
    val pressScale: Float,
    val releaseScale: Float,
    val longPressScale: Float,
    val scaleAnimationSpec: AnimationSpec<Float>,
    val colorAnimationSpec: AnimationSpec<Color>,
    val scalePressDown: Float,
    val elevationPressIncrease: Dp,
    val elevationDefault: Dp,
) {
    companion object {
        val Default = ExpressiveMotionSpec(
            pressScale = 0.96f,
            releaseScale = 1f,
            longPressScale = 0.94f,
            scaleAnimationSpec = spring<Float>(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            ),
            colorAnimationSpec = tween<Color>(
                durationMillis = 180,
                easing = ExpressiveMotion.EmphasizedEasing
            ),
            scalePressDown = 0.96f,
            elevationPressIncrease = 4.dp,
            elevationDefault = 0.dp,
        )
    }
}

val LocalExpressiveMotion = staticCompositionLocalOf { ExpressiveMotionSpec.Default }

// ── Shared Overlay / Screen Transition Specs ──────────────────────────
object OverlayTransition {

    val navigationCrossfade = tween<Float>(
        durationMillis = 220,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val reducedNavigationCrossfade = tween<Float>(
        durationMillis = 90,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val valueChange = tween<Float>(durationMillis = 240, easing = ExpressiveMotion.EmphasizedEasing)
    val progressChange = tween<Float>(durationMillis = 220, easing = ExpressiveMotion.EmphasizedEasing)
    val reducedProgress = tween<Float>(
        durationMillis = 100,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val contentSizeChange = tween<IntSize>(durationMillis = 220, easing = ExpressiveMotion.EmphasizedEasing)
    val reducedContentSize = tween<IntSize>(
        durationMillis = 100,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val reducedColor = tween<Color>(
        durationMillis = 100,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val fastEnter = tween<Float>(durationMillis = 120, easing = ExpressiveMotion.StandardDecelerate)
    val fastExit = tween<Float>(durationMillis = 90, easing = ExpressiveMotion.StandardDecelerate)
    val sequentialContentExit = tween<Float>(durationMillis = 90, easing = ExpressiveMotion.StandardDecelerate)
    val sequentialContentEnter = tween<Float>(
        durationMillis = 120,
        delayMillis = 90,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val reducedSequentialContentExit = tween<Float>(durationMillis = 40, easing = ExpressiveMotion.StandardDecelerate)
    val reducedSequentialContentEnter = tween<Float>(
        durationMillis = 50,
        delayMillis = 40,
        easing = ExpressiveMotion.StandardDecelerate
    )
    val itemPlacementChange = tween<IntOffset>(durationMillis = 220, easing = ExpressiveMotion.EmphasizedEasing)
    const val TabSlideFraction = 1f / 6f
    const val TabEnterScale = 0.985f
    const val TabExitScale = 0.99f
    const val NavSelectedIconScale = 1.06f
    const val ValueShiftFraction = 0.16f

    val tabScaleEnter = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val navSelectionScale = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // ── Float specs (fade, scale, progress) ──
    val enterFade = tween<Float>(durationMillis = 220, easing = ExpressiveMotion.StandardDecelerate)
    val exitFade = tween<Float>(durationMillis = 160, easing = ExpressiveMotion.StandardDecelerate)
    val reducedEnter = tween<Float>(durationMillis = 100, easing = ExpressiveMotion.StandardDecelerate)
    val reducedExit = tween<Float>(durationMillis = 80, easing = ExpressiveMotion.StandardDecelerate)
    // Reserve bounce for illustrations and explicitly playful moments.
    val bouncyEnter = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val bouncyExit = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // ── IntOffset specs (slide) ──
    val enterSlide = tween<IntOffset>(durationMillis = 220, easing = ExpressiveMotion.StandardDecelerate)
    val exitSlide = tween<IntOffset>(durationMillis = 160, easing = ExpressiveMotion.StandardDecelerate)
    val reducedEnterSlide = tween<IntOffset>(durationMillis = 100, easing = ExpressiveMotion.StandardDecelerate)
    val reducedExitSlide = tween<IntOffset>(durationMillis = 80, easing = ExpressiveMotion.StandardDecelerate)
    val bouncyEnterSlide = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val bouncyExitSlide = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
