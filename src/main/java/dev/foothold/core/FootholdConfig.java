package dev.foothold.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Everything Foothold saves in {@code config/foothold.json}. */
public final class FootholdConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final int MIN_GAP = 1;
	/** Vanilla repeats a held right-click every 4 ticks; a gap of 4 makes Stride Sync do nothing. */
	public static final int MAX_GAP = 4;
	public static final int MAX_DROP = 16;

	// ---- placement assist (toggle key B) ----
	/** Master switch for Stride Sync and the run guards. */
	public boolean placementAssist = true;
	/** While right-click is held, place as soon as the target spot changes instead of on vanilla's fixed 4-tick beat. */
	public boolean strideSync = true;
	/** Fewest ticks between two held placements. 2 ticks = 10 blocks per second. */
	public int minGapTicks = 2;
	/** While held, only place next to the block placed before (stops stray blocks when the crosshair swings). */
	public boolean adjacentOnly = true;
	/** While held, only place on the same face direction as the first click (off by default: breaks diagonal bridging). */
	public boolean faceLock = false;

	// ---- edge lock (toggle key V) ----
	/** Act as if sneaking at ledges, without sneaking. */
	public boolean edgeLock = false;
	/** Only hold at drops at least this deep. 1 = every ledge, like sneaking. */
	public int minDrop = 2;

	// ---- angle lock (key N) ----
	/** Yaw snap step for full lock, in degrees: 15, 45 or 90. */
	public float yawStep = 45f;

	// ---- display ----
	public boolean showHud = true;

	public FootholdConfig clamp() {
		minGapTicks = Math.max(MIN_GAP, Math.min(MAX_GAP, minGapTicks));
		minDrop = Math.max(1, Math.min(MAX_DROP, minDrop));
		if (!(yawStep == 15f || yawStep == 45f || yawStep == 90f)) {
			yawStep = 45f;
		}
		return this;
	}

	public static FootholdConfig load(Path file) {
		if (!Files.exists(file)) {
			return new FootholdConfig();
		}
		try {
			FootholdConfig c = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FootholdConfig.class);
			return c == null ? new FootholdConfig() : c.clamp();
		} catch (IOException | JsonParseException e) {
			// keep the broken file for the user and start fresh
			try {
				Files.copy(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ignored) {
				// nothing more to do
			}
			return new FootholdConfig();
		}
	}

	public void save(Path file) throws IOException {
		Files.createDirectories(file.getParent());
		Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
		Files.writeString(tmp, GSON.toJson(this), StandardCharsets.UTF_8);
		Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}
}
