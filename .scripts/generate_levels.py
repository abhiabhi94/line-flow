#!/usr/bin/env python3
"""Generate valid Eulerian graph levels for LineFlow."""
from collections import defaultdict, deque


def verify_level(level):
    """Verify a level is valid. Returns (is_valid, odd_nodes, issues)."""
    issues = []
    nodes = level['nodes']
    edges = level['edges']
    node_set = set(nodes)

    for a, b in edges:
        if a not in node_set:
            issues.append(f"Edge ({a},{b}) references invalid node {a}")
        if b not in node_set:
            issues.append(f"Edge ({a},{b}) references invalid node {b}")

    edge_set = set()
    for a, b in edges:
        key = (min(a, b), max(a, b))
        if key in edge_set:
            issues.append(f"Duplicate edge ({a},{b})")
        edge_set.add(key)

    degree = defaultdict(int)
    for n in nodes:
        degree[n] = 0
    for a, b in edges:
        degree[a] += 1
        degree[b] += 1

    odd_nodes = sorted([n for n in nodes if degree[n] % 2 != 0])
    odd_count = len(odd_nodes)

    if odd_count != 0 and odd_count != 2:
        issues.append(f"Has {odd_count} odd-degree nodes: {odd_nodes}")
        issues.append(f"  Degrees: {dict(sorted(degree.items()))}")

    adj = defaultdict(set)
    for a, b in edges:
        adj[a].add(b)
        adj[b].add(a)

    if nodes:
        start = nodes[0]
        visited = set()
        queue = deque([start])
        visited.add(start)
        while queue:
            curr = queue.popleft()
            for neighbor in adj[curr]:
                if neighbor not in visited:
                    visited.add(neighbor)
                    queue.append(neighbor)

        unreachable = node_set - visited
        if unreachable:
            issues.append(f"Not connected. Unreachable: {sorted(unreachable)}")

    return len(issues) == 0, odd_nodes, issues


def fmt_edges(edges):
    """Format edges as Kotlin code."""
    parts = []
    for a, b in edges:
        parts.append(f"Edge({a}, {b})")
    lines = []
    line = []
    for p in parts:
        line.append(p)
        if len(line) >= 4:
            lines.append(", ".join(line))
            line = []
    if line:
        lines.append(", ".join(line))
    return (",\n                ".join(lines))


levels_to_fix = []

levels_to_fix.append((19, "The Fish",
    [(0, 0.1, 0.5), (1, 0.3, 0.3), (2, 0.3, 0.7), (3, 0.55, 0.5),
     (4, 0.75, 0.3), (5, 0.75, 0.7), (6, 0.93, 0.5)],
    [(0,1), (0,2), (1,2), (1,3), (2,3), (3,4), (3,5), (4,6), (5,6)],
    "Swim through the fish. Start from a fin."))

levels_to_fix.append((20, "The Butterfly",
    [(0, 0.15, 0.15), (1, 0.15, 0.85), (2, 0.35, 0.5), (3, 0.5, 0.5),
     (4, 0.65, 0.5), (5, 0.85, 0.15), (6, 0.85, 0.85)],
    [(0,1), (1,2), (2,0), (0,3), (3,4), (4,5), (5,6), (6,3), (3,5)],
    "Two wings meet at the body. Start from a wing tip."))

levels_to_fix.append((21, "The Wheel",
    [(0, 0.5, 0.08), (1, 0.87, 0.28), (2, 0.87, 0.72), (3, 0.5, 0.92),
     (4, 0.13, 0.72), (5, 0.13, 0.28), (6, 0.5, 0.5)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,0),
     (0,6),(1,6),(2,6),(3,6),(4,6),(5,6),
     (0,3),(1,4)],
    "The hub connects all spokes. Start anywhere!"))

levels_to_fix.append((22, "The Zigzag",
    [(0, 0.1, 0.3), (1, 0.25, 0.7), (2, 0.38, 0.3), (3, 0.5, 0.7),
     (4, 0.62, 0.3), (5, 0.75, 0.7), (6, 0.88, 0.3), (7, 0.9, 0.7)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),
     (1,3),(2,4),(3,5),(4,6)],
    "Zigzag from one end to the other."))

levels_to_fix.append((24, "The Shield",
    [(0, 0.5, 0.08), (1, 0.9, 0.38), (2, 0.75, 0.88), (3, 0.25, 0.88),
     (4, 0.1, 0.38), (5, 0.55, 0.4), (6, 0.45, 0.65)],
    [(0,1),(1,2),(2,3),(3,4),(4,0),
     (5,0),(5,1),(5,2),(6,2),(6,3),(6,4),(5,6),
     (0,3),(1,4)],
    "A shield with hidden connections. Start anywhere!"))

