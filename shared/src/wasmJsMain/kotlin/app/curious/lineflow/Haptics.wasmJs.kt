// Parameters of the js() functions are read by the JavaScript snippets.
@file:Suppress("UnusedParameter")

package app.curious.lineflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private fun vibrate(millis: Int): Unit = js("{ if (navigator.vibrate) navigator.vibrate(millis); }")

private fun vibratePattern(): Unit =
    js("{ if (navigator.vibrate) navigator.vibrate([50, 50, 50]); }")

/** navigator.vibrate where the browser offers it (Android Chrome); silently nothing elsewhere. */
private object BrowserHaptics : Haptics {
    override fun tick() = vibrate(10)

    override fun error() = vibratePattern()
}

@Composable
actual fun rememberHaptics(): Haptics = remember { BrowserHaptics }
