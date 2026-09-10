package app.curious.lineflow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Every level must be drawable by hand: solvable in one stroke, and laid out
 * so a finger can reach each dot without brushing another one. The numbers
 * mirror `.scripts/leveldesign/geometry.py`, which generates Graph.kt.
 */
class LevelValidationTest {

    companion object {
        const val EXPECTED_LEVELS = 50

        // Smallest play area we support: a 360dp-wide phone with 24dp side
        // margins, and a short 16:9 screen once the top bar and status strip
        // are taken away.
        const val PLAY_WIDTH_DP = 312f
        const val PLAY_HEIGHT_DP = 410f

        const val HIT_RADIUS_DP = 32f
        const val MIN_NODE_DISTANCE_DP = 72f
        const val MIN_NODE_EDGE_DISTANCE_DP = 44f
        const val MIN_CROSSING_NODE_DISTANCE_DP = 44f
        const val MIN_EDGE_ANGLE_DEGREES = 29.5
    }

    private fun Level.degrees(): Map<Int, Int> {
        val degree = nodes.associate { it.id to 0 }.toMutableMap()
        edges.forEach { edge ->
            degree[edge.node1Id] = degree.getValue(edge.node1Id) + 1
            degree[edge.node2Id] = degree.getValue(edge.node2Id) + 1
        }
        return degree
    }

    private fun Level.oddNodes(): Set<Int> = degrees().filterValues { it % 2 != 0 }.keys

    /**
     * Dots in dp on the smallest supported play area, without the vertical
     * stretch (stretching only ever moves dots further apart, so this is the
     * conservative case for every distance check).
     */
    private fun Level.layoutOnSmallPhone(): Map<Int, Offset> =
        layoutNodes(nodes, Size(PLAY_WIDTH_DP + 2 * HIT_RADIUS_DP, PLAY_HEIGHT_DP + 2 * HIT_RADIUS_DP), HIT_RADIUS_DP, maxStretch = 1f)

    // ------------------------------------------------------------------
    // Graph structure
    // ------------------------------------------------------------------

    @Test
    fun expectedNumberOfLevelsInOrder() {
        assertEquals("Expected $EXPECTED_LEVELS levels", EXPECTED_LEVELS, LevelManager.levels.size)
        assertEquals("Levels must be numbered 1..$EXPECTED_LEVELS in order", (1..EXPECTED_LEVELS).toList(), LevelManager.levels.map { it.id })
    }

    @Test
    fun levelNamesAreUnique() {
        val names = LevelManager.levels.map { it.name }
        assertEquals("Level names must be unique: $names", names.size, names.toSet().size)
    }

    @Test
    fun allEdgesReferenceValidDistinctNodesOnce() {
        LevelManager.levels.forEach { level ->
            val nodeIds = level.nodes.map { it.id }.toSet()
            assertEquals("Level ${level.id}: node ids must be unique", level.nodes.size, nodeIds.size)
            val seen = mutableSetOf<Set<Int>>()
            level.edges.forEach { edge ->
                assertTrue("Level ${level.id}: edge references unknown node ${edge.node1Id}", edge.node1Id in nodeIds)
                assertTrue("Level ${level.id}: edge references unknown node ${edge.node2Id}", edge.node2Id in nodeIds)
                assertTrue("Level ${level.id}: edge ${edge.node1Id}-${edge.node2Id} is a loop", edge.node1Id != edge.node2Id)
                assertTrue(
                    "Level ${level.id}: duplicate edge ${edge.node1Id}-${edge.node2Id}",
                    seen.add(setOf(edge.node1Id, edge.node2Id)),
                )
            }
            level.nodes.forEach { node ->
                assertTrue("Level ${level.id}: node ${node.id} has no lines", level.edges.any { it.containsNode(node.id) })
            }
        }
    }

    @Test
    fun allLevelsAreConnected() {
        LevelManager.levels.forEach { level ->
            val adjacency = level.nodes.associate { it.id to mutableSetOf<Int>() }
            level.edges.forEach { edge ->
                adjacency.getValue(edge.node1Id).add(edge.node2Id)
                adjacency.getValue(edge.node2Id).add(edge.node1Id)
            }
            val visited = mutableSetOf(level.nodes.first().id)
            val queue = ArrayDeque(visited)
            while (queue.isNotEmpty()) {
                adjacency.getValue(queue.removeFirst()).forEach { if (visited.add(it)) queue.add(it) }
            }
            assertEquals("Level ${level.id} (${level.name}) is not connected", level.nodes.size, visited.size)
        }
    }

