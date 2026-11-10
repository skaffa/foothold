package dev.foothold.client;

import dev.foothold.core.AngleMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/** Freezes mouse look: pitch only, or yaw (snapped) and pitch. Not saved; resets on leaving a world. */
public final class AngleLock {
	static AngleMode mode = AngleMode.OFF;

	private AngleLock() {
	}

	static void cycle(LocalPlayer p) {
		mode = mode.next();
		if (mode == AngleMode.FULL) {
			float yaw = AngleMode.snapYaw(p.getYRot(), Foothold.config.yawStep);
			p.setYRot(yaw);
			p.yRotO = yaw;
			p.setYHeadRot(yaw);
		}
		Foothold.say("angle " + mode.label + (mode == AngleMode.OFF ? "" : String.format(" (yaw %.0f, pitch %.1f)", p.getYRot(), p.getXRot())));
	}

	static void reset() {
		mode = AngleMode.OFF;
	}

	/** Mouse yaw delta, as passed to {@code Entity.turn}. */
	public static double filterYaw(Entity e, double delta) {
		return mode == AngleMode.FULL && e == Minecraft.getInstance().player ? 0 : delta;
	}

	/** Mouse pitch delta, as passed to {@code Entity.turn}. */
	public static double filterPitch(Entity e, double delta) {
		return mode != AngleMode.OFF && e == Minecraft.getInstance().player ? 0 : delta;
	}
}
