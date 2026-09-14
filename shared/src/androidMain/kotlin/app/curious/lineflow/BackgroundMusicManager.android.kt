package app.curious.lineflow

import android.content.Context
import android.media.MediaPlayer
import app.curious.lineflow.shared.R

actual object BackgroundMusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    fun initialize(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer =
                MediaPlayer.create(context.applicationContext, R.raw.serene_loop).apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f)
                }
            isPrepared = true
        }
    }

    actual fun play() {
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    actual fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }

    actual fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