    @Test
    fun allLevelsHaveZeroOrTwoOddNodes() {
        LevelManager.levels.forEach { level ->
            val odd = level.oddNodes()
            assertTrue(
                "Level ${level.id} (${level.name}) has ${odd.size} odd-degree nodes $odd; must be 0 (loop) or 2 (path)",
                odd.isEmpty() || odd.size == 2,
            )
        }
    }

    @Test
    fun everyLevelHasAVerifiedOneStrokeSolutionFromTheHintedEdge() {
        LevelManager.levels.forEach { level ->
            val firstEdge = level.hints.firstEdge
            assertNotNull("Level ${level.id}: hint must name a first edge", firstEdge)
            val trail = EulerSolver.trail(level, firstEdge!!.first, firstEdge.second)
            assertNotNull(
                "Level ${level.id} (${level.name}) cannot be drawn in one stroke starting ${firstEdge.first}->${firstEdge.second}",
                trail,
            )
            // Replay the trail exactly like a finger would.
            var state = GameState(level).reset().copy(currentStartNodeId = trail!!.first(), currentNodeId = trail.first())
            trail.drop(1).forEach { next ->
                state = state.moveTo(next)
                assertFalse("Level ${level.id}: solution stroke failed at dot $next (${state.gameOverReason})", state.isGameOver)
            }
            assertTrue("Level ${level.id}: solution stroke did not cover every line", state.isLevelComplete)
        }
    }

    // ------------------------------------------------------------------
    // Hints
    // ------------------------------------------------------------------

    @Test
    fun hintValidStartNodesMatchOddDegreeNodes() {
        LevelManager.levels.forEach { level ->
            val odd = level.oddNodes()
            val expected = if (odd.isEmpty()) level.nodes.map { it.id }.toSet() else odd
            assertEquals(
                "Level ${level.id} (${level.name}): valid starts should be $expected",
                expected,
                level.hints.validStartNodeIds.toSet(),
            )
            assertTrue(
                "Level ${level.id}: hinted first edge must leave a valid start",
                level.hints.firstEdge!!.first in level.hints.validStartNodeIds,
            )
        }
    }

    @Test
    fun hintStepsFollowTheSharedFormat() {
        LevelManager.levels.forEach { level ->
            val steps = level.hints.steps
            val expected = if (level.id <= 10) 2 else 3
            assertEquals("Level ${level.id} (${level.name}) should have $expected hint steps", expected, steps.size)
            steps.forEach { assertTrue("Level ${level.id}: empty hint text", it.text.isNotBlank()) }
            assertFalse("Level ${level.id}: first hint is text only", steps.first().showValidStarts || steps.first().showFirstEdge)
            assertTrue("Level ${level.id}: last hint shows valid starts", steps.last().showValidStarts)
            assertTrue("Level ${level.id}: last hint shows the first edge", steps.last().showFirstEdge)
            for (i in 1 until steps.size) {
                if (steps[i - 1].showValidStarts) assertTrue("Level ${level.id}: hints must not hide starts again", steps[i].showValidStarts)
                if (steps[i - 1].showFirstEdge) assertTrue("Level ${level.id}: hints must not hide the first edge again", steps[i].showFirstEdge)
            }
        }
    }

    // ------------------------------------------------------------------
    // Layout: everything measured in dp on the smallest phone we support
    // ------------------------------------------------------------------

    @Test
    fun dotsAreFarEnoughApartToTouch() {
        LevelManager.levels.forEach { level ->
            val pts = level.layoutOnSmallPhone()
            val ids = pts.keys.toList()
            for (i in ids.indices) for (j in i + 1 until ids.size) {
                val d = (pts.getValue(ids[i]) - pts.getValue(ids[j])).getDistance()
                assertTrue(
                    "Level ${level.id} (${level.name}): dots ${ids[i]} and ${ids[j]} are only ${d.toInt()}dp apart",
                    d >= MIN_NODE_DISTANCE_DP - 0.5f,
                )
            }
        }
    }

