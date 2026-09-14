package app.curious.lineflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.curious.lineflow.ui.theme.DarkBackground
import app.curious.lineflow.ui.theme.LineFlowTheme

/**
 * The whole game. Platform entry points (MainActivity, the browser `main`)
 * build a [GameProgressRepository] over their storage and hand it here.
 */
@Composable
fun App(progressRepository: GameProgressRepository) {
    LineFlowTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.LevelSelect) }
        var musicEnabled by remember { mutableStateOf(progressRepository.isMusicEnabled()) }

        // Start music if enabled; the platform pauses it while the app is in the background.
        LaunchedEffect(musicEnabled) {
            if (musicEnabled) {
                BackgroundMusicManager.play()
            } else {
                BackgroundMusicManager.pause()
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground
        ) { innerPadding ->
            var showTutorial by remember { mutableStateOf(!progressRepository.hasSeenTutorial()) }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val direction =
                        when (targetState) {
                            is Screen.Game -> 1
                            is Screen.Settings -> 1
                            else -> -1
                        }
                    (
                        fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> direction * fullWidth / 4 },
                                animationSpec = tween(300)
                            )
                        ).togetherWith(
                        fadeOut(animationSpec = tween(200)) +
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -direction * fullWidth / 4 },
                                animationSpec = tween(200)
                            )
                    )
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.LevelSelect ->
                        LevelSelectScreen(
                            modifier = Modifier.padding(innerPadding),
                            progressRepository = progressRepository,
                            showTutorial = showTutorial,
                            onTutorialDismissed = {
                                showTutorial = false
                                progressRepository.markTutorialSeen()
                            },
                            onSettingsClicked = {
                                currentScreen = Screen.Settings
                            },
                            onLevelSelected = { levelId ->
                                progressRepository.setLastPlayedLevelId(levelId)
                                currentScreen = Screen.Game(levelId)
                            }
                        )
                    is Screen.Settings ->
                        SettingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            progressRepository = progressRepository,
                            onMusicToggled = { enabled ->
                                musicEnabled = enabled
                            },
                            onBack = {
                                currentScreen = Screen.LevelSelect
                            }
                        )
                    is Screen.Game ->
                        OneLineDrawGame(
                            modifier = Modifier.padding(innerPadding),
                            levelId = screen.levelId,
                            progressRepository = progressRepository,
                            onBackToLevelSelect = {
                                currentScreen = Screen.LevelSelect
                            },
                            onNextLevel = { nextLevelId ->
                                progressRepository.setLastPlayedLevelId(nextLevelId)
                                currentScreen = Screen.Game(nextLevelId)
                            }
                        )
                }
            }
        }
    }
}
