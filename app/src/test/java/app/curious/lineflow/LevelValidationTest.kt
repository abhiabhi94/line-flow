package app.curious.lineflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelValidationTest {

    @Test
    fun allLevelsHaveValidEulerianPath() {
        LevelManager.levels.forEach { level ->
            val degreeMap = mutableMapOf<Int, Int>()
            level.nodes.forEach { degreeMap[it.id] = 0 }
            level.edges.forEach { edge ->
                degreeMap[edge.node1Id] = (degreeMap[edge.node1Id] ?: 0) + 1
                degreeMap[edge.node2Id] = (degreeMap[edge.node2Id] ?: 0) + 1
            }

            val oddDegreeCount = degreeMap.values.count { it % 2 != 0 }
            assertTrue(
                "Level ${level.id} (${level.name}) has $oddDegreeCount odd-degree nodes. " +
                    "Must be 0 (circuit) or 2 (path). Degrees: $degreeMap",
                oddDegreeCount == 0 || oddDegreeCount == 2
            )
        }
    }

    @Test
    fun allLevelsAreConnected() {
        LevelManager.levels.forEach { level ->
            val adjacency = mutableMapOf<Int, MutableSet<Int>>()
            level.nodes.forEach { adjacency[it.id] = mutableSetOf() }
            level.edges.forEach { edge ->
                adjacency[edge.node1Id]?.add(edge.node2Id)
                adjacency[edge.node2Id]?.add(edge.node1Id)
            }

            val visited = mutableSetOf<Int>()
            val queue = ArrayDeque<Int>()
            val startId = level.nodes.first().id
            queue.add(startId)
            visited.add(startId)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                adjacency[current]?.forEach { neighbor ->
                    if (neighbor !in visited) {
                        visited.add(neighbor)
                        queue.add(neighbor)
                    }
                }
            }

            assertEquals(
                "Level ${level.id} (${level.name}) is not connected. " +
                    "Reachable: $visited, Total nodes: ${level.nodes.map { it.id }}",
                level.nodes.size,
                visited.size
            )
        }
    }

    @Test
    fun hintValidStartNodesMatchOddDegreeNodes() {
        LevelManager.levels.forEach { level ->
            val degreeMap = mutableMapOf<Int, Int>()
            level.nodes.forEach { degreeMap[it.id] = 0 }
            level.edges.forEach { edge ->
                degreeMap[edge.node1Id] = (degreeMap[edge.node1Id] ?: 0) + 1
                degreeMap[edge.node2Id] = (degreeMap[edge.node2Id] ?: 0) + 1
            }

            val oddDegreeNodes = degreeMap.filter { it.value % 2 != 0 }.keys
            val isCircuit = oddDegreeNodes.isEmpty()

            if (isCircuit) {
                // For circuits, all nodes are valid starts
                assertEquals(
                    "Level ${level.id} (${level.name}) is a circuit but hint doesn't list all nodes as valid starts",
                    level.nodes.map { it.id }.toSet(),
                    level.hints.validStartNodeIds.toSet()
                )
            } else {
                // For paths, only odd-degree nodes are valid starts
                assertEquals(
                    "Level ${level.id} (${level.name}) has odd-degree nodes $oddDegreeNodes " +
                        "but hint lists ${level.hints.validStartNodeIds}",
                    oddDegreeNodes,
                    level.hints.validStartNodeIds.toSet()
                )
            }
        }
    }

    @Test
    fun allLevelsHaveUniqueIds() {
        val ids = LevelManager.levels.map { it.id }
        assertEquals(
            "Level IDs are not unique: $ids",
            ids.size,
            ids.toSet().size
        )
    }

    @Test
    fun levelsAreInOrder() {
        val ids = LevelManager.levels.map { it.id }
        assertEquals(
            "Levels are not in sequential order",
            (1..25).toList(),
            ids
        )
    }

    @Test
    fun exactly15LevelsExist() {
        assertEquals("Expected 25 levels", 25, LevelManager.levels.size)
    }

    @Test
    fun allEdgesReferenceValidNodes() {
        LevelManager.levels.forEach { level ->
            val nodeIds = level.nodes.map { it.id }.toSet()
            level.edges.forEach { edge ->
                assertTrue(
                    "Level ${level.id}: Edge references invalid node1Id ${edge.node1Id}",
                    edge.node1Id in nodeIds
                )
                assertTrue(
                    "Level ${level.id}: Edge references invalid node2Id ${edge.node2Id}",
                    edge.node2Id in nodeIds
                )
            }
        }
    }

    @Test
    fun hintFirstEdgeIsValid() {
        LevelManager.levels.forEach { level ->
            val firstEdge = level.hints.firstEdge ?: return@forEach
            val hasEdge = level.edges.any { edge ->
                (edge.node1Id == firstEdge.first && edge.node2Id == firstEdge.second) ||
                    (edge.node1Id == firstEdge.second && edge.node2Id == firstEdge.first)
            }
            assertTrue(
                "Level ${level.id}: Hint firstEdge (${firstEdge.first}, ${firstEdge.second}) " +
                    "does not match any edge in the level",
                hasEdge
            )
        }
    }

    @Test
    fun allLevelsHaveAtLeastTwoHintSteps() {
        LevelManager.levels.forEach { level ->
            assertTrue(
                "Level ${level.id} (${level.name}) must have at least 2 hint steps, has ${level.hints.steps.size}",
                level.hints.steps.size >= 2
            )
        }
    }

    @Test
    fun earlyLevelsHaveTwoHints_laterLevelsHaveThree() {
        LevelManager.levels.forEach { level ->
            val expectedMin = if (level.id <= 10) 2 else 3
            assertTrue(
                "Level ${level.id} (${level.name}) expected at least $expectedMin hint steps, has ${level.hints.steps.size}",
                level.hints.steps.size >= expectedMin
            )
        }
    }

    @Test
    fun lastHintStepAlwaysShowsFirstEdge() {
        LevelManager.levels.forEach { level ->
            val lastStep = level.hints.steps.last()
            assertTrue(
                "Level ${level.id} (${level.name}) last hint step must show first edge",
                lastStep.showFirstEdge
            )
            assertTrue(
                "Level ${level.id} (${level.name}) last hint step must show valid starts",
                lastStep.showValidStarts
            )
        }
    }

    @Test
    fun firstHintStepIsTextOnly() {
        LevelManager.levels.forEach { level ->
            val firstStep = level.hints.steps.first()
            assertFalse(
                "Level ${level.id} (${level.name}) first hint step should not show visual overlays",
                firstStep.showValidStarts || firstStep.showFirstEdge
            )
        }
    }

    @Test
    fun hintStepsAreProgressivelyRevealing() {
        LevelManager.levels.forEach { level ->
            val steps = level.hints.steps
            for (i in 1 until steps.size) {
                val prev = steps[i - 1]
                val curr = steps[i]
                if (prev.showValidStarts) {
                    assertTrue(
                        "Level ${level.id} step $i should not hide valid starts after step ${i - 1} showed them",
                        curr.showValidStarts
                    )
                }
                if (prev.showFirstEdge) {
                    assertTrue(
                        "Level ${level.id} step $i should not hide first edge after step ${i - 1} showed it",
                        curr.showFirstEdge
                    )
                }
            }
        }
    }

    @Test
    fun noEdgePassesThroughAnotherNode() {
        val epsilon = 0.001f

        LevelManager.levels.forEach { level ->
            val nodePositions = level.nodes.associate { it.id to it.position }

            level.edges.forEach { edge ->
                val p1 = nodePositions[edge.node1Id]!!
                val p2 = nodePositions[edge.node2Id]!!

                level.nodes.forEach { node ->
                    if (node.id == edge.node1Id || node.id == edge.node2Id) return@forEach

                    val p = node.position

                    // Check if point p lies on line segment p1-p2
                    // Using parametric form: p = p1 + t * (p2 - p1), where t in (0, 1)
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y

                    // Calculate parameter t for both x and y
                    val tx = if (kotlin.math.abs(dx) > epsilon) (p.x - p1.x) / dx else Float.NaN
                    val ty = if (kotlin.math.abs(dy) > epsilon) (p.y - p1.y) / dy else Float.NaN

                    val t = when {
                        tx.isNaN() && ty.isNaN() -> Float.NaN // Degenerate edge (same point)
                        tx.isNaN() -> ty // Vertical line
                        ty.isNaN() -> tx // Horizontal line
                        kotlin.math.abs(tx - ty) < epsilon -> tx // Point is on line
                        else -> Float.NaN // Point not on line
                    }

                    if (!t.isNaN() && t > epsilon && t < 1 - epsilon) {
                        // Verify the point is actually on the line (not just collinear at wrong position)
                        val expectedX = p1.x + t * dx
                        val expectedY = p1.y + t * dy
                        val onLine = kotlin.math.abs(p.x - expectedX) < epsilon &&
                            kotlin.math.abs(p.y - expectedY) < epsilon

                        assertFalse(
                            "Level ${level.id} (${level.name}): Edge (${edge.node1Id}, ${edge.node2Id}) " +
                                "passes through node ${node.id} at position (${p.x}, ${p.y})",
                            onLine
                        )
                    }
                }
            }
        }
    }
}