    @Test
    fun noDotSitsCloseToALineItIsNotPartOf() {
        LevelManager.levels.forEach { level ->
            val pts = level.layoutOnSmallPhone()
            level.edges.forEach { edge ->
                val a = pts.getValue(edge.node1Id)
                val b = pts.getValue(edge.node2Id)
                level.nodes.forEach { node ->
                    if (edge.containsNode(node.id)) return@forEach
                    val d = pointToSegment(pts.getValue(node.id), a, b)
                    assertTrue(
                        "Level ${level.id} (${level.name}): dot ${node.id} is ${d.toInt()}dp from line " +
                            "${edge.node1Id}-${edge.node2Id}; tracing it would touch the dot",
                        d >= MIN_NODE_EDGE_DISTANCE_DP - 0.5f,
                    )
                }
            }
        }
    }

    @Test
    fun linesLeavingADotAreVisuallySeparate() {
        LevelManager.levels.forEach { level ->
            val pts = level.layoutOnSmallPhone()
            level.nodes.forEach { node ->
                val here = pts.getValue(node.id)
                val angles = level.edges
                    .filter { it.containsNode(node.id) }
                    .map { edge -> if (edge.node1Id == node.id) edge.node2Id else edge.node1Id }
                    .map { other -> Math.toDegrees(atan2((pts.getValue(other).y - here.y).toDouble(), (pts.getValue(other).x - here.x).toDouble())) }
                    .sorted()
                if (angles.size < 2) return@forEach
                for (i in angles.indices) {
                    var separation = angles[(i + 1) % angles.size] - angles[i]
                    if (separation <= 0) separation += 360.0
                    assertTrue(
                        "Level ${level.id} (${level.name}): lines at dot ${node.id} are only ${"%.1f".format(separation)} degrees apart",
                        separation >= MIN_EDGE_ANGLE_DEGREES,
                    )
                }
            }
        }
    }

    @Test
    fun crossingsNeverHappenNearADot() {
        LevelManager.levels.forEach { level ->
            val pts = level.layoutOnSmallPhone()
            val edges = level.edges
            for (i in edges.indices) for (j in i + 1 until edges.size) {
                val e1 = edges[i]
                val e2 = edges[j]
                if (setOf(e1.node1Id, e1.node2Id, e2.node1Id, e2.node2Id).size < 4) continue
                val x = segmentIntersection(pts.getValue(e1.node1Id), pts.getValue(e1.node2Id), pts.getValue(e2.node1Id), pts.getValue(e2.node2Id))
                    ?: continue
                level.nodes.forEach { node ->
                    val d = (pts.getValue(node.id) - x).getDistance()
                    assertTrue(
                        "Level ${level.id} (${level.name}): lines cross ${d.toInt()}dp from dot ${node.id}",
                        d >= MIN_CROSSING_NODE_DISTANCE_DP - 0.5f,
                    )
                }
            }
        }
    }

    @Test
    fun tallScreensStretchSquatLevelsOnlyUpToTheCap() {
        val square = LevelManager.getLevel(2)!! // The Square, aspect 1.0
        val margin = 20f
        // Play area twice as tall as wide: the square is stretched by the cap, not to fill.
        val tall = layoutNodes(square.nodes, Size(300f, 600f), margin).values
        val width = tall.maxOf { it.x } - tall.minOf { it.x }
        val height = tall.maxOf { it.y } - tall.minOf { it.y }
        assertEquals(260f, width, 0.5f)
        assertEquals(260f * MAX_VERTICAL_STRETCH, height, 0.5f)
        // Play area only slightly taller: stretched just enough to fill it.
        val slight = layoutNodes(square.nodes, Size(300f, 330f), margin).values
        assertEquals(290f, slight.maxOf { it.y } - slight.minOf { it.y }, 0.5f)
        // Wide play area: never stretched horizontally.
        val wide = layoutNodes(square.nodes, Size(600f, 300f), margin).values
        assertEquals(260f, wide.maxOf { it.x } - wide.minOf { it.x }, 0.5f)
        assertEquals(260f, wide.maxOf { it.y } - wide.minOf { it.y }, 0.5f)
    }