levels_to_fix.append((25, "The Tower",
    [(0, 0.3, 0.88), (1, 0.7, 0.88), (2, 0.3, 0.6), (3, 0.7, 0.6),
     (4, 0.3, 0.35), (5, 0.7, 0.35), (6, 0.4, 0.08), (7, 0.6, 0.08)],
    [(0,1),(2,3),(4,5),(6,7),
     (0,2),(2,4),(4,6),(1,3),(3,5),(5,7),
     (0,3),(1,2),(4,7),(5,6)],
    "Build upward from the base. Start from the top."))

levels_to_fix.append((26, "The Pinwheel",
    [(0, 0.3, 0.2), (1, 0.7, 0.2), (2, 0.7, 0.5), (3, 0.3, 0.5),
     (4, 0.3, 0.55), (5, 0.7, 0.55), (6, 0.7, 0.85), (7, 0.3, 0.85),
     (8, 0.5, 0.5)],
    [(0,1),(1,2),(2,3),(3,0),
     (4,5),(5,6),(6,7),(7,4),
     (0,4),(1,5),(2,6),(3,7),
     (8,0),(8,2),(8,5),(8,7),
     (1,6),(3,4)],
    "Spin around the center. Start anywhere!"))

levels_to_fix.append((27, "The Scales",
    [(0, 0.5, 0.1), (1, 0.5, 0.4), (2, 0.2, 0.55), (3, 0.8, 0.55),
     (4, 0.08, 0.8), (5, 0.32, 0.8), (6, 0.68, 0.8), (7, 0.92, 0.8)],
    [(0,1),(1,2),(1,3),(0,2),(0,3),
     (2,4),(4,5),(5,2),(3,6),(6,7),(7,3)],
    "Balance both sides. Start from the top."))

levels_to_fix.append((28, "The Maze",
    [(0, 0.15, 0.15), (1, 0.85, 0.15), (2, 0.15, 0.5), (3, 0.85, 0.5),
     (4, 0.5, 0.5), (5, 0.3, 0.85), (6, 0.7, 0.85), (7, 0.5, 0.95)],
    [(0,1),(0,2),(1,3),(2,4),(3,4),(4,5),(4,6),(5,7),(6,7),(2,3)],
    "Navigate from one side to the other."))

levels_to_fix.append(("fix_hint_only", 30, [6, 7]))

levels_to_fix.append((31, "The Compass",
    [(0, 0.5, 0.5), (1, 0.5, 0.1), (2, 0.9, 0.5), (3, 0.5, 0.9),
     (4, 0.1, 0.5), (5, 0.8, 0.2), (6, 0.8, 0.8), (7, 0.2, 0.8),
     (8, 0.2, 0.2)],
    [(1,2),(2,3),(3,4),(4,1),
     (0,1),(0,2),(0,3),(0,4),
     (1,5),(5,2),(2,6),(6,3),(3,7),(7,4),(4,8),(8,1)],
    "Eight directions from the center. Start anywhere!"))

levels_to_fix.append((32, "The Pyramid",
    [(0, 0.5, 0.05), (1, 0.93, 0.35), (2, 0.77, 0.9), (3, 0.23, 0.9),
     (4, 0.07, 0.35), (5, 0.5, 0.3), (6, 0.73, 0.45), (7, 0.64, 0.73),
     (8, 0.36, 0.73), (9, 0.27, 0.45)],
    [(0,1),(1,2),(2,3),(3,4),(4,0),
     (5,6),(6,7),(7,8),(8,9),(9,5),
     (0,5),(1,6),(2,7),(3,8),(4,9),
     (0,7),(1,8),(2,9),(3,5)],
    "Two pentagons intertwined. Start from node 4 or 6."))

levels_to_fix.append((33, "The Labyrinth",
    [(0, 0.5, 0.05), (1, 0.9, 0.2), (2, 0.97, 0.58), (3, 0.8, 0.92),
     (4, 0.5, 0.97), (5, 0.2, 0.92), (6, 0.03, 0.58), (7, 0.1, 0.2),
     (8, 0.35, 0.4), (9, 0.65, 0.4)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,0),
     (8,9),(0,8),(0,9),
     (4,8),(4,9),
     (7,8),(2,9)],
    "Find your way through the twisting paths."))

levels_to_fix.append((34, "The Claw",
    [(0, 0.5, 0.5), (1, 0.5, 0.08), (2, 0.85, 0.2), (3, 0.92, 0.55),
     (4, 0.75, 0.88), (5, 0.35, 0.88), (6, 0.08, 0.68), (7, 0.08, 0.3),
     (8, 0.3, 0.12)],
    [(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,1),
     (0,1),(0,3),(0,5),(0,7),
     (1,3),(3,5),(5,7),(7,1)],
    "Grip from the center outward. Start anywhere!"))

