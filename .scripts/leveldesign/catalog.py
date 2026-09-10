"""The 60 LineFlow levels, designed by hand.

Design rules of thumb (all enforced by geometry.check):
  * on the smallest phone, dots >= 56dp apart and >= 36dp from lines they are not on
  * lines leaving a dot are >= 30 degrees apart
  * no crossing lines unless the level opts in (crossings never near dots)
  * exactly 0 or 2 odd dots, connected, and a verified one-stroke solution

Difficulty comes from four dials: number of lines, a forced start (two
odd dots), odd dots that do not look like end points, and regions joined
by a single dot or line that must be finished before moving on. Every
chapter turns the dials a little further, and levels are ordered by a
difficulty score (lines + random-walk failure rate, see build.py) so each
one is a little harder than the last.

  1-10  First strokes   loops, crossings, hubs, odd dots
 11-20  Odd ones out    bigger shapes where spotting the two odd dots matters
 21-30  Order matters   shared dots and bridges: finish a region before leaving
 31-40  Deep water      dense shapes, several regions, long strokes
 41-50  Mastery         everything at once
 51-60  Beyond          bigger walls, hidden odd dots, one-way bridges
"""
from __future__ import annotations

import math

from geometry import LevelSpec, random_walk_failure_rate
from shapes import (
    braced_grid, chain, cycle, dedupe, grid, hexagon_with_triangle, merge, pentacle,
    pyramid, ring, shift, star_ring, zigzag_rings,
)


def L(name, nodes, edges, insight, **kw) -> LevelSpec:
    return LevelSpec(name=name, nodes=list(nodes), edges=dedupe(edges), insight=insight, **kw)


def rad(deg):
    return math.radians(deg)


def polar(r, deg):
    return (round(r * math.cos(rad(deg)), 4), round(r * math.sin(rad(deg)), 4))


def plain_grid(cols, rows, sx=1.0, sy=1.0):
    nodes = grid(cols, rows, sx, sy)

    def idx(c, r):
        return r * cols + c

    edges = []
    for r in range(rows):
        for c in range(cols):
            if c + 1 < cols:
                edges.append((idx(c, r), idx(c + 1, r)))
            if r + 1 < rows:
                edges.append((idx(c, r), idx(c, r + 1)))
    return nodes, edges, idx


def bastions(nodes, edges, n, r):
    """Adds a triangular bastion on every side of the n-gon in nodes[0:n]."""
    nodes = list(nodes)
    edges = list(edges)
    for i in range(n):
        nodes.append(polar(r, -90 + 360 * i / n + 180 / n))
        edges += [(i, len(nodes) - 1), (len(nodes) - 1, (i + 1) % n)]
    return nodes, edges


def clover():
    """Three rhombus leaves on a hub, tips joined through outer midpoints."""
    nodes = [(0.0, 0.0)]
    edges = []
    for i in range(3):
        th = -90 + 120 * i
        ia = len(nodes)
        nodes += [polar(1.0, th - 33), polar(1.8, th), polar(1.0, th + 33)]
        edges += [(0, ia), (ia, ia + 1), (ia + 1, ia + 2), (ia + 2, 0)]
    for i in range(3):
        im = len(nodes)
        nodes.append(polar(2.0, -90 + 120 * i + 60))
        edges += [(2 + 3 * i, im), (im, 2 + 3 * ((i + 1) % 3))]
    return nodes, edges


def fortress():
    """4x4 walls, a brace in each corner cell, one bar across the courtyard."""
    nodes, edges, idx = plain_grid(4, 4, 1.0, 1.15)
    edges += [(idx(0, 1), idx(1, 0)), (idx(2, 0), idx(3, 1)), (idx(0, 2), idx(1, 3)), (idx(2, 3), idx(3, 2))]
    edges += [(idx(1, 1), idx(2, 2))]
    return nodes, edges


