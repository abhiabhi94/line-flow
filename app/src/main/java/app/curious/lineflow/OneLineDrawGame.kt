package app.curious.lineflow

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.DarkSurfaceVariant
import app.curious.lineflow.ui.theme.EdgeDefault
import app.curious.lineflow.ui.theme.EdgeVisited
import app.curious.lineflow.ui.theme.HintCyan
import app.curious.lineflow.ui.theme.HintCyanSubtle
import app.curious.lineflow.ui.theme.NodeCurrent
import app.curious.lineflow.ui.theme.NodeDefault
import app.curious.lineflow.ui.theme.NodeStart
import app.curious.lineflow.ui.theme.OverlayScrim
import app.curious.lineflow.ui.theme.Success
import app.curious.lineflow.ui.theme.TextPrimary
import app.curious.lineflow.ui.theme.TextSecondary
import app.curious.lineflow.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

enum class GameOverReason {
    LIFTED_FINGER,
    RETRACED_EDGE,
    INCOMPLETE_PATH
}

data class GameState(
    val level: Level,
    val currentLevelId: Int = level.id,
    val currentStartNodeId: Int? = null,
    val currentNodeId: Int? = null,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val gameOverReason: GameOverReason? = null,
    val failedEdge: Edge? = null
) {
    fun reset(): GameState {
        val resetEdges = level.edges.map { it.copy(isVisited = false) }
        val resetLevel = level.copy(edges = resetEdges)
        return GameState(
            level = resetLevel,
            currentLevelId = resetLevel.id,
            gameOverReason = null,
            failedEdge = null
        )
    }

    fun updateEdgeVisited(edgeToUpdate: Edge, isVisited: Boolean): GameState {
        val newEdges = level.edges.map { edge ->
            val isSameEdge = (edge.node1Id == edgeToUpdate.node1Id && edge.node2Id == edgeToUpdate.node2Id) ||
                             (edge.node1Id == edgeToUpdate.node2Id && edge.node2Id == edgeToUpdate.node1Id)
            Edge(edge.node1Id, edge.node2Id, if (isSameEdge) isVisited else edge.isVisited)
        }
        val newLevel = level.copy(edges = newEdges)
        val isComplete = newLevel.edges.all { it.isVisited }
        return this.copy(level = newLevel, isLevelComplete = isComplete)
    }
}

