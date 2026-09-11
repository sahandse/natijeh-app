package com.natijeh.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun isLightTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f

@Composable
fun natijehCardElevation() = CardDefaults.cardElevation(
    defaultElevation = if (isLightTheme()) 1.5.dp else 0.dp
)

@Composable
fun PulseDot(color: Color = LiveRed, size: Dp = 7.dp) {
    val infinite = rememberInfiniteTransition(label = "live-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "live-pulse-alpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}
