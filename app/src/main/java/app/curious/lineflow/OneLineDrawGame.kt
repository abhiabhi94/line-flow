package app.curious.lineflow

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.DarkSurfaceVariant
import app.curious.lineflow.ui.theme.EdgeDefault
import app.curious.lineflow.ui.theme.EdgeMissing
import app.curious.lineflow.ui.theme.EdgeVisited
import app.curious.lineflow.ui.theme.Error
import app.curious.lineflow.ui.theme.HintCyan
import app.curious.lineflow.ui.theme.HintCyanSubtle
import app.curious.lineflow.ui.theme.NodeCurrent
import app.curious.lineflow.ui.theme.NodeDefault
import app.curious.lineflow.ui.theme.NodeStart
import app.curious.lineflow.ui.theme.OverlayScrim
import app.curious.lineflow.ui.theme.Success
import app.curious.lineflow.ui.theme.TextPrimary
import app.curious.lineflow.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Touch geometry, in dp. The level layouts in Graph.kt are generated against
 * these numbers (see `.scripts/leveldesign/geometry.py`): dots are always at
 * least two hit radii apart and no dot sits within a hit radius of a line it
 * is not part of, so a finger following a line can only ever reach the dot
 * at its end.
 */
object PlayfieldSpec {
    val nodeRadius: Dp = 12.dp
    val hitRadius: Dp = 32.dp
    val horizontalMargin: Dp = 24.dp
    val bottomMargin: Dp = 24.dp
    val topBarHeight: Dp = 60.dp
    val statusStripHeight: Dp = 84.dp

    /** The play area is never squeezed below this height-to-width ratio. */
    const val MIN_PLAY_ASPECT = 1.32f

    /**
     * Space to leave under the drawing so it sits at the optical centre of the
     * screen (mirroring the top bar and status strip above it), unless that
     * would squeeze the play area below [MIN_PLAY_ASPECT].
     */
    fun balancingBottomSpace(screenWidth: Dp, screenHeight: Dp): Dp {
        val chrome = topBarHeight + statusStripHeight
        val minPlayHeight = (screenWidth - horizontalMargin * 2) * MIN_PLAY_ASPECT
        val slack = screenHeight - chrome - bottomMargin - minPlayHeight
        return (chrome - bottomMargin).coerceIn(0.dp, slack.coerceAtLeast(0.dp))
    }
}

enum class GameOverReason {
    LIFTED_FINGER,
    RETRACED_EDGE,
    NO_LINE,
}

data class GameState(
    val level: Level,
    val currentLevelId: Int = level.id,
    val currentStartNodeId: Int? = null,
    val currentNodeId: Int? = null,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val gameOverReason: GameOverReason? = null,
    val failedEdge: Edge? = null,
) {
    fun reset(): GameState {
        val resetEdges = level.edges.map { it.copy(isVisited = false) }
        val resetLevel = level.copy(edges = resetEdges)
        return GameState(
            level = resetLevel,
            currentLevelId = resetLevel.id,
            gameOverReason = null,
            failedEdge = null,
        )
    }

    fun edgeBetween(a: Int, b: Int): Edge? = level.edges.firstOrNull { it.containsNode(a) && it.containsNode(b) }

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

    /**
     * The result of dragging from the current dot onto [nodeId]. Pure so it can
     * be unit tested without a pointer.
     */
    fun moveTo(nodeId: Int): GameState {
        val from = currentNodeId ?: return this
        if (nodeId == from || isGameOver || isLevelComplete) return this
        val edge = edgeBetween(from, nodeId)
        return when {
            edge == null -> copy(isGameOver = true, gameOverReason = GameOverReason.NO_LINE)
            edge.isVisited -> copy(isGameOver = true, gameOverReason = GameOverReason.RETRACED_EDGE, failedEdge = edge)
            else -> updateEdgeVisited(edge, true).copy(currentNodeId = nodeId)
        }
    }

    fun liftFinger(): GameState =
        if (currentNodeId != null && !isLevelComplete && !isGameOver) {
            copy(isGameOver = true, gameOverReason = GameOverReason.LIFTED_FINGER)
        } else {
            this
        }
}

/**
 * Maps a level's normalized node positions into a play area of [size] pixels,
 * centred, with [margin] pixels kept free on every side so the outermost dots
 * are fully touchable. The drawing is scaled uniformly to fit; when the play
 * area is taller than the drawing (a tall phone showing a squat level) the
 * drawing is additionally stretched vertically, up to [maxStretch], so the
 * spare height spreads the dots out instead of staying empty. Stretching only
 * ever increases distances between dots and lines, so the touch guarantees
 * from the level checker still hold.
 */
