#!/usr/bin/env python3
"""Generate verified correct Kotlin code for levels 30-50."""
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


def verify(name, nodes, edges):
    deg = compute_degrees(nodes, edges)
    odd = odd_nodes(nodes, edges)
    conn = is_connected(nodes, edges)
    dups = has_dups(edges)
    ok = len(odd) in (0, 2) and conn and not dups
    if not ok:
        print(f"FAIL {name}: odd={odd}, conn={conn}, dups={dups}, deg={deg}")
    else:
        kind = "Circuit" if len(odd) == 0 else f"Path({odd})"
        print(f"OK {name}: {len(nodes)}n, {len(edges)}e, {kind}")
    return ok, odd, deg


nodes_30 = list(range(8))
edges_30 = [(0,1),(1,2),(2,3),(3,0),(0,4),(4,5),(5,1),(0,6),(1,6),(6,2),(6,3),(3,7),(7,2),(6,7)]
ok30, odd30, deg30 = verify("Level 30", nodes_30, edges_30)
print(f"  Level 30 odd nodes: {odd30}")

nodes_31 = list(range(9))
edges_31 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,0),(0,3),(3,6),(6,0)]
ok31, odd31, deg31 = verify("Level 31", nodes_31, edges_31)

nodes_32 = list(range(10))
edges_32 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,0),(0,3),(3,7),(7,0)]
ok32, odd32, deg32 = verify("Level 32", nodes_32, edges_32)

nodes_33 = list(range(10))
edges_33 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,0),(1,4),(4,7),(7,1),(0,5)]
ok33, odd33, deg33 = verify("Level 33", nodes_33, edges_33)

nodes_34 = list(range(9))
edges_34 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,0),(0,3),(3,6),(6,0),(1,4),(4,7),(7,1)]
ok34, odd34, deg34 = verify("Level 34", nodes_34, edges_34)

nodes_35 = list(range(10))
edges_35 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,0),(0,4),(4,8),(8,0),(2,7)]
ok35, odd35, deg35 = verify("Level 35", nodes_35, edges_35)
print(f"  Level 35 odd nodes: {odd35}")

nodes_36 = list(range(10))
edges_36 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,0),(0,3),(3,6),(6,0),(1,5),(5,8),(8,1)]
ok36, odd36, deg36 = verify("Level 36", nodes_36, edges_36)

nodes_37 = list(range(10))
edges_37 = [(0,2),(0,3),(2,3),(2,4),(3,5),(4,5),(4,6),(5,7),(6,7),(6,1),(7,1),(4,8),(8,6),(5,9),(9,7),(2,5),(3,4)]
ok37, odd37, deg37 = verify("Level 37", nodes_37, edges_37)
print(f"  Level 37 odd nodes: {odd37}")

nodes_38 = list(range(11))
edges_38 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),(0,4),(4,8),(8,0)]
ok38, odd38, deg38 = verify("Level 38", nodes_38, edges_38)

nodes_39 = list(range(11))
edges_39 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),(0,4),(4,8),(8,0),(2,6)]
ok39, odd39, deg39 = verify("Level 39", nodes_39, edges_39)
print(f"  Level 39 odd nodes: {odd39}")

nodes_40 = list(range(11))
edges_40 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),(0,4),(4,8),(8,0),(1,5),(5,9),(9,1)]
ok40, odd40, deg40 = verify("Level 40", nodes_40, edges_40)

nodes_41 = list(range(11))
edges_41 = [(0,2),(0,3),(2,3),(2,4),(3,5),(4,5),(4,6),(5,7),(6,7),(6,8),(7,9),(8,9),(8,1),(9,1),(4,10),(10,5),(10,2),(10,3),(6,9)]
ok41, odd41, deg41 = verify("Level 41", nodes_41, edges_41)
print(f"  Level 41 odd nodes: {odd41}")

nodes_42 = list(range(12))
edges_42 = [(0,1),(0,3),(0,5),(0,6),(1,7),(7,2),(2,8),(8,3),(3,11),(11,4),(4,9),(9,5),(5,10),(10,6),(6,1),(1,2),(2,3),(4,5),(0,2),(0,4)]
ok42, odd42, deg42 = verify("Level 42", nodes_42, edges_42)
print(f"  Level 42 odd nodes: {odd42}")

nodes_43 = list(range(12))
edges_43 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,0),(0,4),(4,8),(8,0),(2,6),(6,10),(10,2),(1,7)]
ok43, odd43, deg43 = verify("Level 43", nodes_43, edges_43)
print(f"  Level 43 odd nodes: {odd43}")

nodes_44 = list(range(11))
edges_44 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,0),(0,4),(4,8),(8,0),(1,5),(5,9),(9,1),(2,6),(6,10),(10,2)]
ok44, odd44, deg44 = verify("Level 44", nodes_44, edges_44)

nodes_45 = list(range(12))
edges_45 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,0),(0,4),(4,8),(8,0),(2,6),(6,10),(10,2),(3,9)]
ok45, odd45, deg45 = verify("Level 45", nodes_45, edges_45)
print(f"  Level 45 odd nodes: {odd45}")

nodes_46 = list(range(13))
edges_46 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,0),(0,4),(4,8),(8,0),(1,5),(5,9),(9,1),(2,7),(7,11),(11,2)]
ok46, odd46, deg46 = verify("Level 46", nodes_46, edges_46)

nodes_47 = list(range(13))
edges_47 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,0),(0,4),(4,8),(8,0),(1,5),(5,9),(9,1),(3,7),(7,11),(11,3),(2,10)]
ok47, odd47, deg47 = verify("Level 47", nodes_47, edges_47)
print(f"  Level 47 odd nodes: {odd47}")

nodes_48 = list(range(14))
edges_48 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,0),(0,5),(5,10),(10,0),(1,6),(6,11),(11,1),(3,8),(8,13),(13,3)]
ok48, odd48, deg48 = verify("Level 48", nodes_48, edges_48)

nodes_49 = list(range(14))
edges_49 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,0),(0,5),(5,10),(10,0),(1,6),(6,11),(11,1),(3,8),(8,13),(13,3),(2,9)]
ok49, odd49, deg49 = verify("Level 49", nodes_49, edges_49)
print(f"  Level 49 odd nodes: {odd49}")

nodes_50 = list(range(15))
edges_50 = [(0,1),(1,2),(2,3),(3,4),(4,5),(5,6),(6,7),(7,8),(8,9),(9,10),(10,11),(11,12),(12,13),(13,14),(14,0),(0,5),(5,10),(10,0),(1,6),(6,11),(11,1),(2,7),(7,12),(12,2),(3,8),(8,13),(13,3),(4,9),(9,14),(14,4)]
ok50, odd50, deg50 = verify("Level 50", nodes_50, edges_50)

all_ok = all([ok30, ok31, ok32, ok33, ok34, ok35, ok36, ok37, ok38, ok39,
              ok40, ok41, ok42, ok43, ok44, ok45, ok46, ok47, ok48, ok49, ok50])
print(f"\n{'ALL PASS' if all_ok else 'SOME FAILED'}")
