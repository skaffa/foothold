package dev.foothold.client;

import com.mojang.blaze3d.platform.InputConstants;

import dev.foothold.core.AngleMode;
import dev.foothold.core.FootholdConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/** Client entrypoint: keys, events and the status HUD. */
public final class FootholdClient implements ClientModInitializer {
	private static KeyMapping edgeKey;
	private static KeyMapping placementKey;
	private static KeyMapping angleKey;
	private static KeyMapping dropKey;
	private static KeyMapping screenKey;
	private static boolean openScreen;

	@Override
	public void onInitializeClient() {
		Foothold.load();
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Foothold.MOD_ID, "main"));
		edgeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.foothold.edge", InputConstants.KEY_V, category));
		placementKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.foothold.placement", InputConstants.KEY_B, category));
		angleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.foothold.angle", InputConstants.KEY_N, category));
		dropKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.foothold.drop", InputConstants.KEY_LALT, category));
		screenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.foothold.screen", InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(FootholdClient::tick);
		UseBlockCallback.EVENT.register(StrideSync::onUseBlock);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> AngleLock.reset());
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(literal("foothold").executes(c -> {
			openScreen = true;
			return 1;
		})));
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Foothold.MOD_ID, "status"), FootholdClient::drawHud);
		Foothold.LOG.info("Foothold loaded");
	}

	static boolean dropKeyDown() {
		return dropKey != null && dropKey.isDown();
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		FootholdConfig c = Foothold.config;
		while (edgeKey.consumeClick()) {
			c.edgeLock = !c.edgeLock;
			Foothold.save();
			Foothold.say("edge lock " + (c.edgeLock ? "ON (hold " + dropKey.getTranslatedKeyMessage().getString() + " to step off)" : "OFF"));
		}
		while (placementKey.consumeClick()) {
			c.placementAssist = !c.placementAssist;
			Foothold.save();
			Foothold.say("placement assist " + (c.placementAssist ? "ON" : "OFF"));
		}
		while (angleKey.consumeClick()) {
			AngleLock.cycle(mc.player);
		}
		while (screenKey.consumeClick()) {
			openScreen = true;
		}
		if (openScreen) {
			openScreen = false;
			mc.gui.setScreen(new FootholdScreen(null));
		}
	}

	private static void drawHud(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		FootholdConfig c = Foothold.config;
		if (!c.showHud || p == null || mc.debugEntries.isOverlayVisible()) {
			return;
		}
		List<String> lines = new ArrayList<>();
		if (c.edgeLock) {
			boolean bypass = dropKeyDown();
			lines.add(bypass ? "Edge lock: released" : "Edge lock: on" + (c.minDrop > 1 ? " (drops " + c.minDrop + "+)" : ""));
		}
		if (c.placementAssist && StrideSync.holdingBlock(p)) {
			double speed = p.getDeltaMovement().horizontalDistance() * 20.0;
			lines.add(String.format("Placement: %s @ %.1f m/s", c.strideSync ? "sync " + c.minGapTicks + "t" : "guard", speed));
		}
		if (AngleLock.mode != AngleMode.OFF) {
			lines.add(String.format(AngleLock.mode == AngleMode.FULL ? "Angle: yaw %.0f, pitch %.1f" : "Angle: pitch %2$.1f",
					p.getYRot(), p.getXRot()));
		}
		int y = 4;
		for (String line : lines) {
			g.text(mc.font, line, 4, y, 0xFFFFC857, true);
			y += 10;
		}
	}
}