fun layoutNodes(nodes: List<Node>, size: Size, margin: Float, maxStretch: Float = MAX_VERTICAL_STRETCH): Map<Int, Offset> {
    if (nodes.isEmpty()) return emptyMap()
    val minX = nodes.minOf { it.position.x }
    val maxX = nodes.maxOf { it.position.x }
    val minY = nodes.minOf { it.position.y }
    val maxY = nodes.maxOf { it.position.y }
    val contentWidth = (maxX - minX).coerceAtLeast(1e-4f)
    val contentHeight = (maxY - minY).coerceAtLeast(1e-4f)
    val availableWidth = (size.width - 2 * margin).coerceAtLeast(1f)
    val availableHeight = (size.height - 2 * margin).coerceAtLeast(1f)
    val scaleX = min(availableWidth / contentWidth, availableHeight / contentHeight)
    val scaleY = min(availableHeight / contentHeight, scaleX * maxStretch)
    val offsetX = (size.width - contentWidth * scaleX) / 2f
    val offsetY = (size.height - contentHeight * scaleY) / 2f
    return nodes.associate { node ->
        node.id to Offset(
            offsetX + (node.position.x - minX) * scaleX,
            offsetY + (node.position.y - minY) * scaleY,
        )
    }
}

/** How far a squat level may be stretched vertically to use a tall screen. */
const val MAX_VERTICAL_STRETCH = 1.3f

/** The dot under [position], if any is within [hitRadius]. */
fun nodeAt(pixelNodes: Map<Int, Offset>, position: Offset, hitRadius: Float): Int? =
    pixelNodes.entries
        .minByOrNull { (_, center) -> (center - position).getDistance() }
        ?.takeIf { (_, center) -> (center - position).getDistance() <= hitRadius }
        ?.key

/**
 * The part of a line the finger has traced so far: from the current dot to
 * the finger's projection onto the best-fitting unvisited line leaving that
 * dot. Null when the finger is not following any line (more than [tolerance]
 * pixels away from all of them, or behind the dot). Purely visual: a line
 * only counts once the finger reaches the dot at its end.
 */
fun partialLine(
    pixelNodes: Map<Int, Offset>,
    edges: List<Edge>,
    currentNodeId: Int,
    finger: Offset,
    tolerance: Float,
): Pair<Offset, Offset>? {
    val from = pixelNodes[currentNodeId] ?: return null
    var best: Pair<Offset, Offset>? = null
    var bestDistance = tolerance
    edges.forEach { edge ->
        if (edge.isVisited || !edge.containsNode(currentNodeId)) return@forEach
        val otherId = if (edge.node1Id == currentNodeId) edge.node2Id else edge.node1Id
        val to = pixelNodes[otherId] ?: return@forEach
        val dir = to - from
        val lengthSquared = dir.x * dir.x + dir.y * dir.y
        if (lengthSquared == 0f) return@forEach
        val t = ((finger.x - from.x) * dir.x + (finger.y - from.y) * dir.y) / lengthSquared
        if (t <= 0f) return@forEach
        val clamped = min(t, 1f)
        val projection = Offset(from.x + dir.x * clamped, from.y + dir.y * clamped)
        val distance = (finger - projection).getDistance()
        if (distance < bestDistance) {
            bestDistance = distance
            best = from to projection
        }
    }
    return best
}

