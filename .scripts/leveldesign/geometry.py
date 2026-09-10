"""Graph + geometry checks for LineFlow levels.

Everything here is derived from how the game is actually played:

* The canvas spans the full screen width. On the smallest phone we support
  (360x640dp with a status bar and a 3-button navigation bar) it measures
  SMALL_CANVAS_WIDTH_DP x SMALL_CANVAS_HEIGHT_DP once the top bar, status
  strip and bottom margin are taken away.
* Dot centres keep CONTENT_MARGIN_DP clear of the canvas edge, so the
  centres span SMALL_SPAN_WIDTH_DP x SMALL_SPAN_HEIGHT_DP, uniformly scaled.
* A dot is "hit" when the finger comes within the touch radius of it. The
  game caps that radius at HIT_RADIUS_DP and shrinks it on cramped screens
  so hit circles never overlap and never reach a line they are not part of;
  the spacing rules below guarantee it never drops under MIN_HIT_RADIUS_DP
  on the smallest phone (and stays at the full 32dp from ~410dp-wide phones).
"""
from __future__ import annotations

import itertools
import math
import random
from collections import defaultdict
from dataclasses import dataclass, field

# --- Play-area assumptions (keep in sync with OneLineDrawGame.kt / tests) ---
SMALL_CANVAS_WIDTH_DP = 360.0   # full width of the smallest supported phone
SMALL_CANVAS_HEIGHT_DP = 400.0  # 640 - 24 status - 48 nav - 60 top bar - 84 strip - 24 bottom
HIT_RADIUS_DP = 32.0            # touch radius on roomy screens
CONTENT_MARGIN_DP = HIT_RADIUS_DP + 8.0  # dot centres keep this clear of the canvas edge
SMALL_SPAN_WIDTH_DP = SMALL_CANVAS_WIDTH_DP - 2 * CONTENT_MARGIN_DP   # 280
SMALL_SPAN_HEIGHT_DP = SMALL_CANVAS_HEIGHT_DP - 2 * CONTENT_MARGIN_DP  # 320
MIN_HIT_RADIUS_DP = 25.0        # the game never has to shrink the touch radius below this
MIN_NODE_DISTANCE_DP = 56.0        # two hit circles of MIN_HIT_RADIUS never overlap (+6dp)
MIN_NODE_EDGE_DISTANCE_DP = 36.0   # tracing a line never brushes another dot (+11dp)
MIN_CROSSING_NODE_DISTANCE_DP = 36.0
MIN_EDGE_ANGLE_DEG = 30.0          # lines leaving a dot are visually distinct
MAX_VERTICAL_STRETCH = 1.3         # tall phones stretch squat levels up to this
TALL_PLAY_ASPECT = 2.1             # play-area height/width on the tallest phones
MIN_EDGE_ANGLE_STRETCHED_DEG = 23.5  # visual-only: touch rules do not depend on angles

Point = tuple[float, float]


@dataclass
class LevelSpec:
    name: str
    nodes: list[Point]
    edges: list[tuple[int, int]]
    insight: str                      # hand-written first hint
    allow_crossings: bool = False
    start: int | None = None          # preferred start node for the hint
    first: int | None = None          # preferred first move for the hint
    id: int = 0
    notes: list[str] = field(default_factory=list)

    # ---- derived helpers -------------------------------------------------
    def degree(self) -> dict[int, int]:
        deg = {i: 0 for i in range(len(self.nodes))}
        for a, b in self.edges:
            deg[a] += 1
            deg[b] += 1
        return deg

    def odd_nodes(self) -> list[int]:
        return [n for n, d in self.degree().items() if d % 2]

    def adjacency(self) -> dict[int, list[int]]:
        adj = defaultdict(list)
        for a, b in self.edges:
            adj[a].append(b)
            adj[b].append(a)
        return adj

    def bbox(self):
        xs = [p[0] for p in self.nodes]
        ys = [p[1] for p in self.nodes]
        return min(xs), min(ys), max(xs), max(ys)

    def valid_starts(self) -> list[int]:
        odd = self.odd_nodes()
        return odd if odd else list(range(len(self.nodes)))

    def scale_dp(self) -> float:
        """dp per level unit on the smallest supported phone (dot centres
        fitted into SMALL_SPAN_WIDTH_DP x SMALL_SPAN_HEIGHT_DP, no stretch)."""
        x0, y0, x1, y1 = self.bbox()
        w = max(x1 - x0, 1e-6)
        h = max(y1 - y0, 1e-6)
        return min(SMALL_SPAN_WIDTH_DP / w, SMALL_SPAN_HEIGHT_DP / h)

    def normalized(self) -> list[Point]:
        """Nodes translated/scaled into 0..1 space keeping aspect ratio."""
        x0, y0, x1, y1 = self.bbox()
        span = max(x1 - x0, y1 - y0, 1e-6)
        return [((x - x0) / span, (y - y0) / span) for x, y in self.nodes]


