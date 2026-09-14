package app.curious.lineflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LineFlowDarkScheme =
    darkColorScheme(
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

/**
 * The game is always dark. Android additionally styles the system bars in
 * MainActivity (enableEdgeToEdge); the browser paints the page background.
 */
@Composable
fun LineFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LineFlowDarkScheme,
        typography = Typography,
        content = content
    )
}
