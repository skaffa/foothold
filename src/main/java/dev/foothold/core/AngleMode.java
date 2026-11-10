package dev.foothold.core;

/** What the angle lock key holds still. */
public enum AngleMode {
	OFF("off"),
	/** Pitch frozen, yaw free: keeps the crosshair at one height along a long wall. */
	PITCH("pitch locked"),
	/** Yaw snapped to the step and frozen; pitch frozen too. */
	FULL("yaw + pitch locked");

	public final String label;

	AngleMode(String label) {
		this.label = label;
	}

	public AngleMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	/** Nearest multiple of {@code step}, wrapped to [-180, 180). */
	public static float snapYaw(float yaw, float step) {
		float s = Math.round(yaw / step) * step;
		s = ((s + 180f) % 360f + 360f) % 360f - 180f;
		return s == 0f ? 0f : s;
	}
}
