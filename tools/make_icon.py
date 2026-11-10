"""Generates the Foothold icon as 32x32 pixel art, in the Ghost Mason / Ortho icon style.

Style rules (shared with Ghost Mason and Ortho): 32x32 grid scaled up with nearest neighbour,
dark rounded-square badge (#10161C) with 4-px stepped corners, a soft radial glow behind the
subject, a block character with a cute face, and cream 4-point sparkles.

Subject: the cube in Foothold's HUD gold, standing at the end of a block ledge over the void,
looking at the edge. A dashed gold "safety rail" rises from the ledge's edge (Edge Lock).

Usage: python tools/make_icon.py  -> media/icon.png (512), media/icon-128.png (128)
"""
import math
import os
import sys

from PIL import Image

N = 32
BG = (16, 22, 28, 255)
GLOW = (58, 46, 26)            # warm glow tint behind the cube

# cube (Foothold HUD gold, 0xFFC857)
TOP = (255, 230, 160, 255)
RIM = (255, 246, 214, 255)
LEFT = (255, 200, 87, 255)
RIGHT = (220, 156, 48, 255)
EDGE = (150, 96, 22, 255)
EYE = (40, 24, 8, 255)
EYE_HI = (255, 248, 230, 255)
# ledge blocks
LEDGE_TOP = (190, 196, 204, 255)
LEDGE = (124, 132, 142, 255)
LEDGE_SHADE = (92, 98, 108, 255)
SEAM = (62, 66, 74, 255)
# safety rail at the ledge edge
RAIL = (255, 200, 87)
SPARK = (255, 236, 140)

LEDGE_END = 22                 # first x past the ledge (the drop)
LEDGE_Y = 25                   # top row of the ledge


def blend(dst, src):
    """Alpha-composite src (r,g,b,a) over dst (r,g,b,a)."""
    sa = src[3] / 255
    da = dst[3] / 255
    oa = sa + da * (1 - sa)
    if oa == 0:
        return (0, 0, 0, 0)
    c = tuple(round((src[i] * sa + dst[i] * da * (1 - sa)) / oa) for i in range(3))
    return c + (round(oa * 255),)


def corner_cut(x, y):
    """Stepped rounded corners: 4,3,2,1 transparent pixels on rows 0..3 (mirrored)."""
    steps = [4, 3, 2, 1]
    for yy, xx in ((y, x), (N - 1 - y, x), (y, N - 1 - x), (N - 1 - y, N - 1 - x)):
        if yy < 4 and xx < steps[yy]:
            return True
    return False


def main(out_dir):
    px = [[(0, 0, 0, 0)] * N for _ in range(N)]

    def put(x, y, c):
        if 0 <= x < N and 0 <= y < N and not corner_cut(x, y):
            px[y][x] = blend(px[y][x], c) if c[3] < 255 else c

    # 1. badge + radial glow, centred on the cube
    for y in range(N):
        for x in range(N):
            if corner_cut(x, y):
                continue
            d = math.hypot(x + 0.5 - 14, y + 0.5 - 15) / 14
            t = max(0.0, 1 - d) ** 1.6
            c = tuple(round(BG[i] + (GLOW[i] - BG[i]) * t) for i in range(3))
            px[y][x] = c + (255,)

    # 2. ledge: side view of 4-px blocks from the left border to LEDGE_END
    for y in range(LEDGE_Y, N):
        for x in range(0, LEDGE_END):
            row = y - LEDGE_Y
            if row == 0:
                c = LEDGE_TOP
            elif (x + (2 if (row // 4) % 2 else 0)) % 4 == 3 or row % 4 == 0:
                c = SEAM                       # brick-offset block seams
            elif x == LEDGE_END - 1:
                c = LEDGE_SHADE                # shaded cliff face
            else:
                c = LEDGE
            put(x, y, c)

    # 3. dashed safety rail rising from the edge
    for y in range(9, LEDGE_Y):
        if (y - 9) % 3 != 2:
            a = round(110 + 145 * (y - 9) / (LEDGE_Y - 9))   # fades out upward
            put(LEDGE_END, y, RAIL + (a,))
    put(LEDGE_END, LEDGE_Y, RAIL + (255,))
    put(LEDGE_END + 1, LEDGE_Y, RAIL + (255,))

    # 4. isometric cube with a face, standing on the ledge near its edge
    xc, yc, W, H, F = 13, 13, 8, 4, 8
    for y in range(N):
        for x in range(N):
            fx, fy = x + 0.5, y + 0.5
            if abs(fx - xc) / W + abs(fy - yc) / H <= 1.0:
                put(x, y, TOP)
                continue
            if xc - W <= fx < xc:
                top_edge = yc + (fx - (xc - W)) / 2
                if top_edge <= fy < top_edge + F:
                    put(x, y, LEFT)
            elif xc <= fx < xc + W:
                top_edge = yc + ((xc + W) - fx) / 2
                if top_edge <= fy < top_edge + F:
                    put(x, y, RIGHT)
    for x in range(xc - W, xc + W):
        fx = x + 0.5
        top = yc + ((fx - (xc - W)) if fx < xc else ((xc + W) - fx)) / 2
        put(x, math.floor(top - 0.01), RIM)
    for y in range(yc + H, yc + H + F):
        put(xc, y, EDGE)
    # eyes on the right face, looking toward the edge
    for ex, ey in ((15, 18), (18, 16)):
        for dy in range(3):
            for dx in range(2):
                put(ex + dx, ey + dy, EYE)
        put(ex + 1, ey, EYE_HI)

    # 5. cream sparkles (shared brand detail)
    def sparkle(x, y, strong=True):
        put(x, y, SPARK + (255,))
        a = 200 if strong else 140
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            put(x + dx, y + dy, SPARK + (a,))

    sparkle(27, 5)
    sparkle(4, 5, strong=False)
    sparkle(28, 27, strong=False)

    img = Image.new("RGBA", (N, N))
    img.putdata([c for row in px for c in row])
    os.makedirs(out_dir, exist_ok=True)
    img.resize((512, 512), Image.NEAREST).save(os.path.join(out_dir, "icon.png"))
    img.resize((128, 128), Image.NEAREST).save(os.path.join(out_dir, "icon-128.png"))
    print("wrote", out_dir)


if __name__ == "__main__":
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    main(sys.argv[1] if len(sys.argv) > 1 else os.path.join(root, "media"))
