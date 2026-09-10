#!/usr/bin/env python3
"""Verify all levels, print a difficulty table, render previews and
optionally regenerate app/src/main/java/app/curious/lineflow/Graph.kt.

    python3 .scripts/leveldesign/build.py            # check + table
    python3 .scripts/leveldesign/build.py --png out  # also render previews
    python3 .scripts/leveldesign/build.py --write    # also rewrite Graph.kt
"""
from __future__ import annotations

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from catalog import build  # noqa: E402
from geometry import check, metrics  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
GRAPH_KT = os.path.join(ROOT, "app", "src", "main", "java", "app", "curious", "lineflow", "Graph.kt")


def difficulty_score(m: dict) -> float:
    """Random-walk failure rate plus line count scaled so 40 lines add 1.0.

    Only used for the printed table; ordering itself is done in the catalog.
    """
    return m["fail_rate"] + m["edges"] / 40.0


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--png", help="directory to write preview sheets into")
    ap.add_argument("--write", action="store_true", help="rewrite Graph.kt")
    args = ap.parse_args()

    levels = build()
    bad = 0
    print(f"{'#':>2} {'name':18} {'n':>2} {'e':>2} odd deg  fail  score  aspect  dp/unit")
    prev_edges = 0
    for lv in levels:
        problems = check(lv)
        m = metrics(lv)
        score = difficulty_score(m)
        # Never fewer lines than the level before: that is the drop players notice.
        if m["edges"] < prev_edges:
            problems.append(f"fewer lines than the previous level ({m['edges']} < {prev_edges})")
        prev_edges = m["edges"]
        flag = ""
        if problems:
            bad += 1
            flag = "  <-- " + "; ".join(problems[:4])
        print(f"{lv.id:>2} {lv.name:18} {m['nodes']:>2} {m['edges']:>2} {m['odd']:>3} {m['max_deg']:>3} "
              f"{m['fail_rate']:5.2f} {score:6.2f} {m['aspect']:7.2f} {m['scale_dp']:8.0f}{flag}")
    print(f"\n{len(levels)} levels, {bad} with problems")
    if len(levels) != 50:
        print("expected exactly 50 levels")
        bad += 1

    if args.png:
        from render import contact_sheet
        os.makedirs(args.png, exist_ok=True)
        for start in range(0, len(levels), 25):
            chunk = levels[start:start + 25]
            path = os.path.join(args.png, f"levels_{chunk[0].id:02d}_{chunk[-1].id:02d}.png")
            contact_sheet(chunk, path)
            print("wrote", path)

    if args.write:
        if bad:
            print("refusing to write Graph.kt while levels have problems")
            return 1
        from codegen import render_graph_kt
        with open(GRAPH_KT, "w") as f:
            f.write(render_graph_kt(levels))
        print("wrote", GRAPH_KT)
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
