package dev.foothold.client;

import dev.foothold.core.FootholdConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** All Foothold settings in one column. Opened with {@code /foothold}, its key, or Mod Menu. */
public final class FootholdScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFA8A8A8;
	private static final int ACCENT = 0xFFFFC857;
	private static final int ROW = 19;
	private static final int H = 18;
	private static final int HEADER = 12;

	private final Screen parent;
	private final List<Label> labels = new ArrayList<>();
	private int colW;
	private int x;

	private record Label(String text, int x, int y, int color) {
	}

	public FootholdScreen(Screen parent) {
		super(Component.literal("Foothold"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		clearWidgets();
		labels.clear();
		FootholdConfig c = Foothold.config;
		colW = Math.min(260, width - 24);
		x = width / 2 - colW / 2;
		int y = 22;

		y = header(y, "Placement assist  [B]");
		y = toggle(y, "Enabled", () -> c.placementAssist, v -> c.placementAssist = v, null);
		y = toggle(y, "Stride Sync", () -> c.strideSync, v -> c.strideSync = v,
				"While right-click is held, place as soon as the target spot changes instead of on vanilla's fixed 4-tick beat.");
		y = cycle(y, "Min gap (ticks)", List.of(1, 2, 3, 4), () -> c.minGapTicks, v -> c.minGapTicks = v,
				"Fewest ticks between held placements. 2 = 10 blocks/s. 4 = vanilla rate (sync does nothing). Low values can trip strict anti-cheat.");
		y = toggle(y, "Adjacent only", () -> c.adjacentOnly, v -> c.adjacentOnly = v,
				"While held, only place touching the block placed just before.");
		y = toggle(y, "Face lock", () -> c.faceLock, v -> c.faceLock = v,
				"While held, only place on the same face direction as the first click. Breaks diagonal bridging.");

		y = header(y + 4, "Edge lock  [V]  (hold Left Alt to step off)");
		y = toggle(y, "Enabled", () -> c.edgeLock, v -> c.edgeLock = v, null);
		y = cycle(y, "Hold at drops of", List.of(1, 2, 3, 4, 6, 8, 16), () -> c.minDrop, v -> c.minDrop = v,
				"1 = every ledge, like sneaking. 2+ lets you walk down single steps.");

		y = header(y + 4, "Angle lock  [N]: off / pitch / yaw + pitch");
		y = cycle(y, "Yaw snap", List.of(15f, 45f, 90f), () -> c.yawStep, v -> c.yawStep = v, null);

		y = header(y + 4, "Display");
		toggle(y, "Status HUD", () -> c.showHud, v -> c.showHud = v, null);

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(width / 2 - 50, height - 24, 100, 20).build());
	}

	private int header(int y, String text) {
		labels.add(new Label(text, x, y, ACCENT));
		return y + HEADER;
	}

	private int toggle(int y, String label, BooleanSupplier get, Consumer<Boolean> set, String tip) {
		labels.add(new Label(label, x + 6, y + 5, GRAY));
		int bw = 70;
		Button.Builder b = Button.builder(onOff(get.getAsBoolean()), btn -> {
			set.accept(!get.getAsBoolean());
			btn.setMessage(onOff(get.getAsBoolean()));
		}).bounds(x + colW - bw, y, bw, H);
		if (tip != null) {
			b.tooltip(Tooltip.create(Component.literal(tip)));
		}
		addRenderableWidget(b.build());
		return y + ROW;
	}

	private <T> int cycle(int y, String label, List<T> options, Supplier<T> get, Consumer<T> set, String tip) {
		labels.add(new Label(label, x + 6, y + 5, GRAY));
		int bw = 70;
		Button.Builder b = Button.builder(Component.literal(fmt(get.get())), btn -> {
			int i = options.indexOf(get.get());
			set.accept(options.get((i + 1) % options.size()));
			btn.setMessage(Component.literal(fmt(get.get())));
		}).bounds(x + colW - bw, y, bw, H);
		if (tip != null) {
			b.tooltip(Tooltip.create(Component.literal(tip)));
		}
		addRenderableWidget(b.build());
		return y + ROW;
	}

	private static String fmt(Object v) {
		return v instanceof Float f ? String.format("%.0f°", f) : String.valueOf(v);
	}

	private static Component onOff(boolean v) {
		return Component.literal(v ? "On" : "Off");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
		super.extractRenderState(g, mx, my, delta);
		g.centeredText(font, title, width / 2, 8, WHITE);
		for (Label l : labels) {
			g.text(font, Component.literal(l.text()), l.x(), l.y(), l.color());
		}
	}

	@Override
	public void onClose() {
		Foothold.save();
		minecraft.gui.setScreen(parent);
	}
}
