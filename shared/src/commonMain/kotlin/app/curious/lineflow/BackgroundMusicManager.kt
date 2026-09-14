package app.curious.lineflow

/**
 * Background music: "Musical Ambient Background Loop" by AKTASOK
 * Source: https://pixabay.com/sound-effects/musical-ambient-background-loop-234090/
 * License: Pixabay Content License (free for commercial and non-commercial use)
 *
 * Each platform prepares the player in its entry point; [play] is safe to call
 * before that and simply does nothing.
 */
expect object BackgroundMusicManager {
    fun play()

    fun pause()

    fun isPlaying(): Boolean
}
