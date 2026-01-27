#!/usr/bin/env python3
"""Fix remaining broken levels."""
from collections import defaultdict, deque

def compute_degrees(nodes, edges):
    deg = defaultdict(int)
    for n in nodes:
        deg[n] = 0
    for a, b in edges:
        deg[a] += 1
        deg[b] += 1
    return dict(deg)

def odd_nodes(nodes, edges):
    deg = compute_degrees(nodes, edges)
    return sorted([n for n in nodes if deg[n] % 2 != 0])

def is_connected(nodes, edges):
    adj = defaultdict(set)
    for a, b in edges:
        adj[a].add(b)
        adj[b].add(a)
    visited = set()
    queue = deque([nodes[0]])
    visited.add(nodes[0])
    while queue:
        curr = queue.popleft()
        for n in adj[curr]:
            if n not in visited:
                visited.add(n)
                queue.append(n)
    return len(visited) == len(nodes)

def has_dups(edges):
    s = set()
    for a, b in edges:
        k = (min(a, b), max(a, b))
        if k in s:
            return True
        s.add(k)
    return False

def check(name, nodes, edges):
    deg = compute_degrees(nodes, edges)
    odd = odd_nodes(nodes, edges)
    conn = is_connected(nodes, edges)
    dups = has_dups(edges)
    ok = len(odd) in (0, 2) and conn and not dups
    status = "OK" if ok else "FAIL"
    type_str = "Circuit" if len(odd) == 0 else f"Path({odd})"
    print(f"  {name}: {status} - {len(nodes)}n, {len(edges)}e, {type_str}, conn={conn}, dups={dups}")
    if not ok:
        print(f"    Degrees: {deg}")
    return ok, odd

# Level 21: Wheel — 7 nodes
# Ring 0-5 (hexagon) + center 6 connecting to all + 2 diameters
# Ring: 0-1,1-2,2-3,3-4,4-5,5-0 (each node deg 2)
# Spokes: 0-6,1-6,2-6,3-6,4-6,5-6 (center=6, each ring+1=3)
# Diameters: 0-3, 1-4 (those nodes +1 = 4, 2,5 still 3)
# Result: 0=4,1=4,2=3,3=4,4=4,5=3,6=6. Odd: 2,5.
# To make circuit: also add 2-5: 2=4,5=4. All even! 6+6+2+1=15 edges.
nodes_21 = list(range(7))
edges_21 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,0),  # ring
            (0,6),(1,6),(2,6),(3,6),(4,6),(5,6),  # spokes
            (0,3),(1,4),(2,5)]  # diameters
check("Level 21", nodes_21, edges_21)

# Level 22: Zigzag — 8 nodes
# Chain: 0-1-2-3-4-5-6-7 (7 edges, 0=1, 7=1, rest=2)
# Add cross-links making a zigzag ladder:
# 0-2, 1-3, 2-4, 3-5, 4-6, 5-7
# Each node except 0,7 gets +2. 0=1+1=2, 7=1+1=2. Others=2+2=4.
# All even! 7+6=13 edges. Circuit!
# But that might be too easy. Let's remove 5-7 to make path.
# Then: 5=2+1=3, 7=1. 2 odd (5,7). Hmm but 7 has deg 1.
# Actually: 0=1(chain)+1(0-2)=2. 1=2(chain)+1(1-3)=3. Hmm.
# Wait. Chain: 0-1(0:1,1:1),1-2(1:2,2:1),2-3(2:2,3:1),3-4(3:2,4:1),4-5(4:2,5:1),5-6(5:2,6:1),6-7(6:2,7:1)
# Cross: 0-2(0:2,2:3),1-3(1:3,3:3),2-4(2:4,4:3),3-5(3:4,5:3),4-6(4:4,6:3),5-7(5:4,7:2)
# Degrees: 0=2,1=3,2=4,3=4,4=4,5=4,6=3,7=2. Odd: 1,6. Path! 13 edges.
# With 5-7 removed: 0=2,1=3,2=4,3=4,4=4,5=3,6=3,7=1. 4 odd.
# Keep all 13 edges: odd = 1,6. Path. Good.
nodes_22 = list(range(8))
edges_22 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),  # chain
            (0,2),(1,3),(2,4),(3,5),(4,6),(5,7)]  # cross