@Composable
fun OneLineDrawGame(
    modifier: Modifier = Modifier,
    levelId: Int,
    progressRepository: GameProgressRepository,
    onBackToLevelSelect: () -> Unit,
    onNextLevel: (Int) -> Unit
) {
    BackHandler { onBackToLevelSelect() }

    val level = remember(levelId) { LevelManager.getLevel(levelId) } ?: return

    var gameState by remember(levelId) {
        mutableStateOf(GameState(level = level, currentLevelId = level.id))
    }

    var hintRevealIndex by remember(levelId) { mutableIntStateOf(-1) }
    val currentHintStep = level.hints.steps.getOrNull(hintRevealIndex)

    var showOverlay by remember(levelId) { mutableStateOf(false) }

    // Track if hints were used this attempt (for star rating)
    var hintsUsedThisAttempt by remember(levelId) { mutableStateOf(false) }

    // Vibration setting
    val vibrationEnabled = remember { progressRepository.isVibrationEnabled() }

    // Only show overlay for win - loss shows inline retry button
    LaunchedEffect(gameState.isLevelComplete) {
        if (gameState.isLevelComplete) {
            delay(600)
            showOverlay = true
        } else {
            showOverlay = false
        }
    }

    val view = LocalView.current
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrateTick() {
        if (!vibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10L)
        }
    }

    fun vibrateError() {
        if (!vibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    // Hint pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    // Current node glow pulse
    val currentNodeGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "current_node_glow"
    )

    // Node touch bounce
    val nodeScaleAnimatable = remember { Animatable(1f) }
    LaunchedEffect(gameState.currentNodeId) {
        if (gameState.currentNodeId != null) {
            nodeScaleAnimatable.snapTo(1.3f)
            nodeScaleAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    // Snap ring effect on new edge visited
    val visitedEdgeCount = gameState.level.edges.count { it.isVisited }
    val snapRingRadius = remember { Animatable(0f) }
    val snapRingAlpha = remember { Animatable(0f) }
    LaunchedEffect(visitedEdgeCount) {
        if (visitedEdgeCount > 0 && gameState.currentNodeId != null) {
            launch {
                snapRingRadius.snapTo(14f)
                snapRingRadius.animateTo(35f, tween(300))
            }
            launch {
                snapRingAlpha.snapTo(0.8f)
                snapRingAlpha.animateTo(0f, tween(300))
            }
        }
    }

    // Screen shake on loss
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(gameState.isGameOver, gameState.isLevelComplete) {
        if (gameState.isGameOver && !gameState.isLevelComplete) {
            vibrateError()
            repeat(3) {
                shakeOffset.animateTo(12f, tween(40))
                shakeOffset.animateTo(-12f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    // Red flash on loss
    val failFlashAlpha = remember { Animatable(0f) }
    LaunchedEffect(gameState.isGameOver, gameState.isLevelComplete) {
        if (gameState.isGameOver && !gameState.isLevelComplete) {
            failFlashAlpha.snapTo(0.25f)
            failFlashAlpha.animateTo(0f, tween(400))
        }
    }

    // Hint button bounce
    val hintBounce = remember { Animatable(1f) }
    LaunchedEffect(hintRevealIndex) {
        if (hintRevealIndex >= 0) {
            hintBounce.snapTo(1.2f)
            hintBounce.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeOffset.value }
            .background(DarkBackground)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val nodeRadius = with(LocalDensity.current) { 14.dp.toPx() }
            val nodeHitRadius = nodeRadius * 3f

            val side = min(width, height)
            val xOffset = (width - side) / 2f
            val yOffset = (height - side) / 2f

            fun toPixelOffset(normalized: Offset): Offset {
                return Offset(
                    xOffset + normalized.x * side,
                    yOffset + normalized.y * side
                )
            }

            val pixelNodes = remember(gameState.level.nodes, width, height) {
                gameState.level.nodes.associateWith { node ->
                    toPixelOffset(node.position)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(gameState.isLevelComplete, gameState.isGameOver) {
                        if (gameState.isLevelComplete || gameState.isGameOver) return@pointerInput

                        awaitPointerEventScope {
                            while (true) {
                                val downEvent = awaitPointerEvent()
                                if (downEvent.type != PointerEventType.Press) continue
                                val down = downEvent.changes.firstOrNull() ?: continue

                                hintRevealIndex = -1

                                val closestNode = pixelNodes.minByOrNull { (_, pixelOffset) ->
                                    (pixelOffset - down.position).getDistance()
                                }

                                val startNodeId = closestNode?.key?.id
                                if (startNodeId != null && (closestNode.value - down.position).getDistance() < nodeHitRadius) {
                                    gameState = gameState.reset().copy(
                                        currentStartNodeId = startNodeId,
                                        currentNodeId = startNodeId
                                    )
                                    vibrateTick()
                                    down.consume()
                                }

                                var tracking = gameState.currentNodeId != null
                                while (tracking) {
                                    val moveEvent = awaitPointerEvent()
                                    val change = moveEvent.changes.firstOrNull() ?: break

                                    if (!change.pressed) {
                                        if (gameState.currentNodeId != null && !gameState.isLevelComplete) {
                                            val reason = if (gameState.level.edges.all { it.isVisited }) {
                                                GameOverReason.INCOMPLETE_PATH
                                            } else {
                                                GameOverReason.LIFTED_FINGER
                                            }
                                            gameState = gameState.copy(
                                                isGameOver = true,
                                                gameOverReason = reason
                                            )
                                        }
                                        change.consume()
                                        break
                                    }

                                    if (gameState.currentNodeId == null || gameState.isGameOver) {
                                        change.consume()
                                        break
                                    }

                                    val currentPos = change.position

                                    val movedToNewNode = gameState.level.nodes.firstOrNull { node ->
                                        val pixelOffset = pixelNodes.getValue(node)
                                        val distance = (pixelOffset - currentPos).getDistance()
                                        distance < nodeHitRadius
                                    }

                                    if (movedToNewNode != null && movedToNewNode.id != gameState.currentNodeId) {
                                        val unvisitedEdges = gameState.level.edges.filter { !it.isVisited }

                                        val connectingEdge = unvisitedEdges.firstOrNull { edge ->
                                            edge.containsNode(gameState.currentNodeId!!) && edge.containsNode(movedToNewNode.id)
                                        }

                                        if (connectingEdge != null) {
                                            gameState = gameState
                                                .updateEdgeVisited(connectingEdge, true)
                                                .copy(currentNodeId = movedToNewNode.id)
                                            vibrateTick()
                                        } else {
                                            // Find the already-visited edge that was retraced
                                            val retracedEdge = gameState.level.edges.firstOrNull { edge ->
                                                edge.isVisited &&
                                                edge.containsNode(gameState.currentNodeId!!) &&
                                                edge.containsNode(movedToNewNode.id)
                                            }
                                            gameState = gameState.copy(
                                                isGameOver = true,
                                                gameOverReason = GameOverReason.RETRACED_EDGE,
                                                failedEdge = retracedEdge
                                            )
                                        }
                                    }

                                    change.consume()
                                }
                            }
                        }
                    }
            ) {
                val defaultStrokeWidth = 6f
                val visitedStrokeWidth = 8f

                // Glow pass for visited edges (drawn first, behind everything)
                gameState.level.edges.forEach { edge ->
                    if (!edge.isVisited) return@forEach
                    val startNode = gameState.level.nodes.first { it.id == edge.node1Id }
                    val endNode = gameState.level.nodes.first { it.id == edge.node2Id }
                    val startOff = pixelNodes.getValue(startNode)
                    val endOff = pixelNodes.getValue(endNode)

                    // Outer glow
                    drawLine(
                        color = EdgeVisited.copy(alpha = 0.15f),
                        start = startOff,
                        end = endOff,
                        strokeWidth = 24f,
                        cap = StrokeCap.Round
                    )
                    // Inner glow
                    drawLine(
                        color = EdgeVisited.copy(alpha = 0.3f),
                        start = startOff,
                        end = endOff,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw edges
                gameState.level.edges.forEach { edge ->
                    val startNode = gameState.level.nodes.first { it.id == edge.node1Id }
                    val endNode = gameState.level.nodes.first { it.id == edge.node2Id }
                    val startOff = pixelNodes.getValue(startNode)
                    val endOff = pixelNodes.getValue(endNode)

                    val isFailedEdge = gameState.failedEdge?.let { failed ->
                        (edge.node1Id == failed.node1Id && edge.node2Id == failed.node2Id) ||
                        (edge.node1Id == failed.node2Id && edge.node2Id == failed.node1Id)
                    } ?: false

                    val color = when {
                        isFailedEdge -> app.curious.lineflow.ui.theme.Error
                        edge.isVisited -> EdgeVisited
                        else -> EdgeDefault
                    }
                    val strokeWidth = if (edge.isVisited || isFailedEdge) visitedStrokeWidth else defaultStrokeWidth

                    drawLine(
                        color = color,
                        start = startOff,
                        end = endOff,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Hint first edge highlight
                if (currentHintStep?.showFirstEdge == true) {
                    val firstEdge = level.hints.firstEdge
                    if (firstEdge != null) {
                        val fromNode = gameState.level.nodes.firstOrNull { it.id == firstEdge.first }
                        val toNode = gameState.level.nodes.firstOrNull { it.id == firstEdge.second }
                        if (fromNode != null && toNode != null) {
                            drawLine(
                                color = HintCyan.copy(alpha = hintAlpha),
                                start = pixelNodes.getValue(fromNode),
                                end = pixelNodes.getValue(toNode),
                                strokeWidth = visitedStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Draw nodes
                pixelNodes.forEach { (node, pixelOffset) ->
                    val color = when (node.id) {
                        gameState.currentStartNodeId -> NodeStart
                        gameState.currentNodeId -> NodeCurrent
                        else -> NodeDefault
                    }

                    val isCurrentNode = node.id == gameState.currentNodeId

                    // Current node ambient glow
                    if (isCurrentNode && gameState.currentNodeId != null) {
                        drawCircle(
                            color = NodeCurrent.copy(alpha = 0.2f + currentNodeGlow * 0.3f),
                            radius = nodeRadius + 8f + currentNodeGlow * 6f,
                            center = pixelOffset
                        )
                    }

                    // Hint glow on valid start nodes
                    if (currentHintStep?.showValidStarts == true &&
                        level.hints.validStartNodeIds.contains(node.id) &&
                        gameState.currentNodeId == null
                    ) {
                        drawCircle(
                            color = HintCyan.copy(alpha = hintAlpha * 0.5f),
                            radius = nodeRadius + 10f,
                            center = pixelOffset,
                            style = Stroke(width = 3f)
                        )
                    }

                    // Outer ring
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = nodeRadius + 3f,
                        center = pixelOffset,
                        style = Stroke(width = 2f)
                    )

                    // Inner fill with bounce scale for current node
                    val radius = if (isCurrentNode) {
                        nodeRadius * nodeScaleAnimatable.value
                    } else {
                        nodeRadius
                    }
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = pixelOffset
                    )
                }

                // Snap ring effect at current node
                if (snapRingAlpha.value > 0f && gameState.currentNodeId != null) {
                    val currentNode = gameState.level.nodes.firstOrNull { it.id == gameState.currentNodeId }
                    if (currentNode != null) {
                        val currentPixel = pixelNodes.getValue(currentNode)
                        drawCircle(
                            color = NodeCurrent.copy(alpha = snapRingAlpha.value),
                            radius = snapRingRadius.value,
                            center = currentPixel,
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // Inline loss UI - positioned just above the topmost node
            if (gameState.isGameOver && !gameState.isLevelComplete) {
                val topMostNodeY = pixelNodes.values.minOf { it.y }
                val topMostNodeYDp = with(LocalDensity.current) { topMostNodeY.toDp() }

                val errorMessage = when (gameState.gameOverReason) {
                    GameOverReason.LIFTED_FINGER -> "Lifted finger too early"
                    GameOverReason.RETRACED_EDGE -> "Retraced a line"
                    GameOverReason.INCOMPLETE_PATH -> "Not all edges covered"
                    null -> "Try again"
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = (topMostNodeYDp - 140.dp).coerceAtLeast(16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage,
                        color = app.curious.lineflow.ui.theme.Error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            hintsUsedThisAttempt = false
                            gameState = gameState.reset()
                            hintRevealIndex = -1
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = DarkBackground
                        )
                    ) {
                        Text(
                            text = "Retry",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Red flash overlay on loss
        if (failFlashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(app.curious.lineflow.ui.theme.Error.copy(alpha = failFlashAlpha.value))
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button with press scale
            val backInteraction = remember { MutableInteractionSource() }
            val backPressed by backInteraction.collectIsPressedAsState()
            val backScale by animateFloatAsState(
                targetValue = if (backPressed) 0.9f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "back_btn_scale"
            )

            TextButton(
                onClick = onBackToLevelSelect,
                interactionSource = backInteraction,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = backScale
                        scaleY = backScale
                    }
                    .background(DarkSurfaceVariant, CircleShape),
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
                Text(
                    text = "\u2190",
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Level info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${level.id}",
                    color = Accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = level.name,
                    color = TextTertiary,
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp
                )
            }

            // Hint button with press scale and bounce
            val hintInteraction = remember { MutableInteractionSource() }
            val hintPressed by hintInteraction.collectIsPressedAsState()
            val hintScale by animateFloatAsState(
                targetValue = if (hintPressed) 0.9f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "hint_btn_scale"
            )

            val totalHintSteps = level.hints.steps.size
            val hintRemaining = when {
                hintRevealIndex < 0 -> totalHintSteps
                else -> totalHintSteps - (hintRevealIndex + 1)
            }

            Box {
                TextButton(
                    onClick = {
                        val maxIndex = level.hints.steps.lastIndex
                        if (hintRevealIndex < maxIndex) {
                            hintRevealIndex += 1
                            if (hintRevealIndex == 0) {
                                hintsUsedThisAttempt = true
                            }
                            if (hintRevealIndex == maxIndex) {
                                progressRepository.markHintUsed(level.id, hintRevealIndex + 1)
                            }
                        } else {
                            hintRevealIndex = -1
                        }
                    },
                    interactionSource = hintInteraction,
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            scaleX = hintScale
                            scaleY = hintScale
                        }
                        .background(
                            color = if (hintRevealIndex < 0) DarkSurfaceVariant else HintCyanSubtle,
                            shape = CircleShape
                        ),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text(
                        text = "\uD83D\uDCA1",
                        color = if (hintRevealIndex < 0) Accent else HintCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer {
                            scaleX = hintBounce.value
                            scaleY = hintBounce.value
                        }
                    )
                }

                // Badge showing remaining hints
                if (hintRemaining > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .background(Accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$hintRemaining",
                            color = DarkBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Hint text overlay
        if (currentHintStep != null && gameState.currentNodeId == null) {
            Text(
                text = currentHintStep.text,
                color = HintCyan,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Progress bar - shown during active tracing
        if (gameState.currentNodeId != null && !gameState.isGameOver && !gameState.isLevelComplete) {
            val totalEdges = gameState.level.edges.size
            val progress = visitedEdgeCount.toFloat() / totalEdges

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .fillMaxWidth(0.5f)
                    .height(4.dp)
                    .background(DarkSurfaceVariant, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(EdgeVisited, RoundedCornerShape(2.dp))
                )
            }
        }

        // Win overlay
        if (showOverlay && gameState.isLevelComplete) {
            remember(gameState.currentLevelId) {
                progressRepository.markLevelCompleted(gameState.currentLevelId)
                true
            }

            val isLastLevel = LevelManager.getNextLevel(gameState.currentLevelId) == null

            if (isLastLevel) {
                // Show special champion screen for completing the final level
                ChampionScreen(
                    onBackToLevelSelect = {
                        showOverlay = false
                        onBackToLevelSelect()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OverlayScrim),
                    contentAlignment = Alignment.Center
                ) {
                    // Confetti inside the overlay
                    ConfettiOverlay()

                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(
                            initialScale = 0.7f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(animationSpec = tween(200))
                    ) {
                        WinOverlayContent(
                            gameState = gameState,
                            hintsUsedThisAttempt = hintsUsedThisAttempt,
                            onNextLevel = {
                                showOverlay = false
                                hintsUsedThisAttempt = false
                                val nextLevel = LevelManager.getNextLevel(gameState.currentLevelId)
                                if (nextLevel != null) {
                                    onNextLevel(nextLevel.id)
                                } else {
                                    onBackToLevelSelect()
                                }
                            },
                            onBackToLevelSelect = {
                                showOverlay = false
                                onBackToLevelSelect()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WinOverlayContent(
    gameState: GameState,
    hintsUsedThisAttempt: Boolean,
    onNextLevel: () -> Unit,
    onBackToLevelSelect: () -> Unit
) {
    val starsEarned = if (hintsUsedThisAttempt) 1 else 3

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(DarkSurface, RoundedCornerShape(24.dp))
            .padding(32.dp)
    ) {
        Text(
            text = "Level ${gameState.currentLevelId} Complete",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Success,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(12.dp))

        // Star rating
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                Text(
                    text = "\u2B50",
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (index < starsEarned) 1f else 0.2f
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onNextLevel,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = DarkBackground
            )
        ) {
            Text(
                text = "Next Level",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onBackToLevelSelect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Back to Levels",
                color = TextTertiary,
                fontSize = 14.sp
            )
        }
    }
}