# --- basic geometry ----------------------------------------------------------

def dist(a: Point, b: Point) -> float:
    return math.hypot(a[0] - b[0], a[1] - b[1])


def point_segment_distance(p: Point, a: Point, b: Point) -> float:
    ax, ay = a
    bx, by = b
    px, py = p
    dx, dy = bx - ax, by - ay
    l2 = dx * dx + dy * dy
    if l2 == 0:
        return dist(p, a)
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / l2))
    return dist(p, (ax + t * dx, ay + t * dy))


def segment_intersection(p1: Point, p2: Point, p3: Point, p4: Point) -> Point | None:
    """Proper (interior) intersection point of two segments, or None."""
    d = (p1[0] - p2[0]) * (p3[1] - p4[1]) - (p1[1] - p2[1]) * (p3[0] - p4[0])
    if abs(d) < 1e-12:
        return None
    t = ((p1[0] - p3[0]) * (p3[1] - p4[1]) - (p1[1] - p3[1]) * (p3[0] - p4[0])) / d
    u = -((p1[0] - p2[0]) * (p1[1] - p3[1]) - (p1[1] - p2[1]) * (p1[0] - p3[0])) / d
    eps = 1e-9
    if eps < t < 1 - eps and eps < u < 1 - eps:
        return (p1[0] + t * (p2[0] - p1[0]), p1[1] + t * (p2[1] - p1[1]))
    return None


# --- Euler trails ------------------------------------------------------------

def euler_trail(spec: LevelSpec, start: int, first: int | None = None) -> list[int] | None:
    """Hierholzer's algorithm. Returns the node sequence or None.

    If `first` is given the trail is forced to begin with edge start->first.
    """
    remaining = defaultdict(list)
    for idx, (a, b) in enumerate(spec.edges):
        remaining[a].append((b, idx))
        remaining[b].append((a, idx))
    used = [False] * len(spec.edges)

    prefix = [start]
    if first is not None:
        for idx, (a, b) in enumerate(spec.edges):
            if {a, b} == {start, first}:
                used[idx] = True
                break
        else:
            return None
        prefix.append(first)

    cur_start = prefix[-1]
    stack = [cur_start]
    circuit: list[int] = []
    ptr = {n: 0 for n in range(len(spec.nodes))}
    while stack:
        v = stack[-1]
        nbrs = remaining[v]
        while ptr[v] < len(nbrs) and used[nbrs[ptr[v]][1]]:
            ptr[v] += 1
        if ptr[v] < len(nbrs):
            w, idx = nbrs[ptr[v]]
            used[idx] = True
            stack.append(w)
        else:
            circuit.append(stack.pop())
    circuit.reverse()
    trail = prefix[:-1] + circuit
    return trail if is_valid_trail(spec, trail) else None


def is_valid_trail(spec: LevelSpec, trail: list[int]) -> bool:
    """Replays the trail exactly like a finger would: every step must be an
    unused line between the current dot and the next one."""
    if len(trail) != len(spec.edges) + 1:
        return False
    pool = defaultdict(int)
    for a, b in spec.edges:
        pool[frozenset((a, b))] += 1
    for a, b in zip(trail, trail[1:]):
        key = frozenset((a, b))
        if pool[key] <= 0:
            return False
        pool[key] -= 1
    return all(v == 0 for v in pool.values())


def solve(spec: LevelSpec) -> tuple[int, int, list[int]]:
    """Picks a start and first move (respecting preferences) and returns a
    verified trail. Raises if the level cannot be drawn in one stroke."""
    starts = [spec.start] if spec.start is not None else spec.valid_starts()
    for s in starts:
        firsts = [spec.first] if spec.first is not None else sorted(set(spec.adjacency()[s]))
        for f in firsts:
            trail = euler_trail(spec, s, f)
            if trail:
                return s, f, trail
    raise ValueError(f"{spec.name}: no one-stroke solution from starts {starts}")


# --- difficulty --------------------------------------------------------------