def keep():
    """4x5 walls, corner braces and a diamond of braces around the centre."""
    nodes, edges, idx = plain_grid(4, 5, 1.0, 1.1)
    edges += [(idx(0, 1), idx(1, 0)), (idx(2, 0), idx(3, 1)), (idx(0, 3), idx(1, 4)), (idx(2, 4), idx(3, 3))]
    edges += [(idx(1, 2), idx(2, 1)), (idx(2, 1), idx(3, 2)), (idx(2, 3), idx(3, 2)), (idx(1, 2), idx(2, 3))]
    return nodes, edges


def courtyard():
    """5x5 braced walls with the centre dot removed; one chord crosses the
    courtyard so the two dots beside it are the odd ones."""
    nodes, edges = braced_grid(5, 5, 1.0, 1.15, skip=((3, 0), (0, 3)))
    centre = 12
    edges = [(a, b) for a, b in edges if centre not in (a, b)] + [(7, 17)]
    keep = [i for i in range(len(nodes)) if i != centre]
    remap = {old: new for new, old in enumerate(keep)}
    return [nodes[i] for i in keep], [(remap[a], remap[b]) for a, b in edges]


def great_fortress():
    """5x5 walls, corner braces, two brace paths across the top-left and
    bottom-right corners, and a bar through the courtyard."""
    nodes, edges, idx = plain_grid(5, 5, 1.0, 1.15)
    edges += [(idx(1, 0), idx(0, 1)), (idx(3, 0), idx(4, 1)), (idx(0, 3), idx(1, 4)), (idx(3, 4), idx(4, 3))]
    edges += [(idx(2, 0), idx(1, 1)), (idx(1, 1), idx(0, 2)), (idx(2, 4), idx(3, 3)), (idx(3, 3), idx(4, 2))]
    edges += [(idx(1, 1), idx(2, 2)), (idx(2, 2), idx(3, 3))]
    return nodes, edges


def trellis(cols, rows, sy, parity=0):
    """Plain grid with an X in every other cell (checkerboard).

    `parity` picks which colour of the checkerboard is crossed."""
    cells = [(c, r) for c in range(cols - 1) for r in range(rows - 1)]
    crossed = [(c, r) for c, r in cells if (c + r) % 2 == parity]
    plain = [cell for cell in cells if cell not in crossed]
    return braced_grid(cols, rows, 1.0, sy, skip=plain, falling=crossed)


