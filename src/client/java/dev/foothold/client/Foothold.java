package dev.foothold.client;

import dev.foothold.core.FootholdConfig;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/** Shared state: the loaded config and small helpers. */
public final class Foothold {
	public static final String MOD_ID = "foothold";
	public static final Logger LOG = LoggerFactory.getLogger("Foothold");
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("foothold.json");

	public static FootholdConfig config = new FootholdConfig();

	private Foothold() {
	}

	static void load() {
		config = FootholdConfig.load(FILE).clamp();
	}

	public static void save() {
		try {
			config.clamp().save(FILE);
		} catch (IOException e) {
			LOG.warn("Could not save {}: {}", FILE, e.toString());
		}
	}

	/** Action-bar message, so it does not fill the chat. */
	public static void say(String msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.sendOverlayMessage(Component.literal("Foothold: " + msg).withStyle(ChatFormatting.GOLD));
		}
	}
}
