package app.curious.lineflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import app.curious.lineflow.ui.theme.DarkBackground
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.curious.lineflow.ui.theme.LineFlowTheme

sealed interface Screen {
    data object LevelSelect : Screen
    data object Settings : Screen
    data class Game(val levelId: Int) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val progressRepository = GameProgressRepository(this)
        BackgroundMusicManager.initialize(this)

        setContent {
            LineFlowTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.LevelSelect) }
                var musicEnabled by remember { mutableStateOf(progressRepository.isMusicEnabled()) }

                // Handle lifecycle for music playback
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner, musicEnabled) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                if (musicEnabled) {
                                    BackgroundMusicManager.play()
                                }
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                BackgroundMusicManager.pause()
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    // Start music if enabled
                    if (musicEnabled) {
                        BackgroundMusicManager.play()
                    }

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
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
                            val direction = when (targetState) {
                                is Screen.Game -> 1
                                is Screen.Settings -> 1
                                else -> -1
                            }
                            (fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> direction * fullWidth / 4 },
                                    animationSpec = tween(300)
                                )).togetherWith(
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
                            is Screen.LevelSelect -> LevelSelectScreen(
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
                                    currentScreen = Screen.Game(levelId)
                                }
                            )
                            is Screen.Settings -> SettingsScreen(
                                modifier = Modifier.padding(innerPadding),
                                progressRepository = progressRepository,
                                onMusicToggled = { enabled ->
                                    musicEnabled = enabled
                                    if (enabled) {
                                        BackgroundMusicManager.play()
                                    } else {
                                        BackgroundMusicManager.pause()
                                    }
                                },
                                onBack = {
                                    currentScreen = Screen.LevelSelect
                                }
                            )
                            is Screen.Game -> OneLineDrawGame(
                                modifier = Modifier.padding(innerPadding),
                                levelId = screen.levelId,
                                progressRepository = progressRepository,
                                onBackToLevelSelect = {
                                    currentScreen = Screen.LevelSelect
                                },
                                onNextLevel = { nextLevelId ->
                                    currentScreen = Screen.Game(nextLevelId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundMusicManager.release()
    }
}
