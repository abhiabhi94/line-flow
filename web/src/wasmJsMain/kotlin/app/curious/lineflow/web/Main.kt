// Parameters of the js() functions are read by the JavaScript snippets.
@file:Suppress("UnusedParameter")

package app.curious.lineflow.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.curious.lineflow.App
import app.curious.lineflow.BackgroundMusicManager
import app.curious.lineflow.GameProgressRepository
import app.curious.lineflow.LocalStorageStore
import app.curious.lineflow.UiProbe

private fun devicePixelRatio(): Double = js("window.devicePixelRatio || 1")

// Automation hook for tool/screenshot.mjs: window.lineflowProbe.bounds(tag) returns
// "x,y,width,height" in CSS pixels (or null) and window.lineflowProbe.tags() lists
// the known tags. It reads a registry the UI keeps; it cannot act on the UI.
private fun installAutomationBridge(bounds: (String) -> String?, tags: () -> String): Unit =
    js("{ window.lineflowProbe = { bounds: bounds, tags: tags }; }")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val progressRepository = GameProgressRepository(LocalStorageStore())
    BackgroundMusicManager.initialize("serene_loop.mp3")
    installAutomationBridge(
        bounds = { tag ->
            UiProbe.bounds(tag)?.let { rect ->
                val scale = devicePixelRatio()
                listOf(rect.left, rect.top, rect.width, rect.height).joinToString(",") {
                    (it / scale).toString()
                }
            }
        },
        tags = { UiProbe.tags().joinToString(",") }
    )

    ComposeViewport(viewportContainerId = "lineflow") {
        App(progressRepository)
    }
}
