package app.curious.lineflow

// The JVM target exists to run the level tests on the host; it has no speaker.
actual object BackgroundMusicManager {
    actual fun play() = Unit

    actual fun pause() = Unit

    actual fun isPlaying(): Boolean = false
}
