package app.curious.lineflow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.EdgeDefault
import app.curious.lineflow.ui.theme.EdgeVisited
import app.curious.lineflow.ui.theme.Error
import app.curious.lineflow.ui.theme.NodeCurrent
import app.curious.lineflow.ui.theme.NodeDefault
import app.curious.lineflow.ui.theme.OverlayScrim
import app.curious.lineflow.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.min

// Triangle demo node positions (normalized 0..1)
private val DEMO_NODES = listOf(
    Offset(0.5f, 0.25f),   // top
    Offset(0.2f, 0.75f),   // bottom-left
    Offset(0.8f, 0.75f)    // bottom-right
)

// Edge path order: top -> bottom-left -> bottom-right -> top
private val DEMO_EDGE_PATH = listOf(0, 1, 2, 0)

// Animation timing constants
private const val LIFT_TRACE_DURATION_MS = 1_200
private const val RETRACE_SETUP_DURATION_MS = 1_000
private const val RETRACE_FAIL_DURATION_MS = 400
private const val FAIL_DISPLAY_DURATION_MS = 800
private const val RESET_DURATION_MS = 300
private const val SUCCESS_TRACE_DURATION_MS = 1_800
private const val FADE_OUT_DURATION_MS = 500

private enum class TutorialPhase {
    LIFT_FAIL_TRACE,
    LIFT_FAIL_DISPLAY,
    RESET_1,
    RETRACE_SETUP,
    RETRACE_FAIL,
    RETRACE_DISPLAY,
    RESET_2,
    SUCCESS_TRACE,
    FADE_OUT
}

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit
) {
    val edgeCount = DEMO_EDGE_PATH.size - 1 // 3 edges

    var phase by remember { mutableStateOf(TutorialPhase.LIFT_FAIL_TRACE) }
    val traceProgress = remember { Animatable(0f) }
    val fingerAlpha = remember { Animatable(1f) }
    val fingerOffsetY = remember { Animatable(0f) }
    val errorFlashAlpha = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    var isRetracing by remember { mutableStateOf(false) }

    // Track visited edges for proper rendering during retrace demo
    var visitedEdgeCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Phase 1: Lift fail - trace 1.5 edges then lift
        phase = TutorialPhase.LIFT_FAIL_TRACE
        visitedEdgeCount = 0
        traceProgress.animateTo(0.5f, tween(LIFT_TRACE_DURATION_MS, easing = LinearEasing))

        // Finger lifts
        fingerAlpha.animateTo(0.3f, tween(150))
        fingerOffsetY.animateTo(-30f, tween(150))
        errorFlashAlpha.animateTo(0.4f, tween(100))
        phase = TutorialPhase.LIFT_FAIL_DISPLAY
        delay(FAIL_DISPLAY_DURATION_MS.toLong())

        // Reset 1
        phase = TutorialPhase.RESET_1
        errorFlashAlpha.animateTo(0f, tween(RESET_DURATION_MS))
        traceProgress.snapTo(0f)
        fingerAlpha.snapTo(1f)
        fingerOffsetY.snapTo(0f)
        delay(RESET_DURATION_MS.toLong())

        // Phase 2: Retrace fail - trace 2 edges, then go back
        phase = TutorialPhase.RETRACE_SETUP
        visitedEdgeCount = 0
        traceProgress.animateTo(0.67f, tween(RETRACE_SETUP_DURATION_MS, easing = LinearEasing))
        visitedEdgeCount = 2

        // Now retrace (finger goes backward)
        phase = TutorialPhase.RETRACE_FAIL
        isRetracing = true
        traceProgress.animateTo(0.5f, tween(RETRACE_FAIL_DURATION_MS, easing = LinearEasing))
        errorFlashAlpha.animateTo(0.4f, tween(100))
        phase = TutorialPhase.RETRACE_DISPLAY
        delay(FAIL_DISPLAY_DURATION_MS.toLong())

        // Reset 2
        phase = TutorialPhase.RESET_2
        isRetracing = false
        errorFlashAlpha.animateTo(0f, tween(RESET_DURATION_MS))
        traceProgress.snapTo(0f)
        visitedEdgeCount = 0
        delay(RESET_DURATION_MS.toLong())

        // Phase 3: Success trace
        phase = TutorialPhase.SUCCESS_TRACE
        traceProgress.animateTo(1f, tween(SUCCESS_TRACE_DURATION_MS, easing = LinearEasing))
        delay(400)

        // Fade out
        phase = TutorialPhase.FADE_OUT
        overlayAlpha.animateTo(0f, tween(FADE_OUT_DURATION_MS))
        onDismiss()
    }

    val instructionText = when (phase) {
        TutorialPhase.LIFT_FAIL_TRACE -> "Watch closely..."
        TutorialPhase.LIFT_FAIL_DISPLAY -> "Don't lift your finger!"
        TutorialPhase.RESET_1 -> ""
        TutorialPhase.RETRACE_SETUP -> "Watch closely..."
        TutorialPhase.RETRACE_FAIL -> ""
        TutorialPhase.RETRACE_DISPLAY -> "Don't retrace a line!"
        TutorialPhase.RESET_2 -> ""
        TutorialPhase.SUCCESS_TRACE -> "Trace every line\nin one stroke"
        TutorialPhase.FADE_OUT -> ""
    }

    val isErrorPhase = phase == TutorialPhase.LIFT_FAIL_DISPLAY || phase == TutorialPhase.RETRACE_DISPLAY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(OverlayScrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 60.dp, vertical = 120.dp)
        ) {
            val side = min(size.width, size.height)
            val xOffset = (size.width - side) / 2f
            val yOffset = (size.height - side) / 2f

            fun toPixel(normalized: Offset): Offset {
                return Offset(
                    xOffset + normalized.x * side,
                    yOffset + normalized.y * side
                )
            }

            val pixelNodes = DEMO_NODES.map { toPixel(it) }
            val nodeRadius = 14f
            val progress = traceProgress.value
            val currentEdgeFloat = progress * edgeCount

            // Draw error flash overlay
            if (errorFlashAlpha.value > 0f) {
                drawRect(
                    color = Error.copy(alpha = errorFlashAlpha.value * 0.3f),
                    size = size
                )
            }

            // Draw edges
            for (i in 0 until edgeCount) {
                val fromIdx = DEMO_EDGE_PATH[i]
                val toIdx = DEMO_EDGE_PATH[i + 1]
                val from = pixelNodes[fromIdx]
                val to = pixelNodes[toIdx]

                val isFullyVisited = currentEdgeFloat > (i + 1).toFloat()
                val isBeingTraced = currentEdgeFloat > i.toFloat() && currentEdgeFloat <= (i + 1).toFloat()

                // During retrace, check if this edge is being retraced
                val isRetracedEdge = isRetracing && i == 1 // Edge from node 1 to node 2

                when {
                    isRetracedEdge && (phase == TutorialPhase.RETRACE_FAIL || phase == TutorialPhase.RETRACE_DISPLAY) -> {
                        // Draw retraced edge in error color
                        drawLine(
                            color = Error.copy(alpha = 0.15f),
                            start = from,
                            end = to,
                            strokeWidth = 24f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Error.copy(alpha = 0.3f),
                            start = from,
                            end = to,
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Error,
                            start = from,
                            end = to,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                    isFullyVisited || (isRetracing && i < visitedEdgeCount) -> {
                        // Glow pass for visited edges
                        drawLine(
                            color = EdgeVisited.copy(alpha = 0.15f),
                            start = from,
                            end = to,
                            strokeWidth = 24f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = EdgeVisited.copy(alpha = 0.3f),
                            start = from,
                            end = to,
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = EdgeVisited,
                            start = from,
                            end = to,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                    isBeingTraced && !isRetracing -> {
                        val edgeProgress = currentEdgeFloat - i.toFloat()
                        val partialEnd = Offset(
                            from.x + (to.x - from.x) * edgeProgress,
                            from.y + (to.y - from.y) * edgeProgress
                        )
                        // Unvisited portion
                        drawLine(
                            color = EdgeDefault,
                            start = from,
                            end = to,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                        // Visited portion glow
                        drawLine(
                            color = EdgeVisited.copy(alpha = 0.15f),
                            start = from,
                            end = partialEnd,
                            strokeWidth = 24f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = EdgeVisited.copy(alpha = 0.3f),
                            start = from,
                            end = partialEnd,
                            strokeWidth = 14f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = EdgeVisited,
                            start = from,
                            end = partialEnd,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                    else -> {
                        // Not yet reached
                        drawLine(
                            color = EdgeDefault,
                            start = from,
                            end = to,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw nodes
            pixelNodes.forEachIndexed { index, pixelPos ->
                val nodeVisitEdge = DEMO_EDGE_PATH.indexOf(index).toFloat()
                val isVisited = currentEdgeFloat >= nodeVisitEdge
                val isCurrent = run {
                    val currentEdgeIdx = currentEdgeFloat.toInt().coerceAtMost(edgeCount - 1)
                    val edgeProgress = currentEdgeFloat - currentEdgeIdx.toFloat()
                    if (edgeProgress < 0.15f) {
                        DEMO_EDGE_PATH[currentEdgeIdx] == index
                    } else if (edgeProgress > 0.85f) {
                        DEMO_EDGE_PATH[currentEdgeIdx + 1] == index
                    } else {
                        false
                    }
                }

                val color = when {
                    isCurrent -> NodeCurrent
                    isVisited -> NodeCurrent.copy(alpha = 0.6f)
                    else -> NodeDefault
                }

                drawCircle(
                    color = color,
                    radius = nodeRadius,
                    center = pixelPos
                )
            }

            // Draw finger icon (circle + tail) following the cursor
            val showFinger = phase != TutorialPhase.RESET_1 &&
                             phase != TutorialPhase.RESET_2 &&
                             phase != TutorialPhase.FADE_OUT &&
                             progress < 1f

            if (showFinger) {
                val currentEdgeIdx = currentEdgeFloat.toInt().coerceAtMost(edgeCount - 1)
                val edgeProgress = currentEdgeFloat - currentEdgeIdx.toFloat()
                val fromIdx = DEMO_EDGE_PATH[currentEdgeIdx]
                val toIdx = DEMO_EDGE_PATH[currentEdgeIdx + 1]
                val from = pixelNodes[fromIdx]
                val to = pixelNodes[toIdx]

                val cursorPos = Offset(
                    from.x + (to.x - from.x) * edgeProgress,
                    from.y + (to.y - from.y) * edgeProgress + fingerOffsetY.value
                )

                // Calculate angle for finger orientation (pointing in direction of movement)
                val angle = atan2(to.y - from.y, to.x - from.x) * (180f / Math.PI.toFloat()) + 90f

                drawFingerIcon(
                    center = cursorPos,
                    angle = angle,
                    alpha = fingerAlpha.value
                )
            }
        }

        // Instruction text
        if (instructionText.isNotEmpty()) {
            val textColor = if (isErrorPhase) Error else TextSecondary
            Text(
                text = instructionText,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

/**
 * Draws a finger icon (circle tip + tapered oval body) at the specified position.
 */
private fun DrawScope.drawFingerIcon(
    center: Offset,
    angle: Float,
    alpha: Float
) {
    val fingerColor = NodeCurrent.copy(alpha = alpha)
    val glowColor = NodeCurrent.copy(alpha = alpha * 0.3f)

    rotate(degrees = angle, pivot = center) {
        // Outer glow
        drawOval(
            color = glowColor,
            topLeft = Offset(center.x - 18f, center.y - 25f),
            size = Size(36f, 70f)
        )

        // Finger body (tapered oval)
        val fingerPath = Path().apply {
            // Start at bottom center
            moveTo(center.x, center.y + 35f)
            // Left side curve
            cubicTo(
                center.x - 14f, center.y + 30f,
                center.x - 12f, center.y - 5f,
                center.x - 10f, center.y - 15f
            )
            // Top curve (fingertip)
            cubicTo(
                center.x - 8f, center.y - 22f,
                center.x + 8f, center.y - 22f,
                center.x + 10f, center.y - 15f
            )
            // Right side curve
            cubicTo(
                center.x + 12f, center.y - 5f,
                center.x + 14f, center.y + 30f,
                center.x, center.y + 35f
            )
            close()
        }

        drawPath(
            path = fingerPath,
            color = fingerColor
        )

        // Fingertip highlight
        drawCircle(
            color = fingerColor.copy(alpha = alpha * 0.8f),
            radius = 8f,
            center = Offset(center.x, center.y - 12f)
        )
    }
}
