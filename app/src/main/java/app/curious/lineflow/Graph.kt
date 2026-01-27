package app.curious.lineflow

import androidx.compose.ui.geometry.Offset

data class Node(
    val id: Int,
    // Position in a normalized coordinate space (0.0 to 1.0)
    val position: Offset
)

data class Edge(
    val node1Id: Int,
    val node2Id: Int,
    var isVisited: Boolean = false
) {
    fun containsNode(nodeId: Int): Boolean = nodeId == node1Id || nodeId == node2Id
}

data class HintStep(
    val text: String,
    val showValidStarts: Boolean = false,
    val showFirstEdge: Boolean = false
)

data class LevelHints(
    val validStartNodeIds: List<Int>,
    val firstEdge: Pair<Int, Int>?,
    val steps: List<HintStep>
)

data class Level(
    val id: Int,
    val name: String,
    val nodes: List<Node>,
    val edges: List<Edge>,
    val hints: LevelHints
)

object LevelManager {
    val levels = listOf(
        // Level 1: The Triangle — 3 nodes, 3 edges
        // Degrees: [2,2,2] -> 0 odd -> Circuit
        Level(
            id = 1,
            name = "The Triangle",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.2f)),
                Node(1, Offset(0.15f, 0.8f)),
                Node(2, Offset(0.85f, 0.8f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 0)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "This is a circuit \u2014 start at any corner!"),
                    HintStep(text = "Try starting at the top and going left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 2: The Square — 4 nodes, 4 edges
        // Degrees: [2,2,2,2] -> 0 odd -> Circuit
        Level(
            id = 2,
            name = "The Square",
            nodes = listOf(
                Node(0, Offset(0.2f, 0.2f)),
                Node(1, Offset(0.8f, 0.2f)),
                Node(2, Offset(0.8f, 0.8f)),
                Node(3, Offset(0.2f, 0.8f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 0)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A simple circuit. Any corner works!"),
                    HintStep(text = "Start top-left and trace clockwise.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 3: The Star — 5 nodes, 5 edges (pentagram)
        // Degrees: [2,2,2,2,2] -> 0 odd -> Circuit
        Level(
            id = 3,
            name = "The Star",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.08f)),
                Node(1, Offset(0.88f, 0.38f)),
                Node(2, Offset(0.73f, 0.88f)),
                Node(3, Offset(0.27f, 0.88f)),
                Node(4, Offset(0.12f, 0.38f))
            ),
            edges = listOf(
                Edge(0, 2), Edge(2, 4), Edge(4, 1), Edge(1, 3), Edge(3, 0)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4),
                firstEdge = Pair(0, 2),
                steps = listOf(
                    HintStep(text = "Draw the star without lifting. Any point works!"),
                    HintStep(text = "Start at the top and skip to the bottom-right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 4: The Bow Tie — 5 nodes, 6 edges
        // Degrees: 0=2, 1=2, 2=4, 3=2, 4=2 -> 0 odd -> Circuit
        Level(
            id = 4,
            name = "The Bow Tie",
            nodes = listOf(
                Node(0, Offset(0.15f, 0.2f)),
                Node(1, Offset(0.15f, 0.8f)),
                Node(2, Offset(0.5f, 0.5f)),
                Node(3, Offset(0.85f, 0.2f)),
                Node(4, Offset(0.85f, 0.8f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(0, 2), Edge(1, 2),
                Edge(2, 3), Edge(2, 4), Edge(3, 4)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "Two triangles share a center. Start anywhere!"),
                    HintStep(text = "Start top-left and go down first.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 5: The Grid — 6 nodes, 7 edges (2x3 grid)
        // Degrees: 0=2, 1=3, 2=2, 3=2, 4=3, 5=2 -> 2 odd (1,4) -> Path
        Level(
            id = 5,
            name = "The Grid",
            nodes = listOf(
                Node(0, Offset(0.2f, 0.35f)),
                Node(1, Offset(0.5f, 0.35f)),
                Node(2, Offset(0.8f, 0.35f)),
                Node(3, Offset(0.2f, 0.65f)),
                Node(4, Offset(0.5f, 0.65f)),
                Node(5, Offset(0.8f, 0.65f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2),
                Edge(3, 4), Edge(4, 5),
                Edge(0, 3), Edge(1, 4), Edge(2, 5)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(1, 4),
                firstEdge = Pair(1, 0),
                steps = listOf(
                    HintStep(text = "Only two nodes let you finish. Find the center ones."),
                    HintStep(text = "Start from the top-center node and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 6: The Envelope — 5 nodes, 8 edges
        // Degrees: 0=3, 1=3, 2=4, 3=4, 4=2 -> 2 odd (0,1) -> Path
        Level(
            id = 6,
            name = "The Envelope",
            nodes = listOf(
                Node(0, Offset(0.2f, 0.8f)),
                Node(1, Offset(0.8f, 0.8f)),
                Node(2, Offset(0.8f, 0.4f)),
                Node(3, Offset(0.2f, 0.4f)),
                Node(4, Offset(0.5f, 0.1f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 0),
                Edge(0, 2), Edge(1, 3),
                Edge(3, 4), Edge(2, 4)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "Start from one of the bottom corners."),
                    HintStep(text = "Begin at the bottom-left and trace right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 7: The Arrow — 5 nodes, 7 edges (planar fan)
        // Degrees: 0=2, 1=4, 2=3, 3=2, 4=3 -> 2 odd (2,4) -> Path
        Level(
            id = 7,
            name = "The Arrow",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.1f)),
                Node(1, Offset(0.15f, 0.45f)),
                Node(2, Offset(0.85f, 0.45f)),
                Node(3, Offset(0.35f, 0.85f)),
                Node(4, Offset(0.65f, 0.85f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(0, 2), Edge(1, 2),
                Edge(1, 3), Edge(1, 4), Edge(2, 4), Edge(3, 4)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 4),
                firstEdge = Pair(2, 0),
                steps = listOf(
                    HintStep(text = "Start from the right side — two nodes have odd connections."),
                    HintStep(text = "Begin at the upper-right and go to the top.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 8: The Hexagon — 6 nodes, 9 edges (hexagon + inner triangle)
        // Degrees: 0=4, 1=2, 2=4, 3=2, 4=4, 5=2 -> 0 odd -> Circuit
        Level(
            id = 8,
            name = "The Hexagon",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.1f)),
                Node(1, Offset(0.85f, 0.3f)),
                Node(2, Offset(0.85f, 0.7f)),
                Node(3, Offset(0.5f, 0.9f)),
                Node(4, Offset(0.15f, 0.7f)),
                Node(5, Offset(0.15f, 0.3f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(3, 4), Edge(4, 5), Edge(5, 0),
                Edge(0, 2), Edge(2, 4), Edge(4, 0)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "The inner triangle connects alternate corners."),
                    HintStep(text = "Start at the top and go clockwise.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 9: The Diamond — 6 nodes, 9 edges (planar: hexagon + 3 non-crossing diags)
        // Diags: (0,2), (0,3), (3,5) — all same-side, no crossing
        // Degrees: 0=4, 1=2, 2=3, 3=4, 4=2, 5=3 -> 2 odd (2,5) -> Path
        Level(
            id = 9,
            name = "The Diamond",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.08f)),
                Node(1, Offset(0.87f, 0.35f)),
                Node(2, Offset(0.87f, 0.72f)),
                Node(3, Offset(0.5f, 0.92f)),
                Node(4, Offset(0.13f, 0.72f)),
                Node(5, Offset(0.13f, 0.35f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(3, 4), Edge(4, 5), Edge(5, 0),
                Edge(0, 2), Edge(0, 3), Edge(3, 5)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 5),
                firstEdge = Pair(2, 1),
                steps = listOf(
                    HintStep(text = "Two nodes have odd connections — find the right side."),
                    HintStep(text = "Start at the bottom-right and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 10: The Hourglass — 6 nodes, 11 edges (two triangles with connections)
        // Degrees: 0=3, 1=4, 2=4, 3=4, 4=4, 5=3 -> 2 odd (0,5) -> Path
        Level(
            id = 10,
            name = "The Hourglass",
            nodes = listOf(
                Node(0, Offset(0.2f, 0.15f)),   // top-left
                Node(1, Offset(0.8f, 0.15f)),   // top-right
                Node(2, Offset(0.5f, 0.38f)),   // upper-center
                Node(3, Offset(0.5f, 0.62f)),   // lower-center
                Node(4, Offset(0.2f, 0.85f)),   // bottom-left
                Node(5, Offset(0.8f, 0.85f))    // bottom-right
            ),
            edges = listOf(
                // Top triangle
                Edge(0, 1), Edge(0, 2), Edge(1, 2),
                // Bottom triangle
                Edge(3, 4), Edge(3, 5), Edge(4, 5),
                // Center connection
                Edge(2, 3),
                // Side verticals
                Edge(0, 4), Edge(1, 5),
                // Cross diagonals (non-crossing)
                Edge(2, 4), Edge(1, 3)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 5),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "Two triangles connected at the center."),
                    HintStep(text = "Start from top-left or bottom-right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 11: The Shield — 7 nodes, 11 edges (house with internal bracing)
        // Edges: 0-1,0-2,1-2,1-3,2-4,3-4,3-5,4-6,5-6,3-6,4-5
        // Degrees: 0=2,1=3,2=3,3=4,4=4,5=3,6=3 -> 4 odd (1,2,5,6)
        // FIX: Remove one edge to get 2 odd. Remove 4-5:
        // Degrees: 0=2,1=3,2=3,3=4,4=3,5=2,6=3 -> 4 odd still
        // Try different structure: Simple house with one diagonal
        // Nodes: roof(0), upper-left(1), upper-right(2), lower-left(3), lower-right(4), center(5), bottom(6)
        // Edges: 0-1,0-2,1-2,1-3,2-4,3-4,1-5,2-5,5-3,5-4,3-6,4-6
        // Degrees: 0=2,1=4,2=4,3=4,4=4,5=4,6=2 -> 0 odd = Circuit!
        Level(
            id = 11,
            name = "The Shield",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.08f)),   // top point (roof)
                Node(1, Offset(0.2f, 0.3f)),    // upper-left
                Node(2, Offset(0.8f, 0.3f)),    // upper-right
                Node(3, Offset(0.2f, 0.65f)),   // lower-left
                Node(4, Offset(0.8f, 0.65f)),   // lower-right
                Node(5, Offset(0.5f, 0.47f)),   // center
                Node(6, Offset(0.5f, 0.92f))    // bottom point
            ),
            edges = listOf(
                // Roof
                Edge(0, 1), Edge(0, 2), Edge(1, 2),
                // Sides
                Edge(1, 3), Edge(2, 4),
                // Center cross
                Edge(1, 5), Edge(2, 5), Edge(5, 3), Edge(5, 4),
                // Bottom
                Edge(3, 4), Edge(3, 6), Edge(4, 6)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A shield with internal bracing."),
                    HintStep(text = "This is a circuit - any node works!", showValidStarts = true),
                    HintStep(text = "Start at the top and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 12: The Tower — 8 nodes, 13 edges (tall structure with cross-bracing)
        // Edges: 0-1,0-2,1-2,3-4,5-6,1-3,3-5,2-4,4-6,1-4,3-6,5-7,6-7
        // Degrees: 0=2,1=4,2=3,3=4,4=4,5=3,6=4,7=2 -> 2 odd (2,5) -> Path
        Level(
            id = 12,
            name = "The Tower",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.05f)),   // spire
                Node(1, Offset(0.25f, 0.22f)),  // roof-left
                Node(2, Offset(0.75f, 0.22f)),  // roof-right
                Node(3, Offset(0.25f, 0.48f)),  // mid-left
                Node(4, Offset(0.75f, 0.48f)),  // mid-right
                Node(5, Offset(0.25f, 0.74f)),  // lower-left
                Node(6, Offset(0.75f, 0.74f)),  // lower-right
                Node(7, Offset(0.5f, 0.95f))    // base
            ),
            edges = listOf(
                // Spire
                Edge(0, 1), Edge(0, 2),
                // Horizontal levels
                Edge(1, 2), Edge(3, 4), Edge(5, 6),
                // Left rail
                Edge(1, 3), Edge(3, 5),
                // Right rail
                Edge(2, 4), Edge(4, 6),
                // Cross braces (all go same direction - no crossing)
                Edge(1, 4), Edge(3, 6),
                // Base
                Edge(5, 7), Edge(6, 7)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 5),
                firstEdge = Pair(2, 0),
                steps = listOf(
                    HintStep(text = "A tower with cross-bracing!"),
                    HintStep(text = "Start from roof-right or lower-left.", showValidStarts = true),
                    HintStep(text = "Begin at roof-right and go to the spire.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 13: The Ladder — 8 nodes, 13 edges (ladder with diagonal braces)
        // Proven structure: 2 rails + rungs + NE diagonals
        // Edges: 0-1,1-2,2-3,4-5,5-6,6-7,0-4,1-5,2-6,3-7,1-4,2-5,3-6
        // Degrees: 0=2,1=4,2=4,3=3,4=3,5=4,6=4,7=2 -> 2 odd (3,4) -> Path
        Level(
            id = 13,
            name = "The Ladder",
            nodes = listOf(
                // Left column (top to bottom)
                Node(0, Offset(0.25f, 0.1f)),
                Node(1, Offset(0.25f, 0.4f)),
                Node(2, Offset(0.25f, 0.7f)),
                Node(3, Offset(0.25f, 0.9f)),
                // Right column (top to bottom)
                Node(4, Offset(0.75f, 0.1f)),
                Node(5, Offset(0.75f, 0.4f)),
                Node(6, Offset(0.75f, 0.7f)),
                Node(7, Offset(0.75f, 0.9f))
            ),
            edges = listOf(
                // Left rail
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                // Right rail
                Edge(4, 5), Edge(5, 6), Edge(6, 7),
                // Rungs (horizontal connections)
                Edge(0, 4), Edge(1, 5), Edge(2, 6), Edge(3, 7),
                // NE diagonal braces (no crossing)
                Edge(1, 4), Edge(2, 5), Edge(3, 6)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(3, 4),
                firstEdge = Pair(3, 2),
                steps = listOf(
                    HintStep(text = "A ladder with diagonal braces!"),
                    HintStep(text = "Start from bottom-left or top-right.", showValidStarts = true),
                    HintStep(text = "Begin at bottom-left and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 14: The Windmill — 9 nodes, 14 edges (center with 4 blades)
        // Center(0) + 4 cardinals(1-4) + 4 corners(5-8)
        // Edges: spokes(0-1,0-2,0-3,0-4), outer ring(1-5,5-2,2-6,6-3,3-7,7-4,4-8,8-1), diagonals(0-5,0-7)
        // Degrees: 0=6,1=3,2=3,3=3,4=3,5=3,6=2,7=3,8=2 -> 6 odd...needs adjustment
        // Better: Remove some edges. Use: spokes + partial ring + 2 diagonals
        // Edges: 0-1,0-2,0-3,0-4,1-5,5-2,2-6,6-3,3-7,7-4,4-8,8-1,1-2,3-4
        // Degrees: 0=4,1=4,2=4,3=4,4=4,5=2,6=2,7=2,8=2 -> 0 odd = Circuit!
        Level(
            id = 14,
            name = "The Windmill",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.5f)),    // center
                Node(1, Offset(0.5f, 0.1f)),    // top
                Node(2, Offset(0.9f, 0.5f)),    // right
                Node(3, Offset(0.5f, 0.9f)),    // bottom
                Node(4, Offset(0.1f, 0.5f)),    // left
                Node(5, Offset(0.78f, 0.22f)),  // top-right
                Node(6, Offset(0.78f, 0.78f)),  // bottom-right
                Node(7, Offset(0.22f, 0.78f)),  // bottom-left
                Node(8, Offset(0.22f, 0.22f))   // top-left
            ),
            edges = listOf(
                // Center spokes
                Edge(0, 1), Edge(0, 2), Edge(0, 3), Edge(0, 4),
                // Outer ring segments
                Edge(1, 5), Edge(5, 2), Edge(2, 6), Edge(6, 3),
                Edge(3, 7), Edge(7, 4), Edge(4, 8), Edge(8, 1),
                // Cross connections
                Edge(1, 2), Edge(3, 4)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A windmill with spinning blades!"),
                    HintStep(text = "This is a circuit - any node works!", showValidStarts = true),
                    HintStep(text = "Start at center and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 15: The Grid — 9 nodes, 16 edges (3x3 grid with SW diagonals)
        // Proven structure with verified degrees
        // Edges: 0-1,1-2,3-4,4-5,6-7,7-8,0-3,3-6,1-4,4-7,2-5,5-8,1-3,2-4,4-6,5-7
        // Degrees: 0=2,1=4,2=3,3=4,4=6,5=4,6=3,7=4,8=2 -> 2 odd (2,6) -> Path
        Level(
            id = 15,
            name = "The Grid",
            nodes = listOf(
                // Top row
                Node(0, Offset(0.15f, 0.15f)),
                Node(1, Offset(0.5f, 0.15f)),
                Node(2, Offset(0.85f, 0.15f)),
                // Middle row
                Node(3, Offset(0.15f, 0.5f)),
                Node(4, Offset(0.5f, 0.5f)),
                Node(5, Offset(0.85f, 0.5f)),
                // Bottom row
                Node(6, Offset(0.15f, 0.85f)),
                Node(7, Offset(0.5f, 0.85f)),
                Node(8, Offset(0.85f, 0.85f))
            ),
            edges = listOf(
                // Rows
                Edge(0, 1), Edge(1, 2),
                Edge(3, 4), Edge(4, 5),
                Edge(6, 7), Edge(7, 8),
                // Columns
                Edge(0, 3), Edge(3, 6),
                Edge(1, 4), Edge(4, 7),
                Edge(2, 5), Edge(5, 8),
                // SW diagonals (all same direction - no crossing)
                Edge(1, 3), Edge(2, 4), Edge(4, 6), Edge(5, 7)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 6),
                firstEdge = Pair(2, 1),
                steps = listOf(
                    HintStep(text = "A complex grid with diagonal shortcuts!"),
                    HintStep(text = "Start from top-right or bottom-left.", showValidStarts = true),
                    HintStep(text = "Begin at top-right and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 16: The Gem — 8 nodes, 13 edges (diamond with triangular facets)
        // Degrees: 0=2, 1=4, 2=5, 3=2, 4=2, 5=5, 6=4, 7=2 -> 2 odd (2,5) -> Path
        Level(
            id = 16,
            name = "The Gem",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.1f)),    // Top
                Node(1, Offset(0.3f, 0.3f)),    // Upper-left
                Node(2, Offset(0.7f, 0.3f)),    // Upper-right
                Node(3, Offset(0.15f, 0.5f)),   // Left
                Node(4, Offset(0.85f, 0.5f)),   // Right
                Node(5, Offset(0.3f, 0.7f)),    // Lower-left
                Node(6, Offset(0.7f, 0.7f)),    // Lower-right
                Node(7, Offset(0.5f, 0.9f))     // Bottom
            ),
            edges = listOf(
                // Top V and horizontal
                Edge(0, 1), Edge(0, 2), Edge(1, 2),
                // Left triangle
                Edge(1, 3), Edge(3, 5), Edge(1, 5),
                // Right triangle
                Edge(2, 4), Edge(4, 6), Edge(2, 6),
                // Bottom horizontal and V
                Edge(5, 6), Edge(5, 7), Edge(6, 7),
                // Cross diagonal
                Edge(2, 5)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 5),
                firstEdge = Pair(2, 0),
                steps = listOf(
                    HintStep(text = "A gem with triangular facets!"),
                    HintStep(text = "Two nodes have odd connections — upper-right and lower-left.", showValidStarts = true),
                    HintStep(text = "Start at upper-right and go to the top.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 17: The Steps — 10 nodes, 17 edges (staircase pattern, no crossings)
        // Two columns connected by horizontal rungs, with NE diagonals
        Level(
            id = 17,
            name = "The Steps",
            nodes = listOf(
                // Left column (top to bottom)
                Node(0, Offset(0.25f, 0.1f)),
                Node(1, Offset(0.25f, 0.32f)),
                Node(2, Offset(0.25f, 0.54f)),
                Node(3, Offset(0.25f, 0.76f)),
                Node(4, Offset(0.25f, 0.9f)),
                // Right column (top to bottom)
                Node(5, Offset(0.75f, 0.1f)),
                Node(6, Offset(0.75f, 0.32f)),
                Node(7, Offset(0.75f, 0.54f)),
                Node(8, Offset(0.75f, 0.76f)),
                Node(9, Offset(0.75f, 0.9f))
            ),
            edges = listOf(
                // Left rail
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                // Right rail
                Edge(5, 6), Edge(6, 7), Edge(7, 8), Edge(8, 9),
                // Rungs
                Edge(0, 5), Edge(1, 6), Edge(2, 7), Edge(3, 8), Edge(4, 9),
                // NE diagonals (all same direction, no crossing)
                Edge(1, 5), Edge(2, 6), Edge(3, 7), Edge(4, 8)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(4, 5),
                firstEdge = Pair(4, 3),
                steps = listOf(
                    HintStep(text = "A tall ladder with diagonal braces!"),
                    HintStep(text = "Start from bottom-left or top-right.", showValidStarts = true),
                    HintStep(text = "Start bottom-left and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 18: The Claw — 9 nodes, 15 edges (planar: 3x3 grid + 3 NE diags)
        // NE diags: (3,1),(6,4),(7,5)
        // Degrees: 0=2, 1=4, 2=2, 3=4, 4=5, 5=4, 6=3, 7=4, 8=2 -> 2 odd (4,6) -> Path
        Level(
            id = 18,
            name = "The Claw",
            nodes = listOf(
                Node(0, Offset(0.15f, 0.15f)),
                Node(1, Offset(0.5f, 0.15f)),
                Node(2, Offset(0.85f, 0.15f)),
                Node(3, Offset(0.15f, 0.5f)),
                Node(4, Offset(0.5f, 0.5f)),
                Node(5, Offset(0.85f, 0.5f)),
                Node(6, Offset(0.15f, 0.85f)),
                Node(7, Offset(0.5f, 0.85f)),
                Node(8, Offset(0.85f, 0.85f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2),
                Edge(3, 4), Edge(4, 5),
                Edge(6, 7), Edge(7, 8),
                Edge(0, 3), Edge(3, 6),
                Edge(1, 4), Edge(4, 7),
                Edge(2, 5), Edge(5, 8),
                Edge(3, 1), Edge(6, 4), Edge(7, 5)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(4, 6),
                firstEdge = Pair(4, 3),
                steps = listOf(
                    HintStep(text = "A grid with sweeping diagonal shortcuts."),
                    HintStep(text = "Two nodes have odd degree — find the center and corner.", showValidStarts = true),
                    HintStep(text = "Start at the center and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 19: The Vortex — 10 nodes, 16 edges (planar: 2x5 grid + 3 NE diags)
        // Top row: 0-4, Bottom row: 5-9. NE diags: (5,1),(6,2),(7,3)
        // Degrees: 0=2, 1=4, 2=4, 3=4, 4=2, 5=3, 6=4, 7=4, 8=3, 9=2 -> 2 odd (5,8) -> Path
        Level(
            id = 19,
            name = "The Vortex",
            nodes = listOf(
                Node(0, Offset(0.1f, 0.3f)),
                Node(1, Offset(0.32f, 0.3f)),
                Node(2, Offset(0.54f, 0.3f)),
                Node(3, Offset(0.76f, 0.3f)),
                Node(4, Offset(0.9f, 0.3f)),
                Node(5, Offset(0.1f, 0.7f)),
                Node(6, Offset(0.32f, 0.7f)),
                Node(7, Offset(0.54f, 0.7f)),
                Node(8, Offset(0.76f, 0.7f)),
                Node(9, Offset(0.9f, 0.7f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(5, 6), Edge(6, 7), Edge(7, 8), Edge(8, 9),
                Edge(0, 5), Edge(1, 6), Edge(2, 7), Edge(3, 8), Edge(4, 9),
                Edge(5, 1), Edge(6, 2), Edge(7, 3)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(5, 8),
                firstEdge = Pair(5, 0),
                steps = listOf(
                    HintStep(text = "A wide grid with sweeping diagonal shortcuts."),
                    HintStep(text = "Two nodes on the bottom row have odd degree.", showValidStarts = true),
                    HintStep(text = "Start at the bottom-left and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 20: The Cathedral — 10 nodes, 17 edges (planar: ladder + side wings)
        // Replaced crossing diags (2,5)×(3,4) with wing extensions (8,2),(9,3)
        // Degrees: 0=2, 1=2, 2=4, 3=4, 4=4, 5=4, 6=4, 7=4, 8=3, 9=3 -> 2 odd (8,9) -> Path
        Level(
            id = 20,
            name = "The Cathedral",
            nodes = listOf(
                Node(0, Offset(0.5f, 0.05f)),
                Node(1, Offset(0.5f, 0.92f)),
                Node(2, Offset(0.25f, 0.25f)),
                Node(3, Offset(0.75f, 0.25f)),
                Node(4, Offset(0.25f, 0.55f)),
                Node(5, Offset(0.75f, 0.55f)),
                Node(6, Offset(0.25f, 0.78f)),
                Node(7, Offset(0.75f, 0.78f)),
                Node(8, Offset(0.1f, 0.55f)),
                Node(9, Offset(0.9f, 0.55f))
            ),
            edges = listOf(
                Edge(0, 2), Edge(0, 3), Edge(2, 3),
                Edge(2, 4), Edge(3, 5), Edge(4, 5),
                Edge(4, 6), Edge(5, 7), Edge(6, 7),
                Edge(6, 1), Edge(7, 1),
                Edge(4, 8), Edge(8, 6),
                Edge(5, 9), Edge(9, 7),
                Edge(8, 2), Edge(9, 3)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(8, 9),
                firstEdge = Pair(8, 4),
                steps = listOf(
                    HintStep(text = "Symmetric structure with side wings."),
                    HintStep(text = "The two wing nodes have odd degree.", showValidStarts = true),
                    HintStep(text = "Start at the left wing and go to center-left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 21: The Serpent — 12 nodes, 21 edges (3x4 grid with NE diagonals)
        // Clean grid pattern with all NE diagonals, no crossings
        Level(
            id = 21,
            name = "The Serpent",
            nodes = listOf(
                // Top row
                Node(0, Offset(0.1f, 0.2f)),
                Node(1, Offset(0.37f, 0.2f)),
                Node(2, Offset(0.63f, 0.2f)),
                Node(3, Offset(0.9f, 0.2f)),
                // Middle row
                Node(4, Offset(0.1f, 0.5f)),
                Node(5, Offset(0.37f, 0.5f)),
                Node(6, Offset(0.63f, 0.5f)),
                Node(7, Offset(0.9f, 0.5f)),
                // Bottom row
                Node(8, Offset(0.1f, 0.8f)),
                Node(9, Offset(0.37f, 0.8f)),
                Node(10, Offset(0.63f, 0.8f)),
                Node(11, Offset(0.9f, 0.8f))
            ),
            edges = listOf(
                // Rows
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(4, 5), Edge(5, 6), Edge(6, 7),
                Edge(8, 9), Edge(9, 10), Edge(10, 11),
                // Columns
                Edge(0, 4), Edge(4, 8),
                Edge(1, 5), Edge(5, 9),
                Edge(2, 6), Edge(6, 10),
                Edge(3, 7), Edge(7, 11),
                // NE diagonals (all same direction, no crossing)
                Edge(4, 1), Edge(5, 2), Edge(6, 3),
                Edge(8, 5), Edge(9, 6), Edge(10, 7)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(3, 8),
                firstEdge = Pair(3, 2),
                steps = listOf(
                    HintStep(text = "A serpentine path through a grid!"),
                    HintStep(text = "Start from top-right or bottom-left.", showValidStarts = true),
                    HintStep(text = "Start top-right and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 22: The Kraken — 12 nodes, 19 edges (planar: 3x4 grid + 2 NE diags)
        // Grid: rows 0-3 (top), 4-7 (mid), 8-11 (bot). NE diags: (4,1),(10,7)
        // Degrees: 0=2, 1=4, 2=3, 3=2, 4=4, 5=4, 6=4, 7=4, 8=2, 9=3, 10=4, 11=2 -> 2 odd (2,9) -> Path
        Level(
            id = 22,
            name = "The Kraken",
            nodes = listOf(
                Node(0, Offset(0.1f, 0.15f)),
                Node(1, Offset(0.37f, 0.15f)),
                Node(2, Offset(0.63f, 0.15f)),
                Node(3, Offset(0.9f, 0.15f)),
                Node(4, Offset(0.1f, 0.5f)),
                Node(5, Offset(0.37f, 0.5f)),
                Node(6, Offset(0.63f, 0.5f)),
                Node(7, Offset(0.9f, 0.5f)),
                Node(8, Offset(0.1f, 0.85f)),
                Node(9, Offset(0.37f, 0.85f)),
                Node(10, Offset(0.63f, 0.85f)),
                Node(11, Offset(0.9f, 0.85f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(4, 5), Edge(5, 6), Edge(6, 7),
                Edge(8, 9), Edge(9, 10), Edge(10, 11),
                Edge(0, 4), Edge(4, 8), Edge(1, 5), Edge(5, 9),
                Edge(2, 6), Edge(6, 10), Edge(3, 7), Edge(7, 11),
                Edge(4, 1), Edge(10, 7)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(2, 9),
                firstEdge = Pair(2, 1),
                steps = listOf(
                    HintStep(text = "A wide grid with diagonal shortcuts."),
                    HintStep(text = "Two nodes have odd degree — find them on opposite sides.", showValidStarts = true),
                    HintStep(text = "Start at the top-right area and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 23: The Phoenix — 13 nodes, 22 edges (planar: 3x4 grid + bottom node + NE diags)
        // Grid: rows 0-3 (top), 4-7 (mid), 8-11 (bot). Node 12 at bottom center.
        // NE diags: (4,1),(5,2),(8,5). Node 12: (12,9),(12,10).
        // Degrees: 0=2, 1=4, 2=4, 3=2, 4=4, 5=6, 6=4, 7=3, 8=3, 9=4, 10=4, 11=2, 12=2
        // -> 2 odd (7,8) -> Path
        Level(
            id = 23,
            name = "The Phoenix",
            nodes = listOf(
                Node(0, Offset(0.1f, 0.1f)),
                Node(1, Offset(0.37f, 0.1f)),
                Node(2, Offset(0.63f, 0.1f)),
                Node(3, Offset(0.9f, 0.1f)),
                Node(4, Offset(0.1f, 0.4f)),
                Node(5, Offset(0.37f, 0.4f)),
                Node(6, Offset(0.63f, 0.4f)),
                Node(7, Offset(0.9f, 0.4f)),
                Node(8, Offset(0.1f, 0.7f)),
                Node(9, Offset(0.37f, 0.7f)),
                Node(10, Offset(0.63f, 0.7f)),
                Node(11, Offset(0.9f, 0.7f)),
                Node(12, Offset(0.5f, 0.92f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(4, 5), Edge(5, 6), Edge(6, 7),
                Edge(8, 9), Edge(9, 10), Edge(10, 11),
                Edge(0, 4), Edge(4, 8), Edge(1, 5), Edge(5, 9),
                Edge(2, 6), Edge(6, 10), Edge(3, 7), Edge(7, 11),
                Edge(4, 1), Edge(5, 2), Edge(8, 5),
                Edge(12, 9), Edge(12, 10)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(7, 8),
                firstEdge = Pair(7, 6),
                steps = listOf(
                    HintStep(text = "A large grid with diagonal shortcuts and a tail."),
                    HintStep(text = "Two nodes on the right and left have odd degree.", showValidStarts = true),
                    HintStep(text = "Start at the mid-right and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 24: The Leviathan — 14 nodes, 25 edges (planar: 3x4 grid + 2 bottom nodes + NE diags)
        // Grid: rows 0-3 (top), 4-7 (mid), 8-11 (bot). Extra: 12=(0.25,0.92), 13=(0.75,0.92)
        // NE diags: (4,1),(5,2),(8,5),(9,6),(10,7). Bottom: (8,12),(12,13),(13,11)
        // Degrees: 0=2, 1=4, 2=4, 3=2, 4=4, 5=6, 6=5, 7=4, 8=4, 9=4, 10=4, 11=3, 12=2, 13=2
        // -> 2 odd (6,11) -> Path
        Level(
            id = 24,
            name = "The Leviathan",
            nodes = listOf(
                Node(0, Offset(0.1f, 0.1f)),
                Node(1, Offset(0.37f, 0.1f)),
                Node(2, Offset(0.63f, 0.1f)),
                Node(3, Offset(0.9f, 0.1f)),
                Node(4, Offset(0.1f, 0.38f)),
                Node(5, Offset(0.37f, 0.38f)),
                Node(6, Offset(0.63f, 0.38f)),
                Node(7, Offset(0.9f, 0.38f)),
                Node(8, Offset(0.1f, 0.66f)),
                Node(9, Offset(0.37f, 0.66f)),
                Node(10, Offset(0.63f, 0.66f)),
                Node(11, Offset(0.9f, 0.66f)),
                Node(12, Offset(0.25f, 0.92f)),
                Node(13, Offset(0.75f, 0.92f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(4, 5), Edge(5, 6), Edge(6, 7),
                Edge(8, 9), Edge(9, 10), Edge(10, 11),
                Edge(0, 4), Edge(4, 8), Edge(1, 5), Edge(5, 9),
                Edge(2, 6), Edge(6, 10), Edge(3, 7), Edge(7, 11),
                Edge(4, 1), Edge(5, 2), Edge(8, 5), Edge(9, 6), Edge(10, 7),
                Edge(8, 12), Edge(12, 13), Edge(13, 11)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(6, 11),
                firstEdge = Pair(6, 5),
                steps = listOf(
                    HintStep(text = "The great beast stretches wide and deep."),
                    HintStep(text = "Two nodes on the right side have odd degree.", showValidStarts = true),
                    HintStep(text = "Start at the mid-right area and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 25: The Cosmos — 15 nodes, 30 edges (planar: 3x5 grid + 8 NE diags)
        // Grid: rows 0-4 (top), 5-9 (mid), 10-14 (bot).
        // NE diags: (5,1),(6,2),(7,3),(8,4),(10,6),(11,7),(12,8),(13,9)
        // Degrees: 0=2, 1=4, 2=4, 3=4, 4=3, 5=4, 6=6, 7=6, 8=6, 9=4, 10=3, 11=4, 12=4, 13=4, 14=2
        // -> 2 odd (4,10) -> Path
        Level(
            id = 25,
            name = "The Cosmos",
            nodes = listOf(
                Node(0, Offset(0.1f, 0.15f)),
                Node(1, Offset(0.32f, 0.15f)),
                Node(2, Offset(0.55f, 0.15f)),
                Node(3, Offset(0.78f, 0.15f)),
                Node(4, Offset(0.9f, 0.15f)),
                Node(5, Offset(0.1f, 0.5f)),
                Node(6, Offset(0.32f, 0.5f)),
                Node(7, Offset(0.55f, 0.5f)),
                Node(8, Offset(0.78f, 0.5f)),
                Node(9, Offset(0.9f, 0.5f)),
                Node(10, Offset(0.1f, 0.85f)),
                Node(11, Offset(0.32f, 0.85f)),
                Node(12, Offset(0.55f, 0.85f)),
                Node(13, Offset(0.78f, 0.85f)),
                Node(14, Offset(0.9f, 0.85f))
            ),
            edges = listOf(
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(5, 6), Edge(6, 7), Edge(7, 8), Edge(8, 9),
                Edge(10, 11), Edge(11, 12), Edge(12, 13), Edge(13, 14),
                Edge(0, 5), Edge(5, 10), Edge(1, 6), Edge(6, 11),
                Edge(2, 7), Edge(7, 12), Edge(3, 8), Edge(8, 13),
                Edge(4, 9), Edge(9, 14),
                Edge(5, 1), Edge(6, 2), Edge(7, 3), Edge(8, 4),
                Edge(10, 6), Edge(11, 7), Edge(12, 8), Edge(13, 9)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(4, 10),
                firstEdge = Pair(4, 3),
                steps = listOf(
                    HintStep(text = "The universe unfolds across a vast grid."),
                    HintStep(text = "Two corner nodes have odd degree.", showValidStarts = true),
                    HintStep(text = "Start at the top-right corner and go left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        )
    )

    fun getLevel(id: Int): Level? = levels.firstOrNull { it.id == id }

    fun getNextLevel(currentLevelId: Int): Level? {
        val currentIndex = levels.indexOfFirst { it.id == currentLevelId }
        return levels.getOrNull(currentIndex + 1)
    }
}