    @Test
    fun layoutFillsTheAvailableAreaAndStaysCentred() {
        val size = Size(600f, 900f)
        val margin = 40f
        LevelManager.levels.forEach { level ->
            val pts = layoutNodes(level.nodes, size, margin, maxStretch = 1f).values
            val minX = pts.minOf { it.x }
            val maxX = pts.maxOf { it.x }
            val minY = pts.minOf { it.y }
            val maxY = pts.maxOf { it.y }
            assertTrue("Level ${level.id}: drawing leaves the play area", minX >= margin - 0.5f && maxX <= size.width - margin + 0.5f)
            assertTrue("Level ${level.id}: drawing leaves the play area", minY >= margin - 0.5f && maxY <= size.height - margin + 0.5f)
            assertEquals("Level ${level.id}: not horizontally centred", size.width / 2, (minX + maxX) / 2, 0.5f)
            assertEquals("Level ${level.id}: not vertically centred", size.height / 2, (minY + maxY) / 2, 0.5f)
            val touchesWidth = abs(maxX - minX - (size.width - 2 * margin)) < 0.5f
            val touchesHeight = abs(maxY - minY - (size.height - 2 * margin)) < 0.5f
            assertTrue("Level ${level.id}: drawing should be scaled up to the margins", touchesWidth || touchesHeight)
        }
    }

    // ------------------------------------------------------------------
    // Difficulty curve
    // ------------------------------------------------------------------

    @Test
    fun firstLevelsAreTinyAndNoLevelHasFewerLinesThanTheOneBefore() {
        val levels = LevelManager.levels
        (0 until 5).forEach { i ->
            assertTrue("Level ${i + 1} should have at most 7 lines", levels[i].edges.size <= 7)
        }
        assertTrue("Last level should be the biggest stroke", levels.last().edges.size >= 40)
        levels.zipWithNext().forEach { (before, after) ->
            assertTrue(
                "Level ${after.id} (${after.edges.size} lines) is smaller than level ${before.id} (${before.edges.size} lines)",
                after.edges.size >= before.edges.size,
            )
        }
    }

    // ------------------------------------------------------------------
    // Game rules (pure state)
    // ------------------------------------------------------------------

    @Test
    fun retracingALineEndsTheStroke() {
        val level = LevelManager.getLevel(2)!! // The Square
        var state = GameState(level).reset().copy(currentStartNodeId = 0, currentNodeId = 0)
        state = state.moveTo(1)
        assertFalse(state.isGameOver)
        state = state.moveTo(0)
        assertTrue(state.isGameOver)
        assertEquals(GameOverReason.RETRACED_EDGE, state.gameOverReason)
        assertNotNull(state.failedEdge)
    }

    @Test
    fun connectingTwoDotsWithoutALineEndsTheStroke() {
        val level = LevelManager.getLevel(2)!! // The Square: no diagonal
        val state = GameState(level).reset().copy(currentStartNodeId = 0, currentNodeId = 0).moveTo(2)
        assertTrue(state.isGameOver)
        assertEquals(GameOverReason.NO_LINE, state.gameOverReason)
    }

    @Test
    fun liftingTheFingerEarlyEndsTheStrokeButNotAfterCompletion() {
        val level = LevelManager.getLevel(1)!! // The Triangle
        var state = GameState(level).reset().copy(currentStartNodeId = 0, currentNodeId = 0).moveTo(1)
        assertEquals(GameOverReason.LIFTED_FINGER, state.liftFinger().gameOverReason)
        state = state.moveTo(2).moveTo(0)
        assertTrue(state.isLevelComplete)
        assertFalse(state.liftFinger().isGameOver)
    }

    @Test
    fun stayingOnTheSameDotIsNotAMove() {
        val level = LevelManager.getLevel(1)!!
        val state = GameState(level).reset().copy(currentStartNodeId = 0, currentNodeId = 0)
        assertTrue(state.moveTo(0) === state)
    }

    @Test
    fun nodeAtPicksTheDotUnderTheFingerOnlyWithinTheHitRadius() {
        val pts = mapOf(0 to Offset(100f, 100f), 1 to Offset(300f, 100f))
        assertEquals(0, nodeAt(pts, Offset(120f, 110f), 32f))
        assertEquals(1, nodeAt(pts, Offset(280f, 100f), 32f))
        assertEquals(null, nodeAt(pts, Offset(200f, 100f), 32f))
    }

