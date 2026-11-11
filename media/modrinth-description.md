**Build high without falling, bridge without gaps.** Foothold adds three small helpers for builders, roofers and bridgers: stop at ledges without sneaking, place blocks without gaps while you hold right-click, and lock your camera angle.

Everything happens on your own computer: Foothold needs nothing on the server.

## Edge lock (V)

Press **V** and you stop at ledges as if you were sneaking, but you keep walking at full speed and see over the edge normally.

- **Only real drops.** By default it only holds you at drops of 2 blocks or more, so you can still walk down single steps and stairs. Set it to 1 to stop at every ledge, like sneaking does.
- **Step off on purpose**: hold **Left Alt** and walk off.
- Only works while you stand on the ground. Jumping, flying and falling work as normal.

## Placement assist (B)

When you hold right-click with a block, vanilla places a new block every 4 ticks, on a fixed beat. If you move faster than one block per beat, for example while speed-bridging or walking along a wall, that beat misses spots and leaves gaps.

- **Stride Sync** places as soon as the spot in front of your crosshair changes, instead of waiting for the next beat. It never places faster than once every 2 ticks (10 blocks per second); you can change this.
- **Adjacent only** (on): while you hold right-click, each new block must touch the block you placed just before. A quick flick of the camera no longer drops a stray block somewhere else.
- **Face lock** (off): while you hold right-click, only place on the same side of a block as your first click. It is off by default because diagonal bridging switches sides.

Your first click is never changed, and placement assist only acts while you hold a block.

## Angle lock (N)

Press **N** to step through:

1. **Off**
2. **Pitch locked**: you can turn left and right, but your view stays at the same height. Good for long walls and roofs.
3. **Yaw and pitch locked**: your view snaps to the nearest 45° (or 15° or 90°) and stays there. Good for straight bridges.

Only your mouse is locked; teleports still turn you as normal.

## Controls

| Key | What it does |
|---|---|
| **V** | Edge lock on or off |
| **Left Alt** (hold) | Step off a ledge while edge lock is on |
| **B** | Placement assist on or off |
| **N** | Angle lock: off, pitch, yaw + pitch |
| (not bound) | Open the settings screen |

All keys can be changed under Options → Controls → Foothold. The settings screen also opens with `/foothold` or from [Mod Menu](https://modrinth.com/mod/modmenu). A small status display in the top-left corner shows what is on; you can turn it off in the settings.

Settings are saved in `config/foothold/foothold.json`.

## Servers

Foothold sends no special packets: the server only sees normal walking and normal block placement, so it works on any server without being installed there.

Stride Sync does place faster than vanilla's hold-to-place (but not faster than quick clicking). Servers with strict anti-cheat may notice low gap settings. If you get flagged, set **Min gap** to 4 (vanilla speed) or turn placement assist off with **B**. Some servers have rules against movement or building helpers, so check the rules of the server you play on.

## Compatibility

- Minecraft **26.3**, Fabric Loader 0.20.1+, [Fabric API](https://modrinth.com/mod/fabric-api), Java 25.
- Optional: [Mod Menu](https://modrinth.com/mod/modmenu) for a Configure button.
- Other Minecraft versions and NeoForge may follow.

## Credits

Made by Skaffa, written together with Claude (Anthropic). Source code on [GitHub](https://github.com/skaffa/foothold) under the [MIT License](https://github.com/skaffa/foothold/blob/main/LICENSE).
