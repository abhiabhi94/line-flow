package app.curious.lineflow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.EdgeVisited
import app.curious.lineflow.ui.theme.NodeCurrent
import app.curious.lineflow.ui.theme.Success
import app.curious.lineflow.ui.theme.TextPrimary
import app.curious.lineflow.ui.theme.TextSecondary
import app.curious.lineflow.LevelManager
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private val CHAMPION_CONFETTI_COLORS = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFFFA500), // Orange
    Color(0xFFFF6B6B), // Coral
    Color(0xFFFF69B4), // Hot Pink
    Color(0xFF00CED1), // Dark Turquoise
    Color(0xFF7B68EE), // Medium Slate Blue
    Color(0xFF98FB98), // Pale Green
    Accent,
    Success,
    NodeCurrent,
    EdgeVisited
)

private const val CHAMPION_PARTICLE_COUNT = 150

private data class ChampionParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val startRotation: Float,
    val shape: ParticleShape,
    val delay: Float
)

private enum class ParticleShape {
    RECTANGLE, CIRCLE, STAR, RIBBON
}

@Composable
fun ChampionScreen(
    onBackToLevelSelect: () -> Unit
) {
    // Title animation
    val titleScale = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val starsAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        titleScale.animateTo(1f, animationSpec = tween(600, easing = EaseOutBack))
        delay(200)
        subtitleAlpha.animateTo(1f, animationSpec = tween(400))
        delay(200)
        starsAlpha.animateTo(1f, animationSpec = tween(400))
        delay(200)
        buttonAlpha.animateTo(1f, animationSpec = tween(400))
    }

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "champion_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Massive confetti falling from everywhere
        ChampionConfettiEffect()

        // Radial glow behind the card
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.6f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Accent.copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy icon
            Text(
                text = "\uD83C\uDFC6",
                fontSize = 72.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                }
            )

            Spacer(Modifier.height(24.dp))

            // Champion title with gradient effect
            Text(
                text = "CHAMPION",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Accent,
                letterSpacing = 4.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                }
            )

            Spacer(Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "You've conquered every puzzle!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Master of the one-line path",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                }
            )

            Spacer(Modifier.height(32.dp))

            // Star row with animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.graphicsLayer {
                    alpha = starsAlpha.value
                }
            ) {
                repeat(5) { index ->
                    val starDelay = index * 100L
                    val starScale = remember { Animatable(0f) }

                    LaunchedEffect(starsAlpha.value) {
                        if (starsAlpha.value > 0.5f) {
                            delay(starDelay)
                            starScale.animateTo(1f, animationSpec = tween(300, easing = EaseOutBack))
                        }
                    }

                    Text(
                        text = "\u2B50",
                        fontSize = 28.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = starScale.value
                            scaleY = starScale.value
                        }
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Card with stats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(24.dp)
                    .graphicsLayer {
                        alpha = buttonAlpha.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "All ${LevelManager.levels.size} levels completed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Success
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onBackToLevelSelect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = DarkBackground
                    )
                ) {
                    Text(
                        text = "Back to Levels",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChampionConfettiEffect() {
    val particles = remember {
        List(CHAMPION_PARTICLE_COUNT) {
            val startEdge = Random.nextInt(4) // 0=top, 1=right, 2=left, 3=top-spread
            val (startX, startY, vx, vy) = when (startEdge) {
                0 -> { // Top center burst
                    listOf(
                        0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                        -0.1f,
                        (Random.nextFloat() - 0.5f) * 0.4f,
                        Random.nextFloat() * 0.3f + 0.1f
                    )
                }
                1 -> { // Top right
                    listOf(
                        0.9f + Random.nextFloat() * 0.2f,
                        -0.1f - Random.nextFloat() * 0.2f,
                        -Random.nextFloat() * 0.2f - 0.05f,
                        Random.nextFloat() * 0.3f + 0.1f
                    )
                }
                2 -> { // Top left
                    listOf(
                        -0.1f - Random.nextFloat() * 0.2f,
                        -0.1f - Random.nextFloat() * 0.2f,
                        Random.nextFloat() * 0.2f + 0.05f,
                        Random.nextFloat() * 0.3f + 0.1f
                    )
                }
                else -> { // Spread across top
                    listOf(
                        Random.nextFloat(),
                        -0.15f - Random.nextFloat() * 0.2f,
                        (Random.nextFloat() - 0.5f) * 0.15f,
                        Random.nextFloat() * 0.25f + 0.1f
                    )
                }
            }

            ChampionParticle(
                startX = startX,
                startY = startY,
                vx = vx,
                vy = vy,
                color = CHAMPION_CONFETTI_COLORS[Random.nextInt(CHAMPION_CONFETTI_COLORS.size)],
                size = Random.nextFloat() * 10f + 6f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 300f,
                startRotation = Random.nextFloat() * 360f,
                shape = ParticleShape.entries[Random.nextInt(ParticleShape.entries.size)],
                delay = Random.nextFloat() * 0.3f
            )
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 4_000, easing = LinearEasing),
        label = "champion_confetti_progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = animatedProgress
        if (t <= 0f) return@Canvas

        particles.forEach { p ->
            val particleT = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (particleT <= 0f) return@forEach

            val gravity = 0.8f
            val drag = 0.95f
            val adjustedVx = p.vx * drag.toDouble().pow((particleT * 60).toDouble()).toFloat()

            val x = p.startX + adjustedVx * particleT
            val y = p.startY + p.vy * particleT + 0.5f * gravity * particleT * particleT

            // Fade out near the end
            val alpha = when {
                particleT < 0.1f -> particleT / 0.1f
                particleT > 0.7f -> 1f - ((particleT - 0.7f) / 0.3f)
                else -> 1f
            }.coerceIn(0f, 1f)

            if (alpha > 0f && y < 1.3f && y > -0.3f && x > -0.3f && x < 1.3f) {
                val cx = x * size.width
                val cy = y * size.height
                val rotation = p.startRotation + p.rotationSpeed * particleT

                rotate(degrees = rotation, pivot = Offset(cx, cy)) {
                    when (p.shape) {
                        ParticleShape.RECTANGLE -> {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(cx - p.size, cy - p.size / 3),
                                size = Size(p.size * 2, p.size * 0.7f)
                            )
                        }
                        ParticleShape.CIRCLE -> {
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = p.size / 2,
                                center = Offset(cx, cy)
                            )
                        }
                        ParticleShape.STAR -> {
                            val path = Path()
                            val outerRadius = p.size / 2
                            val innerRadius = outerRadius * 0.4f
                            for (i in 0 until 10) {
                                val radius = if (i % 2 == 0) outerRadius else innerRadius
                                val angle = (i * 36 - 90) * PI / 180
                                val px = cx + radius * cos(angle).toFloat()
                                val py = cy + radius * sin(angle).toFloat()
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close()
                            drawPath(path, color = p.color.copy(alpha = alpha))
                        }
                        ParticleShape.RIBBON -> {
                            // Wavy ribbon effect
                            val waveOffset = sin(particleT * PI * 4).toFloat() * p.size * 0.3f
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(cx - p.size * 1.5f, cy - p.size / 4 + waveOffset),
                                size = Size(p.size * 3, p.size / 2)
                            )
                        }
                    }
                }
            }
        }
    }
}