levels_to_fix.append((35, "The Constellation",
    [(0, 0.5, 0.05), (1, 0.82, 0.18), (2, 0.95, 0.52), (3, 0.78, 0.85),
     (4, 0.5, 0.95), (5, 0.22, 0.85), (6, 0.05, 0.52), (7, 0.18, 0.18),
     (8, 0.35, 0.42), (9, 0.65, 0.42)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,0),
     (0,8),(8,4),(0,4),
     (8,9),(9,2),(7,8),(3,9)],
    "Stars scattered across the sky."))

levels_to_fix.append((36, "The Vortex",
    [(0, 0.5, 0.05), (1, 0.82, 0.15), (2, 0.95, 0.45), (3, 0.85, 0.78),
     (4, 0.6, 0.95), (5, 0.3, 0.92), (6, 0.1, 0.72), (7, 0.05, 0.4),
     (8, 0.2, 0.15), (9, 0.4, 0.08)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,0),
     (0,3),(3,5),(5,0),
     (1,4),(4,7),(7,1)],
    "Spiral into the vortex. Start anywhere!"))

levels_to_fix.append(("fix_hint_only", 37, [4, 5]))

levels_to_fix.append((38, "The Cobweb",
    [(0, 0.5, 0.05), (1, 0.82, 0.12), (2, 0.97, 0.42), (3, 0.88, 0.77),
     (4, 0.62, 0.95), (5, 0.35, 0.95), (6, 0.1, 0.77), (7, 0.03, 0.42),
     (8, 0.18, 0.12), (9, 0.35, 0.35), (10, 0.65, 0.35)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),
     (0,4),(4,8),(8,0)],
    "The spider waits at the center of its web. Start anywhere!"))

levels_to_fix.append((39, "The Clockwork",
    [(0, 0.5, 0.08), (1, 0.83, 0.22), (2, 0.95, 0.55), (3, 0.8, 0.88),
     (4, 0.5, 0.97), (5, 0.2, 0.88), (6, 0.05, 0.55), (7, 0.17, 0.22),
     (8, 0.32, 0.12), (9, 0.5, 0.45), (10, 0.68, 0.42)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),
     (0,3),(3,7),(7,0),
     (1,5),(5,9),(9,1),
     (2,10)],
    "Gears turn around the center."))

levels_to_fix.append((40, "The Citadel",
    [(0, 0.5, 0.08), (1, 0.8, 0.15), (2, 0.95, 0.42), (3, 0.9, 0.72),
     (4, 0.68, 0.92), (5, 0.38, 0.92), (6, 0.12, 0.72), (7, 0.05, 0.42),
     (8, 0.2, 0.15), (9, 0.35, 0.35), (10, 0.65, 0.35)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),
     (0,4),(4,8),(8,0),
     (2,6),(6,10),(10,2)],
    "Fortified walls with inner passages. Start anywhere!"))

levels_to_fix.append(("fix_hint_only", 41, [7, 8]))
levels_to_fix.append(("fix_hint_only", 42, [2, 6]))

levels_to_fix.append((43, "The Kraken",
    [(0, 0.5, 0.08), (1, 0.78, 0.12), (2, 0.95, 0.35), (3, 0.92, 0.62),
     (4, 0.72, 0.88), (5, 0.45, 0.95), (6, 0.2, 0.85), (7, 0.05, 0.6),
     (8, 0.08, 0.32), (9, 0.25, 0.12), (10, 0.42, 0.3), (11, 0.62, 0.32)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,0),
     (0,4),(4,8),(8,0),
     (2,6),(6,10),(10,2),
     (1,7)],
    "Tentacles reach in all directions."))

levels_to_fix.append((44, "The Nexus",
    [(0, 0.5, 0.05), (1, 0.82, 0.15), (2, 0.97, 0.45), (3, 0.88, 0.78),
     (4, 0.62, 0.95), (5, 0.35, 0.95), (6, 0.1, 0.78), (7, 0.03, 0.45),
     (8, 0.18, 0.15), (9, 0.38, 0.3), (10, 0.65, 0.28)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),
     (0,4),(4,7),(7,0),
     (1,5),(5,9),(9,1),
     (2,6),(6,10),(10,2)],
    "Everything connects through the nexus. Start anywhere!"))

levels_to_fix.append((45, "The Serpent",
    [(0, 0.08, 0.5), (1, 0.15, 0.25), (2, 0.3, 0.1), (3, 0.48, 0.12),
     (4, 0.65, 0.2), (5, 0.78, 0.35), (6, 0.88, 0.52), (7, 0.85, 0.72),
     (8, 0.7, 0.85), (9, 0.5, 0.92), (10, 0.3, 0.85), (11, 0.12, 0.7)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,0),
     (0,4),(4,8),(8,0),
     (2,6),(6,10),(10,2),
     (3,9)],
    "Slither from one end to the other."))

