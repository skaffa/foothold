package dev.foothold.client.mixin;

import dev.foothold.client.StrideSync;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stride Sync: clears vanilla's held right-click delay when the placement spot changes. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	private int rightClickDelay;

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void foothold$syncPlacement(CallbackInfo ci) {
		if (StrideSync.beforeKeybinds((Minecraft) (Object) this, rightClickDelay)) {
			rightClickDelay = 0;
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"))
	private void foothold$noteUse(CallbackInfo ci) {
		StrideSync.onStartUse((Minecraft) (Object) this);
	}
}
