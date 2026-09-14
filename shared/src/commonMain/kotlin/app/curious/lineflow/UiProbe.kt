package app.curious.lineflow

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.testTag

/**
 * Where the tappable parts of the UI currently are, by tag, in window pixels.
 *
 * Compose paints to a canvas in the browser, so the screenshot harness
 * (`tool/screenshot.mjs`) has no DOM to query. The web entry point exposes
 * this registry as `window.lineflow.bounds(tag)` and the harness sends real
 * pointer events at those coordinates; on Android the same tags double as
 * Compose test tags. Tags: `settings`, `back`, `hint`, `retry`, `next-level`,
 * `back-to-levels`, `music`, `vibration`, `tutorial`, `level-<id>`,
 * `playfield` and `node-<id>` (a dot's touch circle).
 */
object UiProbe {
    private val rects = mutableMapOf<String, Rect>()

    fun update(tag: String, rect: Rect) {
        rects[tag] = rect
    }

    fun remove(tag: String) {
        rects.remove(tag)
    }

    fun bounds(tag: String): Rect? = rects[tag]

    fun tags(): List<String> = rects.keys.sorted()
}

/** Registers this element's window bounds under [tag] (and sets it as the test tag). */
fun Modifier.probe(tag: String): Modifier = this.then(ProbeElement(tag)).testTag(tag)

private class ProbeNode(var tag: String) :
    Modifier.Node(),
    GlobalPositionAwareModifierNode {
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        UiProbe.update(tag, coordinates.boundsInWindow())
    }

    override fun onDetach() {
        UiProbe.remove(tag)
    }
}

private data class ProbeElement(val tag: String) : ModifierNodeElement<ProbeNode>() {
    override fun create(): ProbeNode = ProbeNode(tag)

    override fun update(node: ProbeNode) {
        if (node.tag != tag) {
            UiProbe.remove(node.tag)
            node.tag = tag
        }
    }
}
