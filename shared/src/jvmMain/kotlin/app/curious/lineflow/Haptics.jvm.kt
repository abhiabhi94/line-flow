package app.curious.lineflow

import androidx.compose.runtime.Composable

// The JVM target exists to run the level tests on the host; it has no motor.
private object NoHaptics : Haptics {
    override fun tick() = Unit

    override fun error() = Unit
}

@Composable
actual fun rememberHaptics(): Haptics = NoHaptics
