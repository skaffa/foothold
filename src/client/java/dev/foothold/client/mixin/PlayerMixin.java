package dev.foothold.client.mixin;

import dev.foothold.client.EdgeGuard;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Edge Lock: the local player keeps to the ground surface at ledges as if sneaking. */
@Mixin(Player.class)
abstract class PlayerMixin {
	@Inject(method = "isStayingOnGroundSurface", at = @At("RETURN"), cancellable = true)
	private void foothold$edgeLock(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && EdgeGuard.engaged((Player) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