def random_walk_failure_rate(spec: LevelSpec, samples: int = 4000, seed: int = 7) -> float:
    """Share of naive random strokes (always pick a random unused line, start
    at a correct dot) that get stuck before finishing. 0 = impossible to get
    stuck, 1 = almost nobody stumbles into the solution."""
    rng = random.Random(seed)
    adj = spec.adjacency()
    starts = spec.valid_starts()
    n_edges = len(spec.edges)
    fails = 0
    for _ in range(samples):
        used = set()
        v = rng.choice(starts)
        for _step in range(n_edges):
            options = [w for w in adj[v] if frozenset((v, w)) not in used]
            if not options:
                fails += 1
                break
            w = rng.choice(options)
            used.add(frozenset((v, w)))
            v = w
    return fails / samples


# --- checks ------------------------------------------------------------------

def check(spec: LevelSpec) -> list[str]:
    """Returns a list of human readable problems (empty == level is good)."""
    problems: list[str] = []
    n = len(spec.nodes)
    deg = spec.degree()

    # graph sanity
    seen = set()
    for a, b in spec.edges:
        if a == b or not (0 <= a < n and 0 <= b < n):
            problems.append(f"bad edge {a}-{b}")
        key = frozenset((a, b))
        if key in seen:
            problems.append(f"duplicate edge {a}-{b}")
        seen.add(key)
    if any(d == 0 for d in deg.values()):
        problems.append("isolated node")

    adj = spec.adjacency()
    reach = {0}
    stack = [0]
    while stack:
        v = stack.pop()
        for w in adj[v]:
            if w not in reach:
                reach.add(w)
                stack.append(w)
    if len(reach) != n:
        problems.append("not connected")

    odd = spec.odd_nodes()
    if len(odd) not in (0, 2):
        problems.append(f"{len(odd)} odd nodes {odd} (need 0 or 2)")

    if problems:
        return problems

    try:
        solve(spec)
    except ValueError as exc:
        problems.append(str(exc))

    # geometry, measured in dp on the smallest supported play area
    s = spec.scale_dp()
    pts = spec.nodes
    for i, j in itertools.combinations(range(n), 2):
        d = dist(pts[i], pts[j]) * s
        if d < MIN_NODE_DISTANCE_DP:
            problems.append(f"nodes {i},{j} only {d:.0f}dp apart")
    for a, b in spec.edges:
        for k in range(n):
            if k in (a, b):
                continue
            d = point_segment_distance(pts[k], pts[a], pts[b]) * s
            if d < MIN_NODE_EDGE_DISTANCE_DP:
                problems.append(f"node {k} is {d:.0f}dp from line {a}-{b}")
    x0, y0, x1, y1 = spec.bbox()
    aspect = (y1 - y0) / max(x1 - x0, 1e-6)
    real_stretch = max(1.0, min(MAX_VERTICAL_STRETCH, TALL_PLAY_ASPECT / max(aspect, 1e-6)))
    for stretch, min_angle, label in ((1.0, MIN_EDGE_ANGLE_DEG, ""), (real_stretch, MIN_EDGE_ANGLE_STRETCHED_DEG, " when stretched")):
        for v in range(n):
            angles = sorted(
                math.degrees(math.atan2((pts[w][1] - pts[v][1]) * stretch, pts[w][0] - pts[v][0])) for w in adj[v]
            )
            if len(angles) < 2:
                continue
            for i in range(len(angles)):
                sep = angles[(i + 1) % len(angles)] - angles[i]
                if sep <= 0:
                    sep += 360
                if sep < min_angle - 0.5:  # tolerate float noise on exact 30s
                    problems.append(f"node {v}: lines only {sep:.0f} deg apart{label}")
    crossings = 0
    for (a, b), (c, d) in itertools.combinations(spec.edges, 2):
        if len({a, b, c, d}) < 4:
            continue
        x = segment_intersection(pts[a], pts[b], pts[c], pts[d])
        if x is None:
            continue
        crossings += 1
        if not spec.allow_crossings:
            problems.append(f"lines {a}-{b} and {c}-{d} cross")
        for k in range(n):
            if dist(x, pts[k]) * s < MIN_CROSSING_NODE_DISTANCE_DP:
                problems.append(f"crossing of {a}-{b}/{c}-{d} too close to node {k}")
    return sorted(set(problems))


def metrics(spec: LevelSpec) -> dict:
    x0, y0, x1, y1 = spec.bbox()
    return {
        "nodes": len(spec.nodes),
        "edges": len(spec.edges),
        "odd": len(spec.odd_nodes()),
        "max_deg": max(spec.degree().values()),
        "fail_rate": random_walk_failure_rate(spec),
        "aspect": (y1 - y0) / max(x1 - x0, 1e-6),
        "scale_dp": spec.scale_dp(),
    }
