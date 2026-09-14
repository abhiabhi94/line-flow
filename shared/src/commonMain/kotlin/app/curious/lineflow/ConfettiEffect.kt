package app.curious.lineflow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.EdgeVisited
import app.curious.lineflow.ui.theme.NodeCurrent
import app.curious.lineflow.ui.theme.Success
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(Accent, Success, NodeCurrent, EdgeVisited)
private const val PARTICLE_COUNT = 60

private data class ParticleState(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val startRotation: Float
)

@Composable
fun ConfettiOverlay() {
    val particles = remember {
        List(PARTICLE_COUNT) {
            ParticleState(
                startX = 0.5f + (Random.nextFloat() - 0.5f) * 0.1f,
                startY = 0.4f,
                vx = (Random.nextFloat() - 0.5f) * 0.6f,
                vy = -Random.nextFloat() * 0.8f - 0.2f,
                color = CONFETTI_COLORS[Random.nextInt(CONFETTI_COLORS.size)],
                size = Random.nextFloat() * 6f + 4f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 400f,
                startRotation = Random.nextFloat() * 360f
            )
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2_000, easing = LinearEasing),
        label = "confetti_progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = animatedProgress
        if (t <= 0f) return@Canvas

        particles.forEach { p ->
            val gravity = 1.5f
            val x = p.startX + p.vx * t
            val y = p.startY + p.vy * t + 0.5f * gravity * t * t
            val alpha = (1f - t).coerceIn(0f, 1f)

            if (alpha > 0f && y < 1.2f) {
                val cx = x * size.width
                val cy = y * size.height
                val rotation = p.startRotation + p.rotationSpeed * t

                rotate(degrees = rotation, pivot = Offset(cx, cy)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(cx - p.size, cy - p.size / 2),
                        size = Size(p.size * 2, p.size)
                    )
                }
            }
        }
    }
}