check("Level 22", nodes_22, edges_22)

# Level 25: Tower — 8 nodes
# Ladder with crosses. 0-1 bottom, 2-3 mid, 4-5 upper, 6-7 top
# Rungs: 0-1,2-3,4-5,6-7 (4 edges)
# Left side: 0-2,2-4,4-6 (3 edges)
# Right side: 1-3,3-5,5-7 (3 edges)
# That's 10 edges. Degrees: 0=2,1=2,2=3,3=3,4=3,5=3,6=2,7=2 -> 4 odd (2,3,4,5). Bad.
# Add: 2-5,3-4 (crosses mid-upper): 2=4,3=4,4=4,5=4. All even! 12 edges. Circuit!
nodes_25 = list(range(8))
edges_25 = [(0,1),(2,3),(4,5),(6,7),  # rungs
            (0,2),(2,4),(4,6),  # left
            (1,3),(3,5),(5,7),  # right
            (2,5),(3,4)]  # crosses
check("Level 25", nodes_25, edges_25)

# Level 31: Compass — 9 nodes
# Inner square 1-2-3-4-1. Center 0 to all 4. Extensions: 1-5,5-2; 2-6,6-3; 3-7,7-4; 4-8,8-1
# That gives inner nodes: sq(2)+spoke(1)+ext_in(2)+ext_out(2)... let me recount:
# 1: sq(1-2,4-1=2 edges) + spoke(0-1) + ext(1-5, 8-1) = 5. Odd!
# Problem: each inner node gets sq(2)+spoke(1)+2 extension edges = 5.
# Fix: remove the spokes. Just inner square + extensions.
# 1-2,2-3,3-4,4-1 (square). 1-5,5-2,2-6,6-3,3-7,7-4,4-8,8-1 (extensions).
# 1=sq(2)+ext(2)=4. 2=sq(2)+ext(2)=4. Similar for 3,4. 5,6,7,8=ext(2)=2. Center 0 unconnected!
# Add: 0-5,0-6,0-7,0-8. Center=4. All nodes: 1-4=4, 5-8=2+1=3. Odd!
# Add: 0-1,0-2,0-3,0-4 instead. Center=4. 1-4=4+1=5. Odd!
# Use ONLY center spokes to inner + inner to extensions:
# Spokes: 0-1,0-2,0-3,0-4 (center=4, inner=1)
# Inner square: 1-2,2-3,3-4,4-1 (inner+2=3). Odd!
# Extensions as bypass: 1-5-3, 2-6-4 (making 2 triangles):
# 1-5,5-3,2-6,6-4. Inner: 1=3+1=4,3=3+1=4,2=3+1=4,4=3+1=4. 5=2,6=2. All even!
# Need 7,8 too. Add 7 between 3&1: 3-7,7-1. 3=4+1=5. BAD.
# Different approach: 9 nodes.
# 0(center). 1-4 inner square. 5-8 extensions on diagonals.
# Square: 1-2,2-3,3-4,4-1. Spokes: 0-1,0-2,0-3,0-4.
# Diags: 1-3,2-4 (crosses). 1=sq(2)+spoke(1)+diag(1)=4. All inner=4. Center=4.
# Extensions: 5 between 1&2: 1-5,5-2. 6 between 2&3: 2-6,6-3. 7: 3-7,7-4. 8: 4-8,8-1.
# Inner: 1=4+1(to5)+1(from8)=6. Too much.
# Let me just use extensions as leaf pairs:
# 0(center), 1-4(inner ring), 5-8(outer leaves)
# Ring: 1-2,2-3,3-4,4-1. Spokes: 0-1,0-2,0-3,0-4.
# Leaves: 1-5,5-0; 3-7,7-0 → 0=4+2=6, 1=4+1=5. BAD.
#
# Simple clean construction:
# 9-cycle: 0-1-2-3-4-5-6-7-8-0. Add chord triplets.
# {0,3,6}: 0-3,3-6,6-0. 0=4,3=4,6=4. Others=2. 12 edges. Circuit!
# But difficulty-wise, 9 nodes with 12 edges in a cycle-chord pattern is OK.
nodes_31 = list(range(9))
edges_31 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,0),  # 9-cycle
            (0,3),(3,6),(6,0)]  # chord triplet
