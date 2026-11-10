package dev.foothold.client;

import dev.foothold.core.EdgeProbe;
import dev.foothold.core.FootholdConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Edge Lock: makes vanilla's sneak-at-ledge logic ({@code Player.maybeBackOffFromEdge}) apply without
 * sneaking. Vanilla still decides the details: only on the ground, not while flying or jumping.
 */
public final class EdgeGuard {
	private EdgeGuard() {
	}

	/** Called from {@code Player.isStayingOnGroundSurface} when vanilla says no. */
	public static boolean engaged(Player player) {
		Minecraft mc = Minecraft.getInstance();
		FootholdConfig c = Foothold.config;
		if (!c.edgeLock || player != mc.player || FootholdClient.dropKeyDown()) {
			return false;
		}
		if (player.getAbilities().flying || !player.onGround()) {
			return false;
		}
		Level level = player.level();
		AABB box = player.getBoundingBox();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		return EdgeProbe.nearDrop(box.minX, box.minZ, box.maxX, box.maxZ, box.minY, c.minDrop,
				(x, y, z) -> !level.getBlockState(pos.set(x, y, z)).getCollisionShape(level, pos).isEmpty());
	}
}
