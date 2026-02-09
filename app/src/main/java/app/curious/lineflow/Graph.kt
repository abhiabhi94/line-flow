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
        ),

        // Level 26: The Diamond — 9 nodes, 16 edges
        // Diamond-shaped layout (rotated square orientation)
        // Nodes 3,5 have degree 3 (odd) -> Path
        Level(
            id = 26,
            name = "The Diamond",
            nodes = listOf(
                // Top point
                Node(0, Offset(0.5f, 0.1f)),
                // Second row (2 nodes)
                Node(1, Offset(0.3f, 0.3f)),
                Node(2, Offset(0.7f, 0.3f)),
                // Middle row (3 nodes) - widest
                Node(3, Offset(0.1f, 0.5f)),
                Node(4, Offset(0.5f, 0.5f)),
                Node(5, Offset(0.9f, 0.5f)),
                // Fourth row (2 nodes)
                Node(6, Offset(0.3f, 0.7f)),
                Node(7, Offset(0.7f, 0.7f)),
                // Bottom point
                Node(8, Offset(0.5f, 0.9f))
            ),
            edges = listOf(
                // Outer diamond outline (8 edges)
                Edge(0, 1), Edge(0, 2),
                Edge(1, 3), Edge(2, 5),
                Edge(3, 6), Edge(5, 7),
                Edge(6, 8), Edge(7, 8),
                // Horizontal connections (2 edges)
                Edge(3, 4), Edge(4, 5),
                // Internal diamond connections (6 edges)
                Edge(1, 4), Edge(2, 4),
                Edge(4, 6), Edge(4, 7),
                Edge(1, 2), Edge(6, 7)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(3, 5),
                firstEdge = Pair(3, 1),
                steps = listOf(
                    HintStep(text = "A diamond rotated 45 degrees from a square."),
                    HintStep(text = "The left and right corners have odd degree.", showValidStarts = true),
                    HintStep(text = "Start from the left corner.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 27: The Hexagon Star — 12 nodes, 24 edges
        // Six-pointed star with outer and inner hexagon - CIRCUIT (all even)
        // All nodes have degree 4 -> 0 odd -> Circuit
        Level(
            id = 27,
            name = "The Hexagon Star",
            nodes = listOf(
                // Outer hexagon (6 nodes)
                Node(0, Offset(0.5f, 0.08f)),     // top
                Node(1, Offset(0.85f, 0.29f)),   // top-right
                Node(2, Offset(0.85f, 0.71f)),   // bottom-right
                Node(3, Offset(0.5f, 0.92f)),    // bottom
                Node(4, Offset(0.15f, 0.71f)),   // bottom-left
                Node(5, Offset(0.15f, 0.29f)),   // top-left
                // Inner hexagon (6 nodes)
                Node(6, Offset(0.5f, 0.25f)),    // inner top
                Node(7, Offset(0.68f, 0.35f)),   // inner top-right
                Node(8, Offset(0.68f, 0.65f)),   // inner bottom-right
                Node(9, Offset(0.5f, 0.75f)),    // inner bottom
                Node(10, Offset(0.32f, 0.65f)),  // inner bottom-left
                Node(11, Offset(0.32f, 0.35f))   // inner top-left
            ),
            edges = listOf(
                // Outer hexagon
                Edge(0, 1), Edge(1, 2), Edge(2, 3),
                Edge(3, 4), Edge(4, 5), Edge(5, 0),
                // Inner hexagon
                Edge(6, 7), Edge(7, 8), Edge(8, 9),
                Edge(9, 10), Edge(10, 11), Edge(11, 6),
                // Radial spokes (outer to inner)
                Edge(0, 6), Edge(1, 7), Edge(2, 8),
                Edge(3, 9), Edge(4, 10), Edge(5, 11),
                // Star points (skip connections)
                Edge(0, 7), Edge(1, 8), Edge(2, 9),
                Edge(3, 10), Edge(4, 11), Edge(5, 6)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A circuit - you can start anywhere and return!"),
                    HintStep(text = "All nodes have even degree. Pick any start.", showValidStarts = true),
                    HintStep(text = "Try starting from the top and going clockwise.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 28: The Pinwheel — 13 nodes, 24 edges
        // A pinwheel shape: center hub with 4 blades extending outward
        // Each blade has an inner and outer node, with diagonal connections
        // Degrees: 0=8, 1-4=4, 5-8=3, 9-12=3 -> 8 odd nodes initially
        // Adjusted for PATH: Center connects to 4 cardinals, cardinals connect to corners
        // Degrees: 0=4, 1=3, 2=3, 3=4, 4=4, 5=4, 6=4, 7=4, 8=4, 9=4, 10=4, 11=4, 12=4
        // Odd: 1,2 -> PATH
        Level(
            id = 28,
            name = "The Pinwheel",
            nodes = listOf(
                // Center
                Node(0, Offset(0.5f, 0.5f)),
                // Cardinal points
                Node(1, Offset(0.5f, 0.12f)),    // top
                Node(2, Offset(0.88f, 0.5f)),    // right
                Node(3, Offset(0.5f, 0.88f)),    // bottom
                Node(4, Offset(0.12f, 0.5f)),    // left
                // Inner corners
                Node(5, Offset(0.68f, 0.32f)),   // top-right inner
                Node(6, Offset(0.68f, 0.68f)),   // bottom-right inner
                Node(7, Offset(0.32f, 0.68f)),   // bottom-left inner
                Node(8, Offset(0.32f, 0.32f)),   // top-left inner
                // Outer corners
                Node(9, Offset(0.82f, 0.18f)),   // top-right outer
                Node(10, Offset(0.82f, 0.82f)),  // bottom-right outer
                Node(11, Offset(0.18f, 0.82f)),  // bottom-left outer
                Node(12, Offset(0.18f, 0.18f))   // top-left outer
            ),
            edges = listOf(
                // Center to cardinals
                Edge(0, 1), Edge(0, 2), Edge(0, 3), Edge(0, 4),
                // Center to inner corners
                Edge(0, 5), Edge(0, 6), Edge(0, 7), Edge(0, 8),
                // Cardinal to adjacent inner corners (clockwise blades)
                Edge(1, 5), Edge(2, 6), Edge(3, 7), Edge(4, 8),
                // Inner corners to outer corners
                Edge(5, 9), Edge(6, 10), Edge(7, 11), Edge(8, 12),
                // Outer ring segments
                Edge(9, 2), Edge(10, 3), Edge(11, 4), Edge(12, 1),
                // Inner to outer across (creating pinwheel spin)
                Edge(5, 2), Edge(6, 3), Edge(7, 4), Edge(8, 1)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A spinning pinwheel with four blades."),
                    HintStep(text = "All nodes have even degree - this is a circuit.", showValidStarts = true),
                    HintStep(text = "Start from the center and go up.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 29: The Pyramid — 10 nodes, 18 edges
        // Triangular grid forming a pyramid shape (4 rows: 1,2,3,4 nodes)
        // Different visual than rectangular grids - PATH
        // Row 0: node 0
        // Row 1: nodes 1,2
        // Row 2: nodes 3,4,5
        // Row 3: nodes 6,7,8,9
        // Vertical + diagonal edges create Eulerian path
        // Degrees: 0=2, 1=4, 2=4, 3=4, 4=6, 5=4, 6=2, 7=4, 8=4, 9=2 -> 0 odd = circuit
        Level(
            id = 29,
            name = "The Pyramid",
            nodes = listOf(
                // Row 0 (top - 1 node)
                Node(0, Offset(0.5f, 0.1f)),
                // Row 1 (2 nodes)
                Node(1, Offset(0.35f, 0.33f)),
                Node(2, Offset(0.65f, 0.33f)),
                // Row 2 (3 nodes)
                Node(3, Offset(0.2f, 0.56f)),
                Node(4, Offset(0.5f, 0.56f)),
                Node(5, Offset(0.8f, 0.56f)),
                // Row 3 (4 nodes)
                Node(6, Offset(0.08f, 0.85f)),
                Node(7, Offset(0.36f, 0.85f)),
                Node(8, Offset(0.64f, 0.85f)),
                Node(9, Offset(0.92f, 0.85f))
            ),
            edges = listOf(
                // Horizontal edges within rows
                Edge(1, 2),             // Row 1
                Edge(3, 4), Edge(4, 5), // Row 2
                Edge(6, 7), Edge(7, 8), Edge(8, 9), // Row 3
                // Left diagonal edges (NE direction going down)
                Edge(0, 1), Edge(1, 3), Edge(3, 6),
                Edge(1, 4), Edge(4, 7),
                // Right diagonal edges (NW direction going down)
                Edge(0, 2), Edge(2, 5), Edge(5, 9),
                Edge(2, 4), Edge(4, 8),
                // Additional cross diagonals for complexity
                Edge(3, 7), Edge(5, 8)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "A pyramid with interlocking triangular sections."),
                    HintStep(text = "All nodes have even degree - this is a circuit.", showValidStarts = true),
                    HintStep(text = "Start from the apex and work down.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 30: The Bastion — 16 nodes, 32 edges
        // Dual concentric octagons with radial spokes and star skip connections - CIRCUIT
        // All nodes have degree 4 (2 ring + 1 spoke + 1 skip) = 0 odd nodes = circuit
        Level(
            id = 30,
            name = "The Bastion",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f))   // inner top-left
            ),
            edges = listOf(
                // Outer octagon ring (8)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - outer to adjacent inner (8)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "Two concentric octagons connected by spokes and star points."),
                    HintStep(text = "All nodes have even degree - this is a circuit.", showValidStarts = true),
                    HintStep(text = "Start from any node - try the top.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 31: The Core — 17 nodes, 34 edges
        // The Bastion with a center hub connecting to inner top and inner bottom - PATH
        // Extends Level 30 by revealing the "core" of the structure
        // Odd nodes: 8, 12 (inner top and inner bottom)
        Level(
            id = 31,
            name = "The Core",
            nodes = listOf(
                // Outer octagon (8 nodes) - same as Bastion
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes) - same as Bastion
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - outer to adjacent inner (8)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center connections (2) - creates PATH with 2 odd nodes
                Edge(16, 8), Edge(16, 12)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(8, 12),
                firstEdge = Pair(8, 0),
                steps = listOf(
                    HintStep(text = "The Bastion's core revealed - a center hub connects two key points."),
                    HintStep(text = "The inner top and inner bottom have odd connections.", showValidStarts = true),
                    HintStep(text = "Start from the inner top and head to the outer ring.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 32: The Rampart — 16 nodes, 32 edges
        // Same structure as The Bastion but with OPPOSITE skip direction - CIRCUIT
        // Creates a mirror pattern that plays differently
        // All nodes have degree 4 = 0 odd nodes = circuit
        Level(
            id = 32,
            name = "The Rampart",
            nodes = listOf(
                // Outer octagon (8 nodes) - same as Bastion
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes) - same as Bastion
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f))   // inner top-left
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8 edges)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - COUNTER-CLOCKWISE (opposite of Bastion)
                Edge(0, 15), Edge(1, 8), Edge(2, 9), Edge(3, 10),
                Edge(4, 11), Edge(5, 12), Edge(6, 13), Edge(7, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = (0..15).toList(),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "The Rampart - the Bastion's mirror."),
                    HintStep(text = "All nodes have even degree - this is a circuit.", showValidStarts = true),
                    HintStep(text = "The skip connections go counter-clockwise.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 33: The Citadel — 17 nodes, 34 edges
        // Bastion structure with center hub connecting top and bottom inner nodes - PATH
        // Creates a vertical axis through the center
        // Odd nodes: 8, 12 (inner top and inner bottom)
        Level(
            id = 33,
            name = "The Citadel",
            nodes = listOf(
                // Outer octagon (8 nodes) - same as Bastion
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes) - same as Bastion
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8 edges)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center to top and bottom inner nodes only (2 edges) - creates PATH
                Edge(16, 8), Edge(16, 12)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(8, 12),
                firstEdge = Pair(8, 0),
                steps = listOf(
                    HintStep(text = "The Citadel - the Bastion with a central axis."),
                    HintStep(text = "Start from inner top or inner bottom.", showValidStarts = true),
                    HintStep(text = "The center connects only top and bottom.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 34: The Nexus — 17 nodes, 34 edges
        // Bastion structure with center hub connecting left and right inner nodes - PATH
        // Creates a horizontal axis through the center (different from Citadel's vertical)
        // Odd nodes: 10, 14 (inner right and inner left)
        Level(
            id = 34,
            name = "The Nexus",
            nodes = listOf(
                // Outer octagon (8 nodes) - same as Bastion
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes) - same as Bastion
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8 edges)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center to left and right inner nodes only (2 edges) - creates PATH
                Edge(16, 10), Edge(16, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(10, 14),
                firstEdge = Pair(10, 2),
                steps = listOf(
                    HintStep(text = "The Nexus - a horizontal core axis."),
                    HintStep(text = "Start from inner right or inner left.", showValidStarts = true),
                    HintStep(text = "The center creates a bridge between sides.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 35: The Apex — 17 nodes, 34 edges
        // Bastion structure with center connecting to diagonal inner nodes - PATH
        // Center connects to inner top-right (9) and inner bottom-left (13)
        // Creates a diagonal axis - different from Citadel (vertical) and Nexus (horizontal)
        // Odd nodes: 9, 13 (inner diagonal pair)
        Level(
            id = 35,
            name = "The Apex",
            nodes = listOf(
                // Outer octagon (8 nodes) - same as Bastion
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes) - same as Bastion
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8 edges)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center to diagonal inner nodes only (2 edges) - creates PATH
                Edge(16, 9), Edge(16, 13)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(9, 13),
                firstEdge = Pair(9, 1),
                steps = listOf(
                    HintStep(text = "The Apex - the final challenge with a diagonal axis."),
                    HintStep(text = "Start from inner top-right or inner bottom-left.", showValidStarts = true),
                    HintStep(text = "The center creates a diagonal bridge.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 36: The Fracture — 17 nodes, 34 edges
        // Mixed CW/CCW skip directions break rotational symmetry - PATH
        // Alternating skip pattern: even outer nodes skip CW, odd outer nodes skip CCW
        // Odd nodes: 8, 12 (inner top and inner bottom)
        Level(
            id = 36,
            name = "The Fracture",
            nodes = listOf(
                // Outer octagon (8 nodes) - same positions as levels 30-35
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner octagon ring (8 edges)
                Edge(8, 9), Edge(9, 10), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Mixed skip connections - alternating CW/CCW (8 edges)
                Edge(0, 9), Edge(1, 8),   // 0 CW, 1 CCW
                Edge(2, 11), Edge(3, 10), // 2 CW, 3 CCW
                Edge(4, 13), Edge(5, 12), // 4 CW, 5 CCW
                Edge(6, 15), Edge(7, 14), // 6 CW, 7 CCW
                // Center to top and bottom inner nodes (2 edges) - creates PATH
                Edge(16, 8), Edge(16, 12)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(8, 12),
                firstEdge = Pair(8, 0),
                steps = listOf(
                    HintStep(text = "The skip connections alternate directions — watch the pattern shift."),
                    HintStep(text = "Inner top and inner bottom have odd connections.", showValidStarts = true),
                    HintStep(text = "Start from the inner top and head outward.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 37: The Rift — 17 nodes, 34 edges
        // Broken inner ring with cross-connections creating shortcuts and bottlenecks - PATH
        // Inner ring breaks at 8-9 and 11-12, replaced with cross-edges 8-11 and 12-15
        // Odd nodes: 13, 15 (both in lower-left quadrant — asymmetric endpoints)
        Level(
            id = 37,
            name = "The Rift",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Modified inner connections (8 edges)
                // Ring segments kept: 9-10, 10-11, 12-13, 13-14, 14-15, 15-8
                // Ring segments removed: 8-9, 11-12
                // Cross-connections added: 8-11, 12-15
                Edge(9, 10), Edge(10, 11), Edge(12, 13), Edge(13, 14),
                Edge(14, 15), Edge(15, 8), Edge(8, 11), Edge(12, 15),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center to inner top-right and inner bottom-left (2 edges) - creates PATH
                Edge(16, 9), Edge(16, 13)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(13, 15),
                firstEdge = Pair(13, 12),
                steps = listOf(
                    HintStep(text = "The inner ring is broken — two cross-connections create shortcuts."),
                    HintStep(text = "Both start nodes are in the lower-left quadrant.", showValidStarts = true),
                    HintStep(text = "Start from inner bottom-left and go to inner bottom.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 38: The Crucible — 17 nodes, 34 edges
        // 4-degree center hub with 2 inner ring breaks - CIRCUIT
        // Inner ring breaks at 9-10 and 13-14, bridged only through center
        // Center must be visited exactly twice with correct edge pairing
        // All even degrees = 0 odd nodes = circuit
        Level(
            id = 38,
            name = "The Crucible",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Modified inner ring (6 edges)
                // Kept: 8-9, 10-11, 11-12, 12-13, 14-15, 15-8
                // Removed: 9-10, 13-14
                Edge(8, 9), Edge(10, 11), Edge(11, 12),
                Edge(12, 13), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center connections (4 edges) - bridges the inner ring gaps
                Edge(16, 9), Edge(16, 10), Edge(16, 13), Edge(16, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = (0..16).toList(),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "The center is a crossroads — plan your visits carefully."),
                    HintStep(text = "All nodes have even degree — this is a circuit, start anywhere.", showValidStarts = true),
                    HintStep(text = "The center must be visited exactly twice. Try starting at the top.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 39: The Labyrinth — 17 nodes, 34 edges
        // Broken inner ring + mixed CW/CCW skips + asymmetric degrees - PATH
        // Node 11 has degree 3 (bottleneck), node 13 has degree 6 (trap hub)
        // No rotational symmetry remains
        // Odd nodes: 9 (degree 5), 11 (degree 3)
        Level(
            id = 39,
            name = "The Labyrinth",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Modified inner connections (8 edges)
                // Ring segments kept: 9-10, 10-11, 12-13, 13-14, 14-15, 15-8
                // Ring segments removed: 8-9, 11-12
                // Cross-connections added: 8-11, 12-15
                Edge(9, 10), Edge(10, 11), Edge(12, 13), Edge(13, 14),
                Edge(14, 15), Edge(15, 8), Edge(8, 11), Edge(12, 15),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Mixed skip connections (8 edges)
                // CW kept: 0-9, 1-10, 3-12, 4-13, 5-14, 7-8
                // CCW replacements: 2-9 (was 2-11), 6-13 (was 6-15)
                Edge(0, 9), Edge(1, 10), Edge(2, 9), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 13), Edge(7, 8),
                // Center connections (2 edges) - creates PATH
                Edge(16, 9), Edge(16, 13)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(9, 11),
                firstEdge = Pair(9, 10),
                steps = listOf(
                    HintStep(text = "A labyrinth of asymmetric connections — node 11 is a critical bottleneck."),
                    HintStep(text = "Inner top-right and inner bottom-right are your endpoints.", showValidStarts = true),
                    HintStep(text = "Start from inner top-right and head to inner right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 40: The Singularity — 17 nodes, 34 edges
        // 6-degree center hub with 4 inner ring breaks creating island topology - PATH
        // Center must be visited 3 times with correct edge pairing each time
        // Inner ring 75% broken — 4 isolated node-pairs connected only through center
        // Odd nodes: 11 (degree 3), 15 (degree 3) — opposite sides, no center access
        Level(
            id = 40,
            name = "The Singularity",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Modified inner ring (4 edges) — 75% broken
                // Kept: 9-10, 11-12, 13-14, 15-8
                // Removed: 8-9, 10-11, 12-13, 14-15
                Edge(9, 10), Edge(11, 12), Edge(13, 14), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Star skip connections - clockwise (8 edges)
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center connections (6 edges) - bridges the island topology
                Edge(16, 8), Edge(16, 9), Edge(16, 10),
                Edge(16, 12), Edge(16, 13), Edge(16, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(11, 15),
                firstEdge = Pair(11, 12),
                steps = listOf(
                    HintStep(text = "The Singularity — the center controls everything with six connections."),
                    HintStep(text = "Inner bottom-right and inner top-left are the only start points.", showValidStarts = true),
                    HintStep(text = "Start from inner bottom-right and go to inner bottom.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 41: The Gauntlet — 17 nodes, 34 edges
        // Missing skip at node 4 creates outer bottleneck, 6-degree center requires 3 visits - PATH
        // 3 inner ring breaks at 8-9, 11-12, 13-14
        // Odd nodes: 4 (degree 3), 13 (degree 3)
        Level(
            id = 41,
            name = "The Gauntlet",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner ring (5 edges) — breaks at 8-9, 11-12, 13-14
                Edge(9, 10), Edge(10, 11), Edge(12, 13), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // CW skip connections (7 edges) — skip removed for outer 4
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(5, 14), Edge(6, 15), Edge(7, 8),
                // Center connections (6 edges)
                Edge(16, 8), Edge(16, 9), Edge(16, 11),
                Edge(16, 12), Edge(16, 13), Edge(16, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(4, 13),
                firstEdge = Pair(4, 5),
                steps = listOf(
                    HintStep(text = "The center is a 6-way junction — time your visits or get trapped."),
                    HintStep(text = "One start is on the outer ring, the other is hidden inside.", showValidStarts = true),
                    HintStep(text = "Start from the outer bottom and head toward bottom-left.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 42: The Vortex — 17 nodes, 33 edges
        // Center node has odd degree — forced start/end at the center is counterintuitive - PATH
        // Single inner ring break at 10-11, missing skip at outer 6
        // Odd nodes: 6 (degree 3), 16 (degree 3)
        Level(
            id = 42,
            name = "The Vortex",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner ring (7 edges) — break at 10-11
                Edge(8, 9), Edge(9, 10), Edge(11, 12),
                Edge(12, 13), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // CW skip connections (7 edges) — skip removed for outer 6
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 13), Edge(5, 14), Edge(7, 8),
                // Center connections (3 edges) — bridges the 10-11 gap
                Edge(16, 10), Edge(16, 11), Edge(16, 15)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(6, 16),
                firstEdge = Pair(16, 10),
                steps = listOf(
                    HintStep(text = "The center draws everything in — but can you start from the eye of the vortex?"),
                    HintStep(text = "One start point is on the outer edge, the other is at the very center.", showValidStarts = true),
                    HintStep(text = "Start from the center and head to inner right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 43: The Funnel — 17 nodes, 33 edges
        // Right half CW skips, left half CCW skips creating directional funnels - PATH
        // Node 8 extreme bottleneck (degree 2), node 12 is 5-way junction
        // 3 inner ring breaks at 8-9, 10-11, 12-13
        // Odd nodes: 12 (degree 5), 15 (degree 3)
        Level(
            id = 43,
            name = "The Funnel",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner ring (5 edges) — breaks at 8-9, 10-11, 12-13
                Edge(9, 10), Edge(11, 12), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Mixed skip connections (8 edges) — right CW, left CCW
                Edge(0, 9), Edge(1, 10), Edge(2, 11), Edge(3, 12),
                Edge(4, 11), Edge(5, 12), Edge(6, 13), Edge(7, 14),
                // Center connections (4 edges)
                Edge(16, 9), Edge(16, 10), Edge(16, 12), Edge(16, 13)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(12, 15),
                firstEdge = Pair(12, 11),
                steps = listOf(
                    HintStep(text = "The skip connections funnel you in one direction — fighting the current is the key."),
                    HintStep(text = "Inner bottom is a 5-way junction. Inner top-left is a tight bottleneck.", showValidStarts = true),
                    HintStep(text = "Start from inner bottom and head to inner bottom-right.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 44: The Paradox — 17 nodes, 34 edges
        // All 17 nodes have degree 4 — perfect balance hides deadly traps - CIRCUIT
        // Alternating CW/CCW skips, 2 inner ring breaks at 10-11 and 13-14
        // Center bridges both gaps, must be visited exactly twice
        // 0 odd nodes = circuit (start anywhere)
        Level(
            id = 44,
            name = "The Paradox",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner ring (6 edges) — breaks at 10-11 and 13-14
                Edge(8, 9), Edge(9, 10), Edge(11, 12),
                Edge(12, 13), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Alternating skip connections (8 edges) — even CW, odd CCW
                Edge(0, 9), Edge(1, 8), Edge(2, 11), Edge(3, 10),
                Edge(4, 13), Edge(5, 12), Edge(6, 15), Edge(7, 14),
                // Center connections (4 edges) — bridges both inner ring gaps
                Edge(16, 10), Edge(16, 11), Edge(16, 13), Edge(16, 14)
            ),
            hints = LevelHints(
                validStartNodeIds = (0..16).toList(),
                firstEdge = Pair(0, 1),
                steps = listOf(
                    HintStep(text = "Every node has exactly 4 connections — perfect symmetry hides a deadly trap."),
                    HintStep(text = "Start anywhere, but the center is a one-way bridge between two halves.", showValidStarts = true),
                    HintStep(text = "Try starting at the top outer node and going clockwise first.", showValidStarts = true, showFirstEdge = true)
                )
            )
        ),

        // Level 45: The Abyss — 17 nodes, 34 edges
        // Reversed skip pattern (mirror of Level 36), 5-degree center as forced endpoint - PATH
        // Node 9 has degree 3 with no direct center access
        // 3 inner ring breaks at 8-9, 10-11, 12-13
        // Odd nodes: 9 (degree 3), 16 (degree 5)
        Level(
            id = 45,
            name = "The Abyss",
            nodes = listOf(
                // Outer octagon (8 nodes)
                Node(0, Offset(0.50f, 0.06f)),   // top
                Node(1, Offset(0.81f, 0.19f)),   // top-right
                Node(2, Offset(0.94f, 0.50f)),   // right
                Node(3, Offset(0.81f, 0.81f)),   // bottom-right
                Node(4, Offset(0.50f, 0.94f)),   // bottom
                Node(5, Offset(0.19f, 0.81f)),   // bottom-left
                Node(6, Offset(0.06f, 0.50f)),   // left
                Node(7, Offset(0.19f, 0.19f)),   // top-left
                // Inner octagon (8 nodes)
                Node(8, Offset(0.50f, 0.25f)),   // inner top
                Node(9, Offset(0.68f, 0.32f)),   // inner top-right
                Node(10, Offset(0.75f, 0.50f)),  // inner right
                Node(11, Offset(0.68f, 0.68f)),  // inner bottom-right
                Node(12, Offset(0.50f, 0.75f)),  // inner bottom
                Node(13, Offset(0.32f, 0.68f)),  // inner bottom-left
                Node(14, Offset(0.25f, 0.50f)),  // inner left
                Node(15, Offset(0.32f, 0.32f)),  // inner top-left
                // Center hub (1 node)
                Node(16, Offset(0.50f, 0.50f))   // center
            ),
            edges = listOf(
                // Outer octagon ring (8 edges)
                Edge(0, 1), Edge(1, 2), Edge(2, 3), Edge(3, 4),
                Edge(4, 5), Edge(5, 6), Edge(6, 7), Edge(7, 0),
                // Inner ring (5 edges) — breaks at 8-9, 10-11, 12-13
                Edge(9, 10), Edge(11, 12), Edge(13, 14), Edge(14, 15), Edge(15, 8),
                // Radial spokes - outer to inner (8 edges)
                Edge(0, 8), Edge(1, 9), Edge(2, 10), Edge(3, 11),
                Edge(4, 12), Edge(5, 13), Edge(6, 14), Edge(7, 15),
                // Reversed alternating skip connections (8 edges) — even CCW, odd CW
                Edge(0, 15), Edge(1, 10), Edge(2, 9), Edge(3, 12),
                Edge(4, 11), Edge(5, 14), Edge(6, 13), Edge(7, 8),
                // Center connections (5 edges)
                Edge(16, 8), Edge(16, 10), Edge(16, 11),
                Edge(16, 12), Edge(16, 13)
            ),
            hints = LevelHints(
                validStartNodeIds = listOf(9, 16),
                firstEdge = Pair(9, 1),
                steps = listOf(
                    HintStep(text = "Everything you learned is reversed. The abyss stares back."),
                    HintStep(text = "The center is a 5-way dead end. Inner top-right is a 3-way bottleneck.", showValidStarts = true),
                    HintStep(text = "Start from inner top-right and head to outer top-right.", showValidStarts = true, showFirstEdge = true)
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
