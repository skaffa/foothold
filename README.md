# Foothold

Client-side building helpers for Minecraft (Fabric 26.3): stop at ledges without sneaking, place
blocks without gaps while holding right-click, and lock your camera angle. Needs nothing on the server.

![Foothold icon](media/icon-128.png)

## Use

* **V** — Edge lock: you stop at ledges as if sneaking, at full walking speed. By default only at
  drops of 2+ blocks, so single steps still work. Hold **Left Alt** to step off.
* **B** — Placement assist, while right-click is held with a block:
  * *Stride Sync* places as soon as the target spot changes instead of on vanilla's fixed 4-tick
    beat (no sooner than *Min gap* ticks, default 2), so fast bridging leaves no gaps.
  * *Adjacent only* (on) and *Face lock* (off) cancel held placements away from the previous
    block or on a different face.
* **N** — Angle lock: off → pitch locked → yaw snapped to 45° (or 15°/90°) + pitch locked.

Settings: `/foothold`, Mod Menu, or a key you bind under Controls → Foothold. Saved in
`config/foothold/foothold.json`. A small HUD in the top-left shows what is on.

Stride Sync places faster than vanilla hold-to-place, so strict anti-cheat may notice low *Min gap*
values; set it to 4 or press **B** to turn it off.

## Build

Needs JDK 25.

```
./gradlew build      # jar in build/libs, runs the JUnit tests
./gradlew runClient  # dev client
```

The logic in `src/main/java/dev/foothold/core` has no Minecraft imports and is tested with plain
JUnit. See [SPEC.md](SPEC.md) for the design and the changes from the first draft.
`python tools/make_icon.py` regenerates the icon.

## License

MIT. Made by Skaffa, written together with Claude (Anthropic).
