package app.curious.lineflow

import android.content.Context
import android.media.MediaPlayer

/**
 * Background music: "Musical Ambient Background Loop" by AKTASOK
 * Source: https://pixabay.com/sound-effects/musical-ambient-background-loop-234090/
 * License: Pixabay Content License (free for commercial and non-commercial use)
 */
object BackgroundMusicManager {

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    fun initialize(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.serene_loop).apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
            }
            isPrepared = true
        }
    }

    fun play() {
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}
