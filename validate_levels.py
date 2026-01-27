#!/usr/bin/env python3
"""Validate all levels in Graph.kt for Eulerian path correctness."""
import re
import sys
from collections import defaultdict, deque

def parse_levels(filepath):
    with open(filepath) as f:
        content = f.read()

    levels = []
    # Find each Level block
    level_pattern = re.compile(
        r'Level\(\s*id\s*=\s*(\d+),\s*name\s*=\s*"([^"]+)"',
        re.DOTALL
    )

    # Split by Level( to process each
    parts = content.split('Level(')

    for part in parts[1:]:  # skip before first Level
        # Get id and name
        id_match = re.search(r'id\s*=\s*(\d+)', part)
        name_match = re.search(r'name\s*=\s*"([^"]+)"', part)
        if not id_match or not name_match:
            continue

        level_id = int(id_match.group(1))
        name = name_match.group(1)

        # Get nodes - find the nodes listOf block (greedy within the nodes section)
        nodes_section = re.search(r'nodes\s*=\s*listOf\(([\s\S]*?)\)\s*,\s*edges', part)
        if not nodes_section:
            continue
        node_ids = [int(x) for x in re.findall(r'Node\(\s*(\d+)', nodes_section.group(1))]

        # Get edges
        edges_section = re.search(r'edges\s*=\s*listOf\(([\s\S]*?)\)\s*,\s*hint', part)
        if not edges_section:
            continue
        edge_pairs = re.findall(r'Edge\(\s*(\d+)\s*,\s*(\d+)\s*\)', edges_section.group(1))
        edges = [(int(a), int(b)) for a, b in edge_pairs]

        # Get hint
        valid_starts_match = re.search(r'validStartNodeIds\s*=\s*listOf\(([\d\s,]+)\)', part)
        first_edge_match = re.search(r'firstEdge\s*=\s*Pair\(\s*(\d+)\s*,\s*(\d+)\s*\)', part)

        valid_starts = []
        if valid_starts_match:
            valid_starts = [int(x.strip()) for x in valid_starts_match.group(1).split(',') if x.strip()]

        first_edge = None
        if first_edge_match:
            first_edge = (int(first_edge_match.group(1)), int(first_edge_match.group(2)))

        levels.append({
            'id': level_id,
            'name': name,
            'nodes': node_ids,
            'edges': edges,
            'valid_starts': valid_starts,
            'first_edge': first_edge,
        })

    return levels

def validate_level(level):
    issues = []
    lid = level['id']
    name = level['name']
    nodes = set(level['nodes'])
    edges = level['edges']
    valid_starts = set(level['valid_starts'])
    first_edge = level['first_edge']

    # Check edges reference valid nodes
    for a, b in edges:
        if a not in nodes:
            issues.append(f"Edge ({a},{b}) references invalid node {a}")
        if b not in nodes:
            issues.append(f"Edge ({a},{b}) references invalid node {b}")

    # Check for duplicate edges
    edge_set = set()
    for a, b in edges:
        key = (min(a, b), max(a, b))
        if key in edge_set:
            issues.append(f"Duplicate edge ({a},{b})")
        edge_set.add(key)

    # Compute degrees
    degree = defaultdict(int)
    for n in nodes:
        degree[n] = 0
    for a, b in edges:
        degree[a] += 1
        degree[b] += 1

    odd_nodes = {n for n in nodes if degree[n] % 2 != 0}
    odd_count = len(odd_nodes)

    if odd_count != 0 and odd_count != 2:
        issues.append(f"Has {odd_count} odd-degree nodes (need 0 or 2): {sorted(odd_nodes)}")
        issues.append(f"  Degrees: {dict(sorted(degree.items()))}")

    # Check connectivity (BFS)
    adj = defaultdict(set)
    for a, b in edges:
        adj[a].add(b)
        adj[b].add(a)

    if nodes:
        start = min(nodes)
        visited = set()
        queue = deque([start])
        visited.add(start)
        while queue:
            curr = queue.popleft()
            for neighbor in adj[curr]:
                if neighbor not in visited:
                    visited.add(neighbor)
                    queue.append(neighbor)

        unreachable = nodes - visited
        if unreachable:
            issues.append(f"Not connected. Unreachable nodes: {sorted(unreachable)}")

    # Check hint valid starts
    is_circuit = (odd_count == 0)
    if is_circuit:
        if valid_starts != nodes:
            issues.append(f"Circuit but validStartNodeIds={sorted(valid_starts)} != all nodes {sorted(nodes)}")
    elif odd_count == 2:
        if valid_starts != odd_nodes:
            issues.append(f"Path: odd nodes={sorted(odd_nodes)} but validStartNodeIds={sorted(valid_starts)}")

    # Check first edge validity
    if first_edge:
        a, b = first_edge
        has_edge = any(
            (e[0] == a and e[1] == b) or (e[0] == b and e[1] == a)
            for e in edges
        )
        if not has_edge:
            issues.append(f"firstEdge ({a},{b}) not found in edges")

    return issues

def main():
    filepath = sys.argv[1] if len(sys.argv) > 1 else '/Users/curious/AndroidStudioProjects/LineFlow/app/src/main/java/com/example/lineflow/Graph.kt'
    levels = parse_levels(filepath)

    print(f"Found {len(levels)} levels\n")

    total_issues = 0
    for level in sorted(levels, key=lambda x: x['id']):
        issues = validate_level(level)
        if issues:
            print(f"Level {level['id']} ({level['name']}) - {len(level['nodes'])} nodes, {len(level['edges'])} edges:")
            for issue in issues:
                print(f"  ERROR: {issue}")
            print()
            total_issues += len(issues)
        else:
            print(f"Level {level['id']} ({level['name']}) - OK ({len(level['nodes'])} nodes, {len(level['edges'])} edges)")

    print(f"\nTotal: {len(levels)} levels, {total_issues} issues")
    return 1 if total_issues > 0 else 0

if __name__ == '__main__':
    sys.exit(main())
