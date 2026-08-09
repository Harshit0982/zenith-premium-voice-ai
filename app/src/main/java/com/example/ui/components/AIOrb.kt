package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.OrbCore
import com.example.ui.theme.OrbGlowOuter
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleAccent

enum class OrbState {
    IDLE, LISTENING, THINKING, SPEAKING, ERROR
}

@Composable
fun AIOrb(
    state: OrbState = OrbState.IDLE,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbScale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRotation"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width / 2) * scale

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbGlowOuter.copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = center
            )

            // Inner glass sphere
            val innerBrush = Brush.sweepGradient(
                colors = listOf(
                    PurpleAccent.copy(alpha = 0.7f),
                    PinkAccent.copy(alpha = 0.5f),
                    CyanAccent.copy(alpha = 0.6f),
                    OrbCore.copy(alpha = 0.8f),
                    PurpleAccent.copy(alpha = 0.7f)
                ),
                center = center
            )

            // Rotate the inner brush effect
            withTransform({
                rotate(rotation, center)
            }) {
                drawCircle(
                    brush = innerBrush,
                    radius = radius,
                    center = center
                )
            }
            
            // Glass reflection highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 0.6f
                ),
                radius = radius,
                center = center
            )

            // Border / Edge light
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
