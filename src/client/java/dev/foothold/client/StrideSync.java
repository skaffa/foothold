package dev.foothold.client;

import dev.foothold.core.FootholdConfig;
import dev.foothold.core.PlacementRun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Placement assist while right-click is held with a block.
 *
 * <p>Vanilla repeats a held right-click on a fixed 4-tick beat that starts when the button goes down.
 * When you move faster than one block per 4 ticks the beat misses spots ("ghost skips"). Stride Sync
 * fires as soon as the spot a block would go to changes, no sooner than {@code minGapTicks} after the
 * last use. The run guards cancel held placements on the wrong face or away from the previous block.
 */
public final class StrideSync {
	private static final PlacementRun RUN = new PlacementRun();
	private static boolean wasDown;
	private static long tick;
	private static long lastUseTick = Long.MIN_VALUE / 2;
	private static BlockPos lastTarget;

	private StrideSync() {
	}

	/** Called at the start of {@code Minecraft.handleKeybinds}. Returns true to make vanilla use the item this tick. */
	public static boolean beforeKeybinds(Minecraft mc, int rightClickDelay) {
		tick++;
		boolean down = mc.options.keyUse.isDown();
		if (!down || mc.player == null) {
			wasDown = false;
			return false;
		}
		if (!wasDown) {
			// Fresh press: vanilla handles the click itself; start a new run.
			wasDown = true;
			RUN.reset();
			return false;
		}
		FootholdConfig c = Foothold.config;
		if (!c.placementAssist || !c.strideSync || mc.player.isUsingItem() || !holdingBlock(mc.player)) {
			return false;
		}
		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		BlockPos target = target(mc.level, hit);
		boolean changed = !target.equals(lastTarget);
		int since = (int) Math.min(Integer.MAX_VALUE, tick - lastUseTick);
		if (!PlacementRun.shouldFireEarly(rightClickDelay, since, c.minGapTicks, changed)) {
			return false;
		}
		// Do not fire early into a spot the guards would refuse anyway.
		return RUN.check(hit.getDirection().ordinal(), target.getX(), target.getY(), target.getZ(), c.faceLock, c.adjacentOnly)
				== PlacementRun.Verdict.ALLOW;
	}

	/** Called at the start of {@code Minecraft.startUseItem}. */
	public static void onStartUse(Minecraft mc) {
		lastUseTick = tick;
		if (mc.level != null && mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
			lastTarget = target(mc.level, hit);
		} else {
			lastTarget = null;
		}
	}

	/** Fabric {@code UseBlockCallback}: runs before the client sends a placement. */
	static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		Minecraft mc = Minecraft.getInstance();
		if (!level.isClientSide() || player != mc.player || !wasDown || !Foothold.config.placementAssist) {
			return InteractionResult.PASS;
		}
		if (!(player.getItemInHand(hand).getItem() instanceof BlockItem)) {
			return InteractionResult.PASS;
		}
		FootholdConfig c = Foothold.config;
		BlockPos target = target(level, hit);
		int face = hit.getDirection().ordinal();
		PlacementRun.Verdict v = RUN.check(face, target.getX(), target.getY(), target.getZ(), c.faceLock, c.adjacentOnly);
		if (v != PlacementRun.Verdict.ALLOW) {
			// FAIL: vanilla sends nothing and does not fall through to the other hand.
			return InteractionResult.FAIL;
		}
		RUN.accept(face, target.getX(), target.getY(), target.getZ());
		return InteractionResult.PASS;
	}

	/** The block a placement would fill: the clicked block if replaceable (grass, snow), else the one in front of the face. */
	static BlockPos target(Level level, BlockHitResult hit) {
		BlockPos clicked = hit.getBlockPos();
		return level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(hit.getDirection());
	}

	static boolean holdingBlock(LocalPlayer p) {
		return p.getMainHandItem().getItem() instanceof BlockItem || p.getOffhandItem().getItem() instanceof BlockItem;
	}
}
