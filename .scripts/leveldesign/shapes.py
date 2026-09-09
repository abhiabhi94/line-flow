"""Small helpers for laying out level shapes.

Coordinates are in arbitrary design units with y pointing down (screen
space). The renderer scales every level to fit the play area, so only the
relative layout matters.
"""
from __future__ import annotations

import math

Point = tuple[float, float]


def ring(n: int, r: float = 1.0, cx: float = 0.0, cy: float = 0.0, rot: float = -90.0) -> list[Point]:
    """n points evenly spaced on a circle, first point at `rot` degrees (top by default)."""
    return [
        (round(cx + r * math.cos(math.radians(rot + 360.0 * i / n)), 4),
         round(cy + r * math.sin(math.radians(rot + 360.0 * i / n)), 4))
        for i in range(n)
    ]


def grid(cols: int, rows: int, sx: float = 1.0, sy: float = 1.0) -> list[Point]:
    """Row-major grid: index = row * cols + col."""
    return [(c * sx, r * sy) for r in range(rows) for c in range(cols)]


def cycle(ids: list[int]) -> list[tuple[int, int]]:
    return [(ids[i], ids[(i + 1) % len(ids)]) for i in range(len(ids))]


def chain(ids: list[int]) -> list[tuple[int, int]]:
    return [(ids[i], ids[i + 1]) for i in range(len(ids) - 1)]


def dedupe(edges: list[tuple[int, int]]) -> list[tuple[int, int]]:
    seen = set()
    out = []
    for a, b in edges:
        key = frozenset((a, b))
        if key in seen:
            continue
        seen.add(key)
        out.append((a, b))
    return out


def shift(nodes: list[Point], dx: float, dy: float) -> list[Point]:
    return [(x + dx, y + dy) for x, y in nodes]


def mirror_x(nodes: list[Point]) -> list[Point]:
    xs = [x for x, _ in nodes]
    m = min(xs) + max(xs)
    return [(m - x, y) for x, y in nodes]


def merge(parts, glue=()):
    """Joins several (nodes, edges) parts into one level.

    `glue` lists ((part_i, node_i), (part_j, node_j)) pairs (i < j) whose two
    dots are the same dot; shared dots keep the position from part i.
    Returns (nodes, edges) with duplicate lines removed.
    """
    alias = {(pj, nj): (pi, ni) for (pi, ni), (pj, nj) in glue}
    nodes: list[Point] = []
    edges: list[tuple[int, int]] = []
    maps: list[dict[int, int]] = []
    for p, (part_nodes, part_edges) in enumerate(parts):
        m = {}
        for i, pt in enumerate(part_nodes):
            if (p, i) in alias:
                ap, an = alias[(p, i)]
                m[i] = maps[ap][an]
            else:
                m[i] = len(nodes)
                nodes.append(pt)
        maps.append(m)
        edges += [(m[a], m[b]) for a, b in part_edges]
    return nodes, dedupe(edges)


def braced_grid(cols: int, rows: int, sx: float = 1.0, sy: float = 1.0, skip=(), falling=(), mirror=False):
    """Grid with a rising diagonal brace in every cell.

    `skip` removes the rising brace of a cell, `falling` adds the other
    diagonal (creating a crossing), `mirror` flips the layout horizontally
    while keeping indices (row-major: index = row * cols + col).
    """
    nodes = grid(cols, rows, sx, sy)
    if mirror:
        nodes = mirror_x(nodes)

    def idx(c, r):
        return r * cols + c

    edges = []
    for r in range(rows):
        for c in range(cols):
            if c + 1 < cols:
                edges.append((idx(c, r), idx(c + 1, r)))
            if r + 1 < rows:
                edges.append((idx(c, r), idx(c, r + 1)))
            if c + 1 < cols and r + 1 < rows:
                if (c, r) not in skip:
                    edges.append((idx(c, r + 1), idx(c + 1, r)))
                if (c, r) in falling:
                    edges.append((idx(c, r), idx(c + 1, r + 1)))
    return nodes, edges


def pyramid(rows: int, s: float = 1.0):
    """Triangular lattice with `rows` rows (1 + 2 + ... + rows dots)."""
    nodes = []
    idx = {}
    for r in range(rows):
        for c in range(r + 1):
            idx[(r, c)] = len(nodes)
            nodes.append((round((c - r / 2) * s, 4), round(r * s * 0.87, 4)))
    edges = []
    for r in range(rows):
        for c in range(r + 1):
            if c + 1 <= r:
                edges.append((idx[(r, c)], idx[(r, c + 1)]))
            if r + 1 < rows:
                edges.append((idx[(r, c)], idx[(r + 1, c)]))
                edges.append((idx[(r, c)], idx[(r + 1, c + 1)]))
    return nodes, edges


def zigzag_rings(n: int, outer_r: float, inner_r: float, cx: float = 0.0, cy: float = 0.0):
    """Two concentric n-gons joined by a zigzag; every dot has four lines."""
    outer = ring(n, outer_r, cx, cy)
    inner = ring(n, inner_r, cx, cy, rot=-90 + 180 / n)
    edges = cycle(list(range(n))) + cycle(list(range(n, 2 * n)))
    for i in range(n):
        edges += [(i, n + i), (i, n + (i - 1) % n)]
    return outer + inner, edges


def pentacle(r: float = 1.0, cx: float = 0.0, cy: float = 0.0, rot: float = -90.0):
    """Pentagon plus pentagram: five dots with four lines each."""
    nodes = ring(5, r, cx, cy, rot)
    return nodes, cycle(list(range(5))) + [(i, (i + 2) % 5) for i in range(5)]


def hexagon_with_triangle(r: float = 1.0, cx: float = 0.0, cy: float = 0.0, tri=(0, 2, 4)):
    nodes = ring(6, r, cx, cy)
    return nodes, cycle(list(range(6))) + cycle(list(tri))


def star_ring(n: int, k: int, r: float = 1.0):
    """n-gon plus the {n/k} star polygon."""
    nodes = ring(n, r)
    return nodes, cycle(list(range(n))) + [(i, (i + k) % n) for i in range(n)]
