package com.natijeh.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.dp

object NatijehSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object NatijehRadius {
    val chip = 10.dp
    val card = 16.dp
    val hero = 24.dp
}

object NatijehSize {
    val minimumTouchTarget = 48.dp
    val icon = 20.dp
    val teamLogo = 44.dp
    val profileLogo = 84.dp
}

object NatijehMotion {
    const val quick = 140
    const val standard = 240
    const val relaxed = 360
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
