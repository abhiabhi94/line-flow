package app.curious.lineflow

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private lateinit var progressRepository: GameProgressRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The game is always dark: light icons on both system bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        // Debug installs ("app.curious.lineflow.debug") are for testing: every level open.
        AppConfig.unlockAllLevels = BuildConfig.DEBUG
        progressRepository = GameProgressRepository(SharedPreferencesStore(this))
        BackgroundMusicManager.initialize(this)

        setContent {
            App(progressRepository)
        }
    }

    override fun onResume() {
        super.onResume()
        if (progressRepository.isMusicEnabled()) {
            BackgroundMusicManager.play()
        }
    }

    override fun onPause() {
        super.onPause()
        BackgroundMusicManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundMusicManager.release()
    }
}
