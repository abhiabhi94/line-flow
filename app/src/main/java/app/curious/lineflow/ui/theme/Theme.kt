package app.curious.lineflow.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LineFlowDarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = DarkBackground,
    primaryContainer = AccentDim,
    onPrimaryContainer = TextPrimary,
    secondary = HintCyan,
    onSecondary = DarkBackground,
    tertiary = Success,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary,
    outline = BorderDefault,
    outlineVariant = BorderHighlight
)

@Composable
fun LineFlowTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = LineFlowDarkScheme,
        typography = Typography,
        content = content
    )
}