@Composable
fun OneLineDrawGame(
    modifier: Modifier = Modifier,
    levelId: Int,
    progressRepository: GameProgressRepository,
    onBackToLevelSelect: () -> Unit,
    onNextLevel: (Int) -> Unit,
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
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hint_alpha",
    )

    // Current node glow pulse
    val currentNodeGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "current_node_glow",
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
                    stiffness = Spring.StiffnessMedium,
                ),
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
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    fun restart() {
        hintsUsedThisAttempt = false
        gameState = gameState.reset()
        hintRevealIndex = -1
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeOffset.value }
            .background(DarkBackground),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val bottomSpace = PlayfieldSpec.balancingBottomSpace(maxWidth, maxHeight)
            Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                level = level,
                hintRevealIndex = hintRevealIndex,
                hintBounce = hintBounce.value,
                onBack = onBackToLevelSelect,
                onHint = {
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
            )

            StatusStrip(
                gameState = gameState,
                hintText = currentHintStep?.text,
                visitedEdgeCount = visitedEdgeCount,
                onRetry = { restart() },
            )

            Playfield(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        start = PlayfieldSpec.horizontalMargin,
                        end = PlayfieldSpec.horizontalMargin,
                        bottom = PlayfieldSpec.bottomMargin,
                    ),
                gameState = gameState,
                level = level,
                currentHintStep = currentHintStep,
                hintAlpha = hintAlpha,
                currentNodeGlow = currentNodeGlow,
                nodeScale = nodeScaleAnimatable.value,
                snapRingRadius = snapRingRadius.value,
                snapRingAlpha = snapRingAlpha.value,
                onStrokeStart = { nodeId ->
                    hintRevealIndex = -1
                    hintsUsedThisAttempt = false
                    gameState = gameState.reset().copy(currentStartNodeId = nodeId, currentNodeId = nodeId)
                    vibrateTick()
                },
                onStrokeMove = { nodeId ->
                    val next = gameState.moveTo(nodeId)
                    if (next !== gameState) {
                        gameState = next
                        if (!next.isGameOver) vibrateTick()
                    }
                },
                onStrokeEnd = { gameState = gameState.liftFinger() },
            )

            Spacer(Modifier.height(bottomSpace))
            }
        }

        // Red flash overlay on loss
        if (failFlashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Error.copy(alpha = failFlashAlpha.value)),
            )
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
                    },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OverlayScrim),
                    contentAlignment = Alignment.Center,
                ) {
                    // Confetti inside the overlay
                    ConfettiOverlay()

                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(
                            initialScale = 0.7f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        ) + fadeIn(animationSpec = tween(200)),
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
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    level: Level,
    hintRevealIndex: Int,
    hintBounce: Float,
    onBack: () -> Unit,
    onHint: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(PlayfieldSpec.topBarHeight)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button with press scale
        val backInteraction = remember { MutableInteractionSource() }
        val backPressed by backInteraction.collectIsPressedAsState()
        val backScale by animateFloatAsState(
            targetValue = if (backPressed) 0.9f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
            label = "back_btn_scale",
        )

        TextButton(
            onClick = onBack,
            interactionSource = backInteraction,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = backScale
                    scaleY = backScale
                }
                .background(DarkSurfaceVariant, CircleShape),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
            Text(
                text = "←",
                color = TextPrimary,
                fontSize = 18.sp,
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
                letterSpacing = 0.5.sp,
            )
            Text(
                text = "${level.name} · ${level.edges.size} lines",
                color = TextTertiary,
                fontSize = 12.sp,
                letterSpacing = 0.3.sp,
            )
        }

        // Hint button with press scale and bounce
        val hintInteraction = remember { MutableInteractionSource() }
        val hintPressed by hintInteraction.collectIsPressedAsState()
        val hintScale by animateFloatAsState(
            targetValue = if (hintPressed) 0.9f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
            label = "hint_btn_scale",
        )

        val totalHintSteps = level.hints.steps.size
        val hintRemaining = when {
            hintRevealIndex < 0 -> totalHintSteps
            else -> totalHintSteps - (hintRevealIndex + 1)
        }

        Box {
            TextButton(
                onClick = onHint,
                interactionSource = hintInteraction,
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer {
                        scaleX = hintScale
                        scaleY = hintScale
                    }
                    .background(
                        color = if (hintRevealIndex < 0) DarkSurfaceVariant else HintCyanSubtle,
                        shape = CircleShape,
                    ),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
            ) {
                Text(
                    text = "💡",
                    color = if (hintRevealIndex < 0) Accent else HintCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        scaleX = hintBounce
                        scaleY = hintBounce
                    },
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
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$hintRemaining",
                        color = DarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

/**
 * Fixed-height band between the top bar and the drawing. It shows, in order
 * of priority: why the last stroke failed (with a retry button), the progress
 * of the current stroke, or the active hint. Keeping it a fixed size means
 * the drawing below never moves or gets covered.
 */
@Composable
private fun StatusStrip(
    gameState: GameState,
    hintText: String?,
    visitedEdgeCount: Int,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PlayfieldSpec.statusStripHeight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            gameState.isGameOver && !gameState.isLevelComplete -> {
                val remaining = gameState.level.edges.count { !it.isVisited }
                val errorMessage = when (gameState.gameOverReason) {
                    GameOverReason.LIFTED_FINGER -> "Figure incomplete · $remaining ${if (remaining == 1) "line" else "lines"} left"
                    GameOverReason.RETRACED_EDGE -> "That line was already drawn"
                    GameOverReason.NO_LINE -> "No line between those dots"
                    null -> "Try again"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = errorMessage,
                        color = Error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = DarkBackground,
                        ),
                    ) {
                        Text(
                            text = "Retry",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }

            gameState.currentNodeId != null && !gameState.isLevelComplete -> {
                val totalEdges = gameState.level.edges.size
                val progress = visitedEdgeCount.toFloat() / totalEdges
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .background(DarkSurfaceVariant, RoundedCornerShape(2.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(EdgeVisited, RoundedCornerShape(2.dp)),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$visitedEdgeCount / $totalEdges",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            hintText != null -> {
                Text(
                    text = hintText,
                    color = HintCyan,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier
                        .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Playfield(
    modifier: Modifier,
    gameState: GameState,
    level: Level,
    currentHintStep: HintStep?,
    hintAlpha: Float,
    currentNodeGlow: Float,
    nodeScale: Float,
    snapRingRadius: Float,
    snapRingAlpha: Float,
    onStrokeStart: (Int) -> Unit,
    onStrokeMove: (Int) -> Unit,
    onStrokeEnd: () -> Unit,
) {
    val density = LocalDensity.current
    val nodeRadiusPx = with(density) { PlayfieldSpec.nodeRadius.toPx() }
    val hitRadiusPx = with(density) { PlayfieldSpec.hitRadius.toPx() }
    val defaultStrokeWidth = with(density) { 4.dp.toPx() }
    val visitedStrokeWidth = with(density) { 6.dp.toPx() }
    val missingDash = remember(density) {
        with(density) { PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 9.dp.toPx())) }
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    // Where the tracked finger is right now, for drawing the line as it is traced.
    var fingerPosition by remember { mutableStateOf<Offset?>(null) }
    val pixelNodes = remember(level.nodes, canvasSize) {
        layoutNodes(
            nodes = level.nodes,
            size = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat()),
            margin = hitRadiusPx,
        )
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(level.id, gameState.isLevelComplete, pixelNodes) {
                if (gameState.isLevelComplete) return@pointerInput

                awaitPointerEventScope {
                    while (true) {
                        // A stroke starts when a finger lands on a dot ...
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startNode = nodeAt(pixelNodes, down.position, hitRadiusPx) ?: continue
                        down.consume()
                        fingerPosition = down.position
                        onStrokeStart(startNode)

                        // ... and is followed by that same finger only.
                        var ended = false
                        while (!ended) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                change?.consume()
                                fingerPosition = null
                                onStrokeEnd()
                                ended = true
                            } else {
                                change.consume()
                                fingerPosition = change.position
                                val node = nodeAt(pixelNodes, change.position, hitRadiusPx)
                                if (node != null) {
                                    onStrokeMove(node)
                                }
                            }
                        }
                    }
                }
            },
    ) {
        if (pixelNodes.isEmpty()) return@Canvas

        val showRemaining = gameState.isGameOver && !gameState.isLevelComplete

        // Soft glow behind visited edges (drawn first, behind everything)
        gameState.level.edges.forEach { edge ->
            if (!edge.isVisited) return@forEach
            drawLine(
                color = EdgeVisited.copy(alpha = 0.18f),
                start = pixelNodes.getValue(edge.node1Id),
                end = pixelNodes.getValue(edge.node2Id),
                strokeWidth = visitedStrokeWidth * 2f,
                cap = StrokeCap.Round,
            )
        }

        // The line being traced right now, growing from the current dot toward the finger.
        val tracing = fingerPosition?.takeIf { gameState.currentNodeId != null && !gameState.isGameOver && !gameState.isLevelComplete }
            ?.let { finger -> partialLine(pixelNodes, gameState.level.edges, gameState.currentNodeId!!, finger, hitRadiusPx) }
        tracing?.let { (from, to) ->
            drawLine(
                color = EdgeVisited.copy(alpha = 0.18f),
                start = from,
                end = to,
                strokeWidth = visitedStrokeWidth * 2f,
                cap = StrokeCap.Round,
            )
        }

        // Draw edges
        gameState.level.edges.forEach { edge ->
            val startOff = pixelNodes.getValue(edge.node1Id)
            val endOff = pixelNodes.getValue(edge.node2Id)

            val isFailedEdge = gameState.failedEdge?.let { failed ->
                (edge.node1Id == failed.node1Id && edge.node2Id == failed.node2Id) ||
                    (edge.node1Id == failed.node2Id && edge.node2Id == failed.node1Id)
            } ?: false

            val color = when {
                isFailedEdge -> Error
                edge.isVisited -> EdgeVisited
                showRemaining -> EdgeMissing
                else -> EdgeDefault
            }
            val strokeWidth = if (edge.isVisited || isFailedEdge || showRemaining) visitedStrokeWidth else defaultStrokeWidth

            val isMissing = showRemaining && !edge.isVisited && !isFailedEdge
            drawLine(
                color = color,
                start = startOff,
                end = endOff,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = if (isMissing) missingDash else null,
            )
        }

        tracing?.let { (from, to) ->
            drawLine(
                color = EdgeVisited,
                start = from,
                end = to,
                strokeWidth = visitedStrokeWidth,
                cap = StrokeCap.Round,
            )
        }

        // Hint first edge highlight
        if (currentHintStep?.showFirstEdge == true) {
            val firstEdge = level.hints.firstEdge
            if (firstEdge != null) {
                val from = pixelNodes[firstEdge.first]
                val to = pixelNodes[firstEdge.second]
                if (from != null && to != null) {
                    drawLine(
                        color = HintCyan.copy(alpha = hintAlpha),
                        start = from,
                        end = to,
                        strokeWidth = visitedStrokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        // Draw nodes
        pixelNodes.forEach { (nodeId, pixelOffset) ->
            val color = when (nodeId) {
                gameState.currentStartNodeId -> NodeStart
                gameState.currentNodeId -> NodeCurrent
                else -> NodeDefault
            }

            val isCurrentNode = nodeId == gameState.currentNodeId

            // Current node ambient glow
            if (isCurrentNode) {
                drawCircle(
                    color = NodeCurrent.copy(alpha = 0.2f + currentNodeGlow * 0.3f),
                    radius = nodeRadiusPx + 8f + currentNodeGlow * 6f,
                    center = pixelOffset,
                )
            }

            // Hint glow on valid start nodes
            if (currentHintStep?.showValidStarts == true &&
                level.hints.validStartNodeIds.contains(nodeId) &&
                gameState.currentNodeId == null
            ) {
                drawCircle(
                    color = HintCyan.copy(alpha = hintAlpha * 0.5f),
                    radius = nodeRadiusPx + 10f,
                    center = pixelOffset,
                    style = Stroke(width = 3f),
                )
            }

            // Outer ring
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = nodeRadiusPx + 3f,
                center = pixelOffset,
                style = Stroke(width = 2f),
            )

            // Inner fill with bounce scale for current node
            val radius = if (isCurrentNode) nodeRadiusPx * nodeScale else nodeRadiusPx
            drawCircle(
                color = color,
                radius = radius,
                center = pixelOffset,
            )
        }

        // Snap ring effect at current node
        if (snapRingAlpha > 0f && gameState.currentNodeId != null) {
            val currentPixel = pixelNodes[gameState.currentNodeId]
            if (currentPixel != null) {
                drawCircle(
                    color = NodeCurrent.copy(alpha = snapRingAlpha),
                    radius = snapRingRadius,
                    center = currentPixel,
                    style = Stroke(width = 2f),
                )
            }
        }
    }
}

@Composable
private fun WinOverlayContent(
    gameState: GameState,
    hintsUsedThisAttempt: Boolean,
    onNextLevel: () -> Unit,
    onBackToLevelSelect: () -> Unit,
) {
    val starsEarned = if (hintsUsedThisAttempt) 1 else 3

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(DarkSurface, RoundedCornerShape(24.dp))
            .padding(32.dp),
    ) {
        Text(
            text = "Level ${gameState.currentLevelId} Complete",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Success,
            letterSpacing = 0.5.sp,
        )

        Spacer(Modifier.height(12.dp))

        // Star rating
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                Text(
                    text = "⭐",
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (index < starsEarned) 1f else 0.2f
                    },
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
                contentColor = DarkBackground,
            ),
        ) {
            Text(
                text = "Next Level",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onBackToLevelSelect,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Back to Levels",
                color = TextTertiary,
                fontSize = 14.sp,
            )
        }
    }
}