levels_to_fix.append((46, "The Phoenix",
    [(0, 0.5, 0.03), (1, 0.75, 0.08), (2, 0.93, 0.25), (3, 0.97, 0.5),
     (4, 0.85, 0.75), (5, 0.65, 0.93), (6, 0.38, 0.93), (7, 0.18, 0.75),
     (8, 0.05, 0.5), (9, 0.08, 0.25), (10, 0.28, 0.08), (11, 0.42, 0.35),
     (12, 0.6, 0.6)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,0),
     (0,5),(5,9),(9,0),
     (2,7),(7,11),(11,2),
     (4,8),(8,12),(12,4)],
    "Rise from the ashes. Start anywhere!"))

levels_to_fix.append((47, "The Tempest",
    [(0, 0.5, 0.05), (1, 0.78, 0.1), (2, 0.95, 0.3), (3, 0.95, 0.58),
     (4, 0.8, 0.82), (5, 0.58, 0.95), (6, 0.32, 0.95), (7, 0.12, 0.8),
     (8, 0.03, 0.55), (9, 0.05, 0.28), (10, 0.2, 0.1), (11, 0.4, 0.32),
     (12, 0.6, 0.42)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,0),
     (0,4),(4,9),(9,0),
     (1,6),(6,10),(10,1),
     (3,7),(7,12),(12,3)],
    "The storm rages from all directions. Start anywhere!"))

levels_to_fix.append((48, "The Leviathan",
    [(0, 0.5, 0.03), (1, 0.72, 0.06), (2, 0.9, 0.18), (3, 0.97, 0.38),
     (4, 0.95, 0.6), (5, 0.82, 0.8), (6, 0.62, 0.93), (7, 0.4, 0.95),
     (8, 0.2, 0.85), (9, 0.07, 0.65), (10, 0.03, 0.42), (11, 0.1, 0.2),
     (12, 0.28, 0.08), (13, 0.42, 0.28)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,0),
     (0,5),(5,10),(10,0),
     (2,7),(7,12),(12,2),
     (3,9),(9,13),(13,3)],
    "The great beast stretches across the deep. Start anywhere!"))

levels_to_fix.append((49, "The Colossus",
    [(0, 0.5, 0.03), (1, 0.72, 0.06), (2, 0.9, 0.18), (3, 0.97, 0.4),
     (4, 0.95, 0.62), (5, 0.82, 0.82), (6, 0.62, 0.95), (7, 0.38, 0.95),
     (8, 0.18, 0.82), (9, 0.05, 0.62), (10, 0.03, 0.4), (11, 0.1, 0.2),
     (12, 0.28, 0.08), (13, 0.42, 0.32)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,0),
     (0,5),(5,10),(10,0),
     (2,7),(7,12),(12,2),
     (3,9),(9,13),(13,3),
     (1,8)],
    "The giant stands from crown to feet."))

levels_to_fix.append((50, "The Cosmos",
    [(0, 0.5, 0.03), (1, 0.7, 0.05), (2, 0.87, 0.15), (3, 0.97, 0.32),
     (4, 0.98, 0.52), (5, 0.9, 0.72), (6, 0.75, 0.88), (7, 0.55, 0.97),
     (8, 0.35, 0.95), (9, 0.18, 0.85), (10, 0.05, 0.68), (11, 0.02, 0.48),
     (12, 0.08, 0.28), (13, 0.22, 0.12), (14, 0.4, 0.05)],
    [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,14),(14,0),
     (0,5),(5,10),(10,0),
     (1,6),(6,11),(11,1),
     (2,7),(7,12),(12,2),
     (3,8),(8,13),(13,3),
     (4,9),(9,14),(14,4)],
    "The universe unfolds in all directions. Start anywhere!"))

print("Verifying generated levels:")
all_ok = True
for entry in levels_to_fix:
    if entry[0] == "fix_hint_only":
        continue
    lid, name, nodes_pos, edges, hint_text = entry
    node_ids = list(range(len(nodes_pos)))
    ok, odd_nodes_list, issues = verify_level({'id': lid, 'name': name, 'nodes': node_ids, 'edges': edges, 'valid_starts': [], 'first_edge': None})
    is_circuit = len(odd_nodes_list) == 0
    if issues:
        print(f"  Level {lid} ({name}): ISSUES!")
        for i in issues:
            print(f"    {i}")
        all_ok = False
    else:
        type_str = "Circuit" if is_circuit else f"Path (odd: {odd_nodes_list})"
        print(f"  Level {lid} ({name}): OK - {len(node_ids)} nodes, {len(edges)} edges, {type_str}")

if all_ok:
    print("\nAll generated levels are valid!")
else:
    print("\nSome levels have issues!")
