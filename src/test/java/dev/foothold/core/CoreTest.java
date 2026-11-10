package dev.foothold.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreTest {
	// ---- PlacementRun ----

	@Test
	void firstPlacementAlwaysAllowed() {
		PlacementRun r = new PlacementRun();
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(2, 100, 64, 100, true, true));
	}

	@Test
	void adjacentGuardRejectsFarSpots() {
		PlacementRun r = new PlacementRun();
		r.accept(2, 0, 64, 0);
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(2, 0, 64, 1, false, true));
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(2, 1, 64, 1, false, true)); // diagonal
		assertEquals(PlacementRun.Verdict.NOT_ADJACENT, r.check(2, 0, 64, 2, false, true));
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(2, 0, 64, 2, false, false));
	}

	@Test
	void faceLockKeepsFirstFace() {
		PlacementRun r = new PlacementRun();
		r.accept(2, 0, 64, 0);
		r.accept(3, 0, 64, 1); // face stays 2
		assertEquals(PlacementRun.Verdict.WRONG_FACE, r.check(3, 0, 64, 2, true, false));
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(2, 0, 64, 2, true, false));
		r.reset();
		assertEquals(PlacementRun.Verdict.ALLOW, r.check(3, 9, 9, 9, true, true));
	}

	@Test
	void fireEarlyOnlyWhenTargetMovedAndGapPassed() {
		assertTrue(PlacementRun.shouldFireEarly(2, 2, 2, true));
		assertFalse(PlacementRun.shouldFireEarly(2, 2, 2, false)); // same spot: vanilla beat
		assertFalse(PlacementRun.shouldFireEarly(3, 1, 2, true)); // too soon
		assertFalse(PlacementRun.shouldFireEarly(0, 4, 2, true)); // vanilla fires anyway
	}

	// ---- EdgeProbe ----

	/** Flat floor at y=63 for x <= 0, nothing beyond; player stands at y=64. */
	private static final EdgeProbe.SolidTest CLIFF = (x, y, z) -> y == 63 && x <= 0;

	@Test
	void cliffIsDetectedNearEdge() {
		// player box x in [0.5, 1.1): reach crosses into x = 1, which has no floor
		assertTrue(EdgeProbe.nearDrop(0.4, 0.2, 1.0, 0.8, 64.0, 2, CLIFF));
	}

	@Test
	void noDropAwayFromEdge() {
		assertFalse(EdgeProbe.nearDrop(-3.3, 0.2, -2.7, 0.8, 64.0, 2, CLIFF));
	}

	@Test
	void singleStepIsNotADrop() {
		// floor y=63 for x <= 0, y=62 for x >= 1: a one-block step down
		EdgeProbe.SolidTest step = (x, y, z) -> (x <= 0 && y == 63) || (x >= 1 && y == 62);
		assertFalse(EdgeProbe.nearDrop(0.4, 0.2, 1.0, 0.8, 64.0, 2, step));
		assertTrue(EdgeProbe.nearDrop(0.4, 0.2, 1.0, 0.8, 64.0, 1, step));
	}

	@Test
	void slabFloorUsesBlockInside() {
		// standing on a bottom slab at y=63 (feet at 63.5): slab block is y=63
		EdgeProbe.SolidTest slab = (x, y, z) -> y == 63;
		assertFalse(EdgeProbe.nearDrop(0.2, 0.2, 0.8, 0.8, 63.5, 2, slab));
	}

	// ---- AngleMode ----

	@Test
	void yawSnaps() {
		assertEquals(45f, AngleMode.snapYaw(50f, 45f));
		assertEquals(-90f, AngleMode.snapYaw(-80f, 45f));
		assertEquals(-180f, AngleMode.snapYaw(179f, 45f));
		assertEquals(0f, AngleMode.snapYaw(365f, 90f));
		assertEquals(AngleMode.PITCH, AngleMode.OFF.next());
		assertEquals(AngleMode.OFF, AngleMode.FULL.next());
	}

	// ---- config ----

	@Test
	void configRoundTripsAndClamps() throws Exception {
		Path dir = Files.createTempDirectory("foothold");
		Path f = dir.resolve("foothold.json");
		FootholdConfig c = new FootholdConfig();
		c.minGapTicks = 99;
		c.minDrop = 0;
		c.yawStep = 30f;
		c.save(f);
		FootholdConfig back = FootholdConfig.load(f);
		assertEquals(FootholdConfig.MAX_GAP, back.minGapTicks);
		assertEquals(1, back.minDrop);
		assertEquals(45f, back.yawStep);
		Files.writeString(f, "{ not json");
		assertEquals(2, FootholdConfig.load(f).minGapTicks);
		assertTrue(Files.exists(dir.resolve("foothold.json.broken")));
	}
}
