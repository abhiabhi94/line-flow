"""Renders levels to a PNG contact sheet so layouts can be eyeballed.

Each tile mimics the in-game play area (PLAY_WIDTH_DP x PLAY_HEIGHT_DP) so
what you see is what a small phone shows. Requires Pillow.
"""
from __future__ import annotations

from PIL import Image, ImageDraw

from geometry import HIT_RADIUS_DP, PLAY_HEIGHT_DP, PLAY_WIDTH_DP, LevelSpec, metrics, solve

BG = (13, 13, 15)
EDGE = (74, 63, 107)
NODE = (108, 92, 231)
ODD = (255, 217, 61)
HIT = (30, 30, 40)
TEXT = (200, 200, 210)
FIRST = (34, 211, 238)


def draw_level(spec: LevelSpec, tile_w: int, tile_h: int, px_per_dp: float, show_ids=True) -> Image.Image:
    img = Image.new("RGB", (tile_w, tile_h), BG)
    d = ImageDraw.Draw(img)
    pad = 12
    play_w = PLAY_WIDTH_DP * px_per_dp
    play_h = PLAY_HEIGHT_DP * px_per_dp
    ox = (tile_w - play_w) / 2
    oy = pad + 28
    d.rectangle([ox, oy, ox + play_w, oy + play_h], outline=(40, 40, 50))

    x0, y0, x1, y1 = spec.bbox()
    w, h = max(x1 - x0, 1e-6), max(y1 - y0, 1e-6)
    s = min(play_w / w, play_h / h)
    cx = ox + (play_w - w * s) / 2
    cy = oy + (play_h - h * s) / 2

    def P(p):
        return (cx + (p[0] - x0) * s, cy + (p[1] - y0) * s)

    pts = [P(p) for p in spec.nodes]
    r_hit = HIT_RADIUS_DP * px_per_dp
    for p in pts:
        d.ellipse([p[0] - r_hit, p[1] - r_hit, p[0] + r_hit, p[1] + r_hit], fill=HIT)
    try:
        start, first, _ = solve(spec)
    except ValueError:
        start, first = None, None
    for a, b in spec.edges:
        col = FIRST if {a, b} == {start, first} else EDGE
        d.line([pts[a], pts[b]], fill=col, width=max(2, int(4 * px_per_dp)))
    odd = set(spec.odd_nodes())
    r = 12 * px_per_dp
    for i, p in enumerate(pts):
        col = ODD if i in odd else NODE
        d.ellipse([p[0] - r, p[1] - r, p[0] + r, p[1] + r], fill=col)
        if show_ids:
            d.text((p[0] - 3, p[1] - 5), str(i), fill=(0, 0, 0))
    m = metrics(spec)
    d.text((pad, 4), f"{spec.id}. {spec.name}", fill=TEXT)
    d.text((pad, 16), f"n={m['nodes']} e={m['edges']} odd={m['odd']} fail={m['fail_rate']:.2f}", fill=TEXT)
    return img


def contact_sheet(specs: list[LevelSpec], path: str, cols: int = 5, px_per_dp: float = 0.5):
    tile_w = int(PLAY_WIDTH_DP * px_per_dp) + 24
    tile_h = int(PLAY_HEIGHT_DP * px_per_dp) + 52
    rows = (len(specs) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * tile_w, rows * tile_h), BG)
    for i, spec in enumerate(specs):
        tile = draw_level(spec, tile_w, tile_h, px_per_dp)
        sheet.paste(tile, ((i % cols) * tile_w, (i // cols) * tile_h))
    sheet.save(path)
    return path
