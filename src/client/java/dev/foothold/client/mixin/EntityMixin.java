package dev.foothold.client.mixin;

import dev.foothold.client.AngleLock;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Angle Lock: drops mouse-look deltas for the locked axes. */
@Mixin(Entity.class)
abstract class EntityMixin {
	@ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double foothold$lockYaw(double yaw) {
		return AngleLock.filterYaw((Entity) (Object) this, yaw);
	}

	@ModifyVariable(method = "turn", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private double foothold$lockPitch(double pitch) {
		return AngleLock.filterPitch((Entity) (Object) this, pitch);
	}
}
