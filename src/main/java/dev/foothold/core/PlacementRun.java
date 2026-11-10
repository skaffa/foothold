package dev.foothold.core;

/**
 * State of one held right-click "run" of block placements. The first placement of a run is always
 * allowed; later ones can be limited to the same face and to spots next to the previous block.
 */
public final class PlacementRun {
	public enum Verdict {
		ALLOW,
		WRONG_FACE,
		NOT_ADJACENT
	}

	private boolean started;
	private int face;
	private int lastX;
	private int lastY;
	private int lastZ;

	public void reset() {
		started = false;
	}

	public boolean started() {
		return started;
	}

	/** {@code face} is any stable id for the clicked face (e.g. {@code Direction.ordinal()}). */
	public Verdict check(int face, int x, int y, int z, boolean faceLock, boolean adjacentOnly) {
		if (!started) {
			return Verdict.ALLOW;
		}
		if (faceLock && face != this.face) {
			return Verdict.WRONG_FACE;
		}
		if (adjacentOnly && !touches(x, y, z)) {
			return Verdict.NOT_ADJACENT;
		}
		return Verdict.ALLOW;
	}

	/** Records a placement; the first one also fixes the run's face. */
	public void accept(int face, int x, int y, int z) {
		if (!started) {
			started = true;
			this.face = face;
		}
		lastX = x;
		lastY = y;
		lastZ = z;
	}

	/** Same block or one of its 26 neighbours (diagonal bridging places corner to corner). */
	private boolean touches(int x, int y, int z) {
		return Math.abs(x - lastX) <= 1 && Math.abs(y - lastY) <= 1 && Math.abs(z - lastZ) <= 1;
	}

	/**
	 * Whether a held right-click should fire this tick although vanilla's repeat delay has not run out.
	 *
	 * @param vanillaDelay  ticks left on vanilla's repeat delay (0 = vanilla fires anyway)
	 * @param ticksSinceUse ticks since the last use
	 * @param targetChanged the spot a block would go to differs from the one at the last use
	 */
	public static boolean shouldFireEarly(int vanillaDelay, int ticksSinceUse, int minGap, boolean targetChanged) {
		return vanillaDelay > 0 && targetChanged && ticksSinceUse >= minGap;
	}
}
