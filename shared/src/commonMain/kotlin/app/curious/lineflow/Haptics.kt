package app.curious.lineflow

import androidx.compose.runtime.Composable

/** Short tactile feedback while drawing; a no-op where the device has none. */
interface Haptics {
    fun tick()

    fun error()
}

@Composable
expect fun rememberHaptics(): Haptics