check("Level 31", nodes_31, edges_31)

# Level 34: The Claw — 9 nodes
# 9-cycle + chord triplets.
# Cycle: 0-1-2-3-4-5-6-7-8-0.
# Triplet {0,3,6}: 0-3,3-6,6-0. Triplet {1,4,7}: 1-4,4-7,7-1.
# 0=4,3=4,6=4,1=4,4=4,7=4,2=2,5=2,8=2. All even! 9+6=15 edges.
nodes_34 = list(range(9))
edges_34 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,0),  # 9-cycle
            (0,3),(3,6),(6,0),  # triplet 1
            (1,4),(4,7),(7,1)]  # triplet 2
check("Level 34", nodes_34, edges_34)

# Level 35: Constellation — 10 nodes
# Same as before but add the 2-7 edge:
nodes_35 = list(range(10))
edges_35 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,0),  # octagon
            (0,8),(8,4),(0,4),  # inner star
            (8,9),(9,2),(7,8),(3,9),  # more structure
            (2,7)]  # fix edge
check("Level 35", nodes_35, edges_35)

print("\n=== Hint-only fixes ===")
# Level 30: recompute odd nodes
nodes_30 = list(range(8))
edges_30 = [(0,1),(1,2),(2,3),(3,0),(0,4),(4,5),(5,1),(0,6),(1,6),(6,2),(6,3),(3,7),(7,2),(6,7)]
deg_30 = compute_degrees(nodes_30, edges_30)
odd_30 = odd_nodes(nodes_30, edges_30)
print(f"Level 30: degrees={deg_30}, odd={odd_30}")

# Level 37:
nodes_37 = list(range(10))
edges_37 = [(0,2),(0,3),(2,3),(2,4),(3,5),(4,5),(4,6),(5,7),(6,7),(6,1),(7,1),(4,8),(8,6),(5,9),(9,7),(2,5),(3,4)]
deg_37 = compute_degrees(nodes_37, edges_37)
odd_37 = odd_nodes(nodes_37, edges_37)
print(f"Level 37: degrees={deg_37}, odd={odd_37}")

# Level 41:
nodes_41 = list(range(11))
edges_41 = [(0,2),(0,3),(2,3),(2,4),(3,5),(4,5),(4,6),(5,7),(6,7),(6,8),(7,9),(8,9),(8,1),(9,1),(4,10),(10,5),(10,2),(10,3),(6,9)]
deg_41 = compute_degrees(nodes_41, edges_41)
odd_41 = odd_nodes(nodes_41, edges_41)
print(f"Level 41: degrees={deg_41}, odd={odd_41}")

# Level 42:
nodes_42 = list(range(12))
edges_42 = [(0,1),(0,3),(0,5),(0,6),(1,7),(7,2),(2,8),(8,3),(3,11),(11,4),(4,9),(9,5),(5,10),(10,6),(6,1),(1,2),(2,3),(4,5),(0,2),(0,4)]
deg_42 = compute_degrees(nodes_42, edges_42)
odd_42 = odd_nodes(nodes_42, edges_42)
print(f"Level 42: degrees={deg_42}, odd={odd_42}")

# Level 49 (original): was circuit not path
nodes_49_orig = list(range(14))
edges_49_orig = [(0,2),(0,3),(2,4),(3,5),(4,6),(5,7),(6,8),(7,9),(8,1),(9,1),(2,10),(10,3),(10,12),(10,13),(12,6),(13,7),(12,11),(13,11),(11,8),(11,9),(4,12),(5,13),(2,3),(4,5),(6,7),(8,9)]
deg_49 = compute_degrees(nodes_49_orig, edges_49_orig)
odd_49 = odd_nodes(nodes_49_orig, edges_49_orig)
print(f"Level 49 (original): degrees={deg_49}, odd={odd_49}")
