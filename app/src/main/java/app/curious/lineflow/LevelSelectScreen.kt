package app.curious.lineflow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.curious.lineflow.ui.theme.Accent
import app.curious.lineflow.ui.theme.AccentSubtle
import app.curious.lineflow.ui.theme.BorderAccent
import app.curious.lineflow.ui.theme.BorderDefault
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.DarkCard
import app.curious.lineflow.ui.theme.DarkSurface
import app.curious.lineflow.ui.theme.Success
import app.curious.lineflow.ui.theme.SuccessDim
import app.curious.lineflow.ui.theme.SuccessSubtle
import app.curious.lineflow.ui.theme.TextDisabled
import app.curious.lineflow.ui.theme.TextPrimary
import app.curious.lineflow.ui.theme.TextSecondary
import app.curious.lineflow.ui.theme.TextTertiary

@Composable
fun SettingsIconButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "settings_button_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(DarkCard)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u2699\uFE0F", // ⚙️
            fontSize = 26.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun LevelSelectScreen(
    modifier: Modifier = Modifier,
    progressRepository: GameProgressRepository,
    showTutorial: Boolean = false,
    onTutorialDismissed: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onLevelSelected: (Int) -> Unit
) {
    val completedLevels = remember { mutableStateOf(progressRepository.getCompletedLevelIds()) }
    val totalLevels = LevelManager.levels.size
    val completedCount = completedLevels.value.size

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(Modifier.height(48.dp))

        // Settings icon row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            SettingsIconButton(onClick = onSettingsClicked)
        }

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LINEFLOW",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Accent,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "One-line puzzles",
                fontSize = 14.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(24.dp))

            ProgressIndicator(
                completed = completedCount,
                total = totalLevels
            )
        }

        Spacer(Modifier.height(28.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(LevelManager.levels.size) { index ->
                val level = LevelManager.levels[index]
                val isCompleted = completedLevels.value.contains(level.id)
                val isUnlocked = progressRepository.isLevelUnlocked(level.id)

                LevelCard(
                    level = level,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    onClick = {
                        if (isUnlocked) onLevelSelected(level.id)
                    }
                )
            }
        }
    }

    if (showTutorial) {
        TutorialOverlay(onDismiss = onTutorialDismissed)
    }
    }
}

@Composable
private fun ProgressIndicator(completed: Int, total: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_bar"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$completed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (completed > 0) Success else TextTertiary
            )
            Text(
                text = " / $total",
                fontSize = 14.sp,
                color = TextTertiary
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BorderDefault)
        ) {
            if (animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Success, Accent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun LevelCard(
    level: Level,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCompleted -> SuccessDim.copy(alpha = 0.3f)
        isUnlocked -> DarkCard
        else -> DarkSurface.copy(alpha = 0.5f)
    }
    val borderColor = when {
        isCompleted -> Success.copy(alpha = 0.4f)
        isUnlocked -> BorderAccent
        else -> BorderDefault.copy(alpha = 0.5f)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_press_scale"
    )

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isUnlocked) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick
                ) else Modifier
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(SuccessSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\u2713",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
                !isUnlocked -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(BorderDefault.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83D\uDD12",
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AccentSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\u25B6",
                            fontSize = 12.sp,
                            color = Accent
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${level.id}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> Success
                    isUnlocked -> TextPrimary
                    else -> TextDisabled
                }
            )

            Text(
                text = level.name,
                fontSize = 10.sp,
                color = when {
                    isCompleted -> Success.copy(alpha = 0.7f)
                    isUnlocked -> TextTertiary
                    else -> TextDisabled.copy(alpha = 0.6f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            )
        }
    }
}
