// Parameters of the js() functions are read by the JavaScript snippets.
@file:Suppress("UnusedParameter")

package app.curious.lineflow

private fun audioCreate(src: String): JsAny = js(
    """
        (function () {
            var audio = new Audio(src);
            audio.loop = true;
            audio.volume = 0.5;
            audio.preload = 'auto';
            return audio;
        })()
        """
)

// Browsers refuse audio before the first user gesture; when play() is
// rejected, retry on the next pointer press.
private fun audioPlay(audio: JsAny): Unit = js(
    """
        {
            var attempt = function () {
                var p = audio.play();
                if (p && p.catch) {
                    p.catch(function () {
                        document.addEventListener('pointerdown', attempt, { once: true });
                    });
                }
            };
            attempt();
        }
        """
)

private fun audioPause(audio: JsAny): Unit = js("audio.pause()")

private fun audioIsPlaying(audio: JsAny): Boolean = js("!audio.paused")

actual object BackgroundMusicManager {
    private var audio: JsAny? = null

    /** Prepares the player; [src] is resolved against the page URL. */
    fun initialize(src: String) {
        if (audio == null) {
            audio = audioCreate(src)
        }
    }

    actual fun play() {
        audio?.let { if (!audioIsPlaying(it)) audioPlay(it) }
    }

    actual fun pause() {
        audio?.let { if (audioIsPlaying(it)) audioPause(it) }
    }

    actual fun isPlaying(): Boolean = audio?.let { audioIsPlaying(it) } ?: false
}
