# Foothold

Client-side Fabric mod for Minecraft 26.3. Helpers for builders, roofers and bridgers. Sends no extra packet types; the server sees normal movement and normal block placements.

Settings: `/foothold`, the settings key (unbound), or Mod Menu. Saved in `config/foothold.json`.

## Features

### Placement assist (key B)

Only acts while right-click is **held** with a block in either hand. The first click is never changed.

- **Stride Sync.** Vanilla repeats a held right-click on a fixed 4-tick beat that starts when the button goes down. When you move more than one block per 4 ticks, the beat misses spots and leaves gaps. Stride Sync fires as soon as the spot a block would go to changes, but no sooner than *Min gap* ticks after the last use (default 2 = at most 10 blocks/s). Min gap 4 = vanilla rate, so sync does nothing.
- **Adjacent only** (on). A held placement must touch (incl. diagonally) the block placed just before. Stops stray blocks when the crosshair swings.
- **Face lock** (off). A held placement must use the same face direction as the first click. Off by default because diagonal bridging alternates faces.

Blocked placements are cancelled client-side (`UseBlockCallback` returns `FAIL`), so nothing is sent.

### Edge lock (key V, hold Left Alt to step off)

Makes vanilla's sneak-at-ledge logic (`Player.maybeBackOffFromEdge`) apply without sneaking, for the local player only. Vanilla still limits it to on-ground, not flying, not jumping.

- **Hold at drops of** (default 2). Holds only next to a drop at least this deep, so you can still walk down single steps. 1 = every ledge, like sneaking.
- Hold the **Step off** key (default Left Alt) to walk off on purpose.

### Angle lock (key N)

Cycles: off → pitch locked (yaw free; keeps the crosshair at one height along a wall) → yaw snapped to 15/45/90° + pitch locked. Only mouse look is frozen; teleports and server rotation still apply. Resets on disconnect.

### HUD

Top-left status lines while a feature is active (hidden with F3 open). Off in settings.

## Changes from the original (Gemini) draft

- **Fabric only.** NeoForge was listed; not built (same as Ortho).
- **"Prevents placements in the open air behind your target"** — vanilla already cannot place a block in open air. Replaced by the *Adjacent only* and *Face lock* run guards, which stop the real failure: a held click landing on the wrong face or a far block.
- **"0.05 s tick timing sync" / "no unnatural packet speeds"** — Stride Sync only helps by firing *earlier* than vanilla's 4-tick repeat, so it does place faster than vanilla hold-to-place (default cap 10/s, still below fast clicking). Strict anti-cheat "fast place" checks may notice low Min gap values; set 4 to disable.
- **"Shift + S or double-tap Shift to drop off"** — Shift is sneak, which itself keeps you on the ledge, so it cannot be the bypass. Replaced by a hold key (Left Alt).
- **Edge lock depth.** The draft would block every ledge, including single steps, making normal walking awkward. Added *Hold at drops of* (default 2).
- **`[Alt + V]` config key** — vanilla key mappings cannot do combos. Settings open via `/foothold`, an unbound key, or Mod Menu.
- **"4.3 m/s" in the UI mock** — the HUD shows your actual horizontal speed instead.
- **Mojang names.** The draft used Yarn names (`ClientPlayerEntity`, `input.sideways`); 26.3 uses Mojang names (`LocalPlayer`, …). Edge lock hooks `Player.isStayingOnGroundSurface`, not the input vector.

## Code

- `src/main/java/dev/foothold/core` — Minecraft-free logic (config, `PlacementRun`, `EdgeProbe`, `AngleMode`), JUnit-tested.
- `src/client/java/dev/foothold/client` — keys, HUD, screen, glue.
- Mixins: `MinecraftMixin` (`handleKeybinds` head clears `rightClickDelay`; `startUseItem` head records the use), `PlayerMixin` (`isStayingOnGroundSurface`), `EntityMixin` (`turn` deltas).

Build: `JAVA_HOME=<JDK 25> ./gradlew build`. Dev client: `./gradlew runClient`.