    // ------------------------------------------------------------------
    // Geometry helpers
    // ------------------------------------------------------------------

    private fun pointToSegment(p: Offset, a: Offset, b: Offset): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val l2 = dx * dx + dy * dy
        if (l2 == 0f) return (p - a).getDistance()
        val t = max(0f, min(1f, ((p.x - a.x) * dx + (p.y - a.y) * dy) / l2))
        return hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
    }

    private fun segmentIntersection(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Offset? {
        val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
        if (abs(d) < 1e-6f) return null
        val t = ((p1.x - p3.x) * (p3.y - p4.y) - (p1.y - p3.y) * (p3.x - p4.x)) / d
        val u = -((p1.x - p2.x) * (p1.y - p3.y) - (p1.y - p2.y) * (p1.x - p3.x)) / d
        val eps = 1e-4f
        if (t <= eps || t >= 1 - eps || u <= eps || u >= 1 - eps) return null
        return Offset(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y))
    }
}

/** Hierholzer's algorithm, used by the tests to prove each level is drawable. */
object EulerSolver {
    /**
     * A one-stroke trail through every line of [level] that begins with the
     * line [start]->[first], or null if none exists.
     */
    fun trail(level: Level, start: Int, first: Int): List<Int>? {
        val edges = level.edges.map { setOf(it.node1Id, it.node2Id) }
        val used = BooleanArray(edges.size)
        val incident = level.nodes.associate { node -> node.id to edges.indices.filter { node.id in edges[it] } }
        val firstIndex = edges.indexOf(setOf(start, first))
        if (firstIndex < 0) return null
        used[firstIndex] = true

        val pointer = level.nodes.associate { it.id to 0 }.toMutableMap()
        val stack = ArrayDeque(listOf(first))
        val circuit = mutableListOf<Int>()
        while (stack.isNotEmpty()) {
            val v = stack.last()
            val candidates = incident.getValue(v)
            var p = pointer.getValue(v)
            while (p < candidates.size && used[candidates[p]]) p++
            pointer[v] = p
            if (p < candidates.size) {
                used[candidates[p]] = true
                stack.addLast((edges[candidates[p]] - v).first())
            } else {
                circuit.add(stack.removeLast())
            }
        }
        val trail = listOf(start) + circuit.reversed()
        return if (isValid(level, trail)) trail else null
    }

    private fun isValid(level: Level, trail: List<Int>): Boolean {
        if (trail.size != level.edges.size + 1) return false
        val pool = level.edges.groupingBy { setOf(it.node1Id, it.node2Id) }.eachCount().toMutableMap()
        trail.zipWithNext().forEach { (a, b) ->
            val key = setOf(a, b)
            val left = pool[key] ?: return false
            if (left == 0) return false
            pool[key] = left - 1
        }
        return pool.values.all { it == 0 }
    }
}

class PlayfieldLayoutTest {
    @Test
    fun tallScreensBalanceTheTopChromeWithSpaceBelowTheDrawing() {
        // A tall phone: the drawing is pushed up to the optical centre.
        val tall = PlayfieldSpec.balancingBottomSpace(screenWidth = 393.dp, screenHeight = 800.dp)
        assertEquals(PlayfieldSpec.topBarHeight + PlayfieldSpec.statusStripHeight - PlayfieldSpec.bottomMargin, tall)
    }

    @Test
    fun shortScreensKeepTheirPlayHeight() {
        // A short 16:9 phone has no slack: nothing is taken from the drawing.
        val short = PlayfieldSpec.balancingBottomSpace(screenWidth = 360.dp, screenHeight = 560.dp)
        assertEquals(0.dp, short)
        // In between, only the slack above the minimum play aspect is used.
        val medium = PlayfieldSpec.balancingBottomSpace(screenWidth = 360.dp, screenHeight = 620.dp)
        assertTrue(medium > 0.dp && medium < PlayfieldSpec.topBarHeight + PlayfieldSpec.statusStripHeight - PlayfieldSpec.bottomMargin)
    }
}