def build() -> list[LevelSpec]:
    levels: list[LevelSpec] = []
    add = levels.append

    # ------------------------------------------------------------------
    # Chapter 1 - First strokes (1-10)
    # ------------------------------------------------------------------
    add(L("The Triangle", ring(3, 1.0), cycle([0, 1, 2]),
          "A loop can be drawn from any corner.", start=0, first=1))

    add(L("The Square", [(0, 0), (1, 0), (1, 1), (0, 1)], cycle([0, 1, 2, 3]),
          "Another loop. Pick a corner and go around.", start=0, first=1))

    add(L("The Star", ring(5, 1.0), [(0, 2), (2, 4), (4, 1), (1, 3), (3, 0)],
          "Lines may cross. Draw the star the way you did as a kid.",
          allow_crossings=True, start=0, first=2))

    add(L("The House", [(0, 1), (2, 1), (2, 3), (0, 3), (1, 0)],
          cycle([0, 1, 2, 3]) + [(0, 4), (4, 1)],
          "Count the lines at each dot. Two dots have three: start at one of them.",
          start=0, first=4))

    add(L("The Window", grid(3, 2, 1.0, 1.0),
          chain([0, 1, 2]) + chain([3, 4, 5]) + [(0, 3), (1, 4), (2, 5)],
          "The corners are easy; the two middle dots decide where you start.",
          start=1, first=0))

    add(L("The Fan", [(1.5, 0), (0, 1.0), (3, 1.0), (0.3, 2.8), (2.7, 2.8)],
          [(0, 1), (0, 2), (1, 2), (1, 3), (1, 4), (2, 4), (3, 4)],
          "One dot on the right and one at the bottom have odd lines.",
          start=2, first=0))

    add(L("The Envelope", [(0, 1), (2, 1), (2, 3), (0, 3), (1, 0)],
          cycle([0, 1, 2, 3]) + [(0, 4), (4, 1), (0, 2), (1, 3)],
          "The famous one. Start at a bottom corner and end at the other.",
          allow_crossings=True, start=3, first=2))

    add(L("The Bow Tie", [(0, 0), (0, 2), (1.4, 1), (2.8, 0), (2.8, 2)],
          [(0, 1), (0, 2), (1, 2), (2, 3), (2, 4), (3, 4)],
          "The middle dot is visited twice. Finish one wing before the other.",
          start=0, first=1))

    add(L("The Pentacle", *pentacle(1.0),
          "A star inside a ring. Every point has four lines, so start anywhere.",
          allow_crossings=True, start=0, first=1))

    add(L("The Hexagon", *hexagon_with_triangle(1.0),
          "A loop with a triangle inside. All even, but the triangle can strand a corner.",
          start=0, first=1))

    # ------------------------------------------------------------------
    # Chapter 2 - Odd ones out (11-20)
    # ------------------------------------------------------------------
    add(L("The Gem",
          [(1.5, 0), (0.6, 0.9), (2.4, 0.9), (-0.2, 2), (3.2, 2), (0.6, 3.1), (2.4, 3.1), (1.5, 4)],
          [(0, 1), (0, 2), (1, 2), (1, 3), (3, 5), (1, 5), (2, 4), (4, 6), (2, 6), (5, 6), (5, 7), (6, 7), (2, 5)],
          "One long facet cuts across the gem. Its two ends are your start and finish.",
          start=2, first=0))

    add(L("The Hexagram", *star_ring(6, 2, 1.0),
          "Two triangles and a ring. Cross the middle freely; only dots count.",
          allow_crossings=True, start=0, first=1))

    add(L("The Shield",
          [(1.5, 0), (0, 1), (3, 1), (0, 2.6), (3, 2.6), (1.5, 1.5), (1.5, 3.6)],
          [(0, 1), (0, 2), (1, 3), (2, 4), (1, 5), (2, 5), (5, 3), (5, 4), (3, 6), (4, 6), (3, 4)],
          "The two shoulders are the odd dots. Everything below them is loops.",
          start=1, first=0))

    add(L("The Sailboat",
          [(0, 2), (1, 3.2), (3, 3.2), (4, 2), (2, 2), (2, 0), (3.4, 0.6), (0.6, 0.6)],
          [(0, 1), (1, 2), (2, 3), (0, 4), (4, 3), (1, 4), (2, 4), (5, 6), (6, 4), (5, 7), (7, 4)],
          "The two hull dots have three lines each. Sail from one to the other.",
          start=1, first=0))

    add(L("The Ladder", *braced_grid(2, 4, 2.0, 1.4),
          "Rails, rungs and braces. The odd dots are where the braces stop.",
          start=1, first=0))

    add(L("The Twin Envelopes",
          [(0, 1), (2, 1), (4, 1), (0, 3), (2, 3), (4, 3), (1, 0), (3, 0)],
          [(0, 1), (1, 2), (3, 4), (4, 5), (0, 3), (2, 5), (0, 4), (1, 3), (1, 5), (2, 4),
           (0, 6), (6, 1), (1, 7), (7, 2)],
          "Two envelopes share a middle. The bottom corners are still the odd ones.",
          allow_crossings=True, start=3, first=0))

    add(L("The Octagram", *star_ring(8, 3, 1.0),
          "Eight points and a star of long chords. All even: any start works.",
          allow_crossings=True, start=0, first=1))

    add(L("The Hourglass Star", *merge(
        [pentacle(1.0, 0, 0, -90), pentacle(1.0, 0, 2 * math.sin(rad(54)), 90)],
        [((0, 3), (1, 2)), ((0, 2), (1, 3))]),
          "Two stars share an edge. The dots on that edge carry seven lines: start on one.",
          allow_crossings=True))

    add(L("The Lattice", *braced_grid(3, 3, 1.0, 1.0),
          "A braced grid. The odd dots sit at two opposite corners.",
          start=2, first=1))

    add(L("The Clover", *clover(),
          "Three leaves on one hub, all even. Do not leave a leaf half done."))

    # ------------------------------------------------------------------
    # Chapter 3 - Order matters (21-30)
    # ------------------------------------------------------------------
    add(L("The Necklace",
          [(0, 0), (-1, 1), (1, 1), (0, 2), (-1, 3), (1, 3), (0, 4), (-1, 5), (1, 5), (0, 6)],
          cycle([0, 1, 3, 2]) + cycle([3, 4, 6, 5]) + cycle([6, 7, 9, 8]) + [(4, 5)],
          "Each shared dot is a door. Close a diamond before you walk through it."))

    add(L("The Wheel", *zigzag_rings(5, 1.0, 0.4),
          "Two rings and a zigzag. Work in wedges so no spoke is stranded.",
          start=0, first=1))

    add(L("The Pyramid", *pyramid(4, 1.0),
          "Ten dots, all even. The middle rows carry six lines each; do not strand them.",
          start=0, first=1))

    add(L("The Star Fort", *bastions(*pentacle(1.0), 5, 1.6),
          "A pentacle with five bastions. Each bastion is a detour you must take once.",
          allow_crossings=True))

    add(L("The Butterfly", *merge(
        [pentacle(1.0, 0, 0, 0), pentacle(1.0, 2.0, 0, 180)], [((0, 0), (1, 0))]),
          "Two stars pinched at one dot. That dot is the only way between the wings.",
          allow_crossings=True))

    add(L("The Hex Fort", *bastions(*hexagon_with_triangle(1.0), 6, 1.5),
          "Six bastions and a triangle inside. All even, but the triangle strands corners."))

    add(L("The Snowflake", *zigzag_rings(6, 1.0, 0.48),
          "Six-fold symmetry, all even. Sweep in wedges, not in circles.",
          start=0, first=1))

    add(L("The Serpent", *braced_grid(3, 4, 1.0, 1.15),
          "Braces everywhere. Top-right and bottom-left are the odd dots.",
          start=2, first=1))

    add(L("The Rocket", *merge(
        [pyramid(3, 1.0), (shift(grid(3, 3), -1.0, 2 * 0.87), braced_grid(3, 3)[1])],
        [((0, 3), (1, 0)), ((0, 4), (1, 1)), ((0, 5), (1, 2))]),
          "A nose cone on a braced body. One odd dot is where the cone meets the body."))

    add(L("The Hourglass", *merge(
        [hexagon_with_triangle(1.0, 0, 0, (0, 2, 4)), hexagon_with_triangle(1.0, 0, 2.0, (1, 3, 5))],
        [((0, 3), (1, 0))]),
          "Two hexagons share one dot. Whatever you leave behind on one side is lost.",
          start=0, first=1))

    # ------------------------------------------------------------------
    # Chapter 4 - Deep water (31-40)
    # ------------------------------------------------------------------
    n1, e1 = pentacle(0.9, 0, 0, -90)
    n2, e2 = pentacle(0.9, 0, 3.4, 90)
    add(L("The Dumbbell", n1 + n2, e1 + [(a + 5, b + 5) for a, b in e2] + [(2, 8)],
          "Two stars, one bar. Cross the bar exactly once, with nothing left behind.",
          allow_crossings=True))

    h = 2 * math.sin(rad(54))
    add(L("The Constellation", *merge(
        [pentacle(1.0, 0, 0, -90), pentacle(1.0, 0, h, 90), pentacle(1.0, 0, h + 2.0, -90)],
        [((0, 3), (1, 2)), ((0, 2), (1, 3)), ((1, 0), (2, 0))]),
          "Three stars in a column. The bottom star hangs from a single dot.",
          allow_crossings=True))

    add(L("The Sunflower", *zigzag_rings(7, 1.0, 0.55),
          "Seven petals, all even. Alternate inner and outer so nothing is stranded.",
          start=0, first=1))

    add(L("The Fortress", *fortress(),
          "Walls, corner braces and one bar across the courtyard. The odd dots are inside."))

    add(L("The Tower", *braced_grid(3, 5, 1.0, 1.1),
          "Five storeys of braces. Odd dots at top-right and bottom-left.",
          start=2, first=1))

    n1, e1 = pentacle(0.85, 1.0, 0)
    n2, e2 = braced_grid(3, 3)
    add(L("The Balloon",
          n1 + shift(n2, 0, 1.7), e1 + [(a + 5, b + 5) for a, b in e2] + [(2, 7)],
          "A star tied to a basket by one string. Cross the string exactly once.",
          allow_crossings=True))

    n1, e1 = braced_grid(3, 3)
    n2, e2 = pentacle(0.9, 1.0, 3.7)
    add(L("The Anchor", n1 + n2, e1 + [(a + 9, b + 9) for a, b in e2] + [(6, 9)],
          "A grid hangs from a star by one line. Cross it once and never come back.",
          allow_crossings=True))

    n1, e1 = braced_grid(3, 3)
    hx = ring(6, 1.1, 0.5, 2.9 + 0.953, rot=0)
    he = cycle(list(range(6))) + cycle([1, 3, 5])
    add(L("The Lantern", n1 + hx, e1 + [(a + 9, b + 9) for a, b in he] + [(6, 13)],
          "A grid above a hexagon, tied by one line. The line is a one-way door."))

    add(L("The Great Pyramid", *pyramid(5, 1.0),
          "Fifteen dots, all even. Work row by row and keep a way back.",
          start=0, first=1))

    add(L("The Mandala", *zigzag_rings(8, 1.0, 0.62),
          "Sixteen dots, thirty-two lines, all even. Wedges, not circles.",
          start=0, first=1))

    # ------------------------------------------------------------------
    # Chapter 5 - Mastery (41-50)
    # ------------------------------------------------------------------
    top_nodes, top_edges = pyramid(4, 1.0)
    add(L("The Diamond", *merge(
        [(top_nodes, top_edges), ([(x, 2 * 2.61 - y) for x, y in top_nodes], top_edges)],
        [((0, 6), (1, 6)), ((0, 7), (1, 7)), ((0, 8), (1, 8)), ((0, 9), (1, 9))]),
          "Two pyramids on a shared base. The tips are the odd dots."))

    add(L("The Net", *braced_grid(4, 4, 1.0, 1.15),
          "Thirty-three lines. Odd dots at two corners; the inside can strand you.",
          start=3, first=2))

    add(L("The Bow Tie Grid", *merge(
        [braced_grid(3, 3), (shift(grid(3, 3), -2.0, 2.0), braced_grid(3, 3)[1])],
        [((0, 6), (1, 2))]),
          "Two grids pinched at a single dot. That dot is the only door between them."))

    n1, e1 = braced_grid(3, 3)
    n2, e2 = braced_grid(3, 3, mirror=True)
    add(L("The Twin Towers", n1 + shift(n2, 0, 3.0), e1 + [(a + 9, b + 9) for a, b in e2] + [(6, 11)],
          "Two braced grids and one tie. Nothing may be stranded when you switch."))

    n2, e2 = braced_grid(4, 3, 1.0, 1.2)
    cx = 3 - 0.9 * math.cos(rad(54))
    n1, e1 = pentacle(0.9, cx, -0.9 * math.sin(rad(54)) - 0.9, -90)
    add(L("The Chandelier", n1 + n2, e1 + [(a + 5, b + 5) for a, b in e2] + [(2, 8)],
          "A star hangs over a wide deck by one chain. Which side do you start on?",
          allow_crossings=True))

    add(L("The Obelisk", *merge(
        [pyramid(4, 1.0), (shift(grid(4, 3, 1.0, 1.2), -1.5, 3 * 0.87), braced_grid(4, 3, 1.0, 1.2)[1])],
        [((0, 6), (1, 0)), ((0, 7), (1, 1)), ((0, 8), (1, 2)), ((0, 9), (1, 3))]),
          "A pyramid on a braced base. One odd dot hides where the two meet."))

    add(L("The Spire", *braced_grid(4, 5, 1.0, 1.1, skip=((2, 0), (0, 3))),
          "Twenty dots. The odd dots have moved one step inside; find them first."))

    add(L("The Keep", *keep(),
          "Corner braces, a diamond in the courtyard. The odd dots are halfway up the walls."))

    n1, e1 = braced_grid(4, 3)
    n2, e2 = braced_grid(4, 3, mirror=True)
    add(L("The Twin Citadels", n1 + shift(n2, 0, 2.9), e1 + [(a + 12, b + 12) for a, b in e2] + [(8, 15)],
          "Forty-seven lines, two citadels, one bridge. Sweep a whole citadel before you cross.",
          start=3, first=2))

    n1, e1 = pyramid(4, 1.0)
    n2, e2 = braced_grid(4, 3, 1.0, 1.0, mirror=True)
    add(L("The Monument", n1 + shift(n2, -1.5, 3.6), e1 + [(a + 10, b + 10) for a, b in e2] + [(6, 13)],
          "A pyramid on a plinth, joined by one line. Finish the top before you descend."))

    # ------------------------------------------------------------------
    # Chapter 6 - Beyond (51-60)
    # ------------------------------------------------------------------
    add(L("The Courtyard", *courtyard(),
          "Walls around an empty courtyard. The two dots beside the chord are the odd ones."))

    add(L("The Great Fortress", *great_fortress(),
          "Corner braces and a bar through the courtyard. The odd dots sit inside the walls."))

    add(L("The Watchtower", *braced_grid(4, 6, 1.0, 0.9),
          "Six storeys of braces. Top-right and bottom-left are the odd dots.",
          start=3, first=2))

    add(L("The Trellis Tower", *trellis(4, 6, 0.9, parity=1),
          "Seven crossed cells, all even. Cross freely, but never leave a storey half done.",
          allow_crossings=True, start=0, first=1))

    add(L("The Trellis", *trellis(5, 5, 1.15),
          "Every other cell is crossed. The odd dots are the two corners with a cross.",
          allow_crossings=True))

    add(L("The Cathedral", *merge(
        [pyramid(5, 1.0), (shift(grid(5, 3, 1.0, 1.0), -2.0, 4 * 0.87), braced_grid(5, 3, 1.0, 1.0)[1])],
        [((0, 10 + i), (1, i)) for i in range(5)]),
          "A pyramid on a braced nave. One odd dot hides where the roof meets the wall."))

    n1, e1 = braced_grid(5, 3, 1.0, 0.9)
    n2, e2 = braced_grid(5, 3, 1.0, 0.9, mirror=True)
    add(L("The Twin Nets", n1 + shift(n2, 0, 2.7), e1 + [(a + 15, b + 15) for a, b in e2] + [(10, 19)],
          "Two nets and one thread between them. Empty a net completely before you cross.",
          start=4, first=3))

    add(L("The Great Tower", *braced_grid(5, 6, 1.0, 0.9, skip=((3, 0), (2, 1), (0, 4))),
          "Thirty dots. Three braces are missing, and that moves the odd dots deep inside."))

    add(L("The Grand Trellis", *trellis(5, 6, 0.9),
          "Ten crossed cells. Both odd dots are on the left wall; the crossings hide them.",
          allow_crossings=True))

    add(L("The Grand Citadel", *braced_grid(5, 6, 1.0, 0.9),
          "Sixty-nine lines, all braces. Odd dots at top-right and bottom-left; never strand a storey.",
          start=4, first=3))

    # Players feel difficulty mostly as "how many lines", then as "how easy it
    # is to get stuck", so that is the order: line count first, failure rate
    # to break ties. The chapters above describe the ideas, not the numbering.
    levels.sort(key=lambda lv: (len(lv.edges), random_walk_failure_rate(lv)))
    for i, lv in enumerate(levels, start=1):
        lv.id = i
    return levels
