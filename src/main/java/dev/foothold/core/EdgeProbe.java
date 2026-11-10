package dev.foothold.core;

/** Decides whether a ledge next to the player is deep enough for Edge Lock to hold. */
public final class EdgeProbe {
	/** How far beyond the bounding box to look: more than one tick of sprinting (about 0.28 blocks). */
	public static final double REACH = 0.35;

	@FunctionalInterface
	public interface SolidTest {
		boolean solid(int x, int y, int z);
	}

	private EdgeProbe() {
	}

	/**
	 * True when any block column under or beside the player's feet has no solid block within
	 * {@code minDrop} blocks below floor level.
	 *
	 * @param feetY the player's Y (bottom of the bounding box)
	 */
	public static boolean nearDrop(double minX, double minZ, double maxX, double maxZ, double feetY, int minDrop, SolidTest test) {
		if (minDrop <= 1) {
			return true;
		}
		// Floor block: the block under a full block's top, or the slab/stair the player stands in.
		int floor = (int) Math.floor(feetY - 1.0e-3);
		int x0 = (int) Math.floor(minX - REACH);
		int x1 = (int) Math.floor(maxX + REACH);
		int z0 = (int) Math.floor(minZ - REACH);
		int z1 = (int) Math.floor(maxZ + REACH);
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				if (!hasGround(x, z, floor, minDrop, test)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasGround(int x, int z, int floor, int minDrop, SolidTest test) {
		for (int d = 0; d < minDrop; d++) {
			if (test.solid(x, floor - d, z)) {
				return true;
			}
		}
		return false;
	}
}
