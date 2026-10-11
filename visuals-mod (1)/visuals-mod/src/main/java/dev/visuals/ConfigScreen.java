package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/** Menu ustawień otwierane klawiszem O. */
public class ConfigScreen extends Screen {
	private enum Tab {
		HAT("Kapelusz", "Własny kapelusz widać w trzeciej osobie (F5)."),
		TRAIL("Trail", "Wstęga i helisa to własny blask. Kolor działa dla większości trybów."),
		KILL("Kill effect", "Efekt pojawia się, gdy zabijesz kogoś, w kogo uderzyłeś."),
		LANDING("Lądowanie", "Magiczny okrąg pojawia się w miejscu, gdzie wylądowałeś."),
		GENERAL("Ogólne", "Jasność dotyczy wszystkich efektów rysowanych blaskiem.");

		final String label;
		final String hint;

		Tab(String label, String hint) {
			this.label = label;
			this.hint = hint;
		}
	}

	private static final int ROW = 24;
	private static final int GAP = 8;
	private static final int COLUMN_WIDTH = 150;

	private static Tab lastTab = Tab.HAT;
	private Tab tab = lastTab;

	// Układ siatki (liczony w init)
	private int columns;
	private int columnWidth;
	private int gridLeft;
	private int gridTop;
	private int gridWidth;
	private int slot;

	public ConfigScreen() {
		super(Text.literal("Visuals"));
	}

	@Override
	protected void init() {
		VisualsConfig cfg = VisualsConfig.get();

		// Zakładki
		Tab[] tabs = Tab.values();
		int tabGap = 4;
		int tabWidth = Math.min(90, (this.width - 20 - tabGap * (tabs.length - 1)) / tabs.length);
		int tabsTotal = tabWidth * tabs.length + tabGap * (tabs.length - 1);
		int tabsX = (this.width - tabsTotal) / 2;
		for (int i = 0; i < tabs.length; i++) {
			Tab t = tabs[i];
			String label = (t == tab ? "» " : "") + t.label;
			this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
				tab = t;
				lastTab = t;
				this.clearAndInit();
			}).dimensions(tabsX + i * (tabWidth + tabGap), 28, tabWidth, 20).build());
		}

		// Siatka: dwie kolumny, jeśli okno jest wystarczająco szerokie.
		boolean twoColumns = this.width >= 2 * COLUMN_WIDTH + GAP + 20;
		columns = twoColumns ? 2 : 1;
		columnWidth = twoColumns ? COLUMN_WIDTH : Math.min(240, this.width - 20);
		gridWidth = columns * columnWidth + (columns - 1) * GAP;
		gridLeft = (this.width - gridWidth) / 2;
		gridTop = 58;
		slot = 0;

		switch (tab) {
			case HAT -> {
				addToggle(false, "Kapelusz", () -> cfg.hatEnabled, v -> cfg.hatEnabled = v);
				addCycle(false, "Kolor", ColorPreset.values(), () -> cfg.hatColor, v -> cfg.hatColor = v, c -> c.label);
				addSlider(false, "Rozmiar", 0.5, 1.8, 0.05, "%.2fx", () -> cfg.hatSize, v -> cfg.hatSize = v);
				addToggle(false, "Świecenie", () -> cfg.hatGlow, v -> cfg.hatGlow = v);
				addToggle(true, "Widoczny u innych graczy", () -> cfg.hatOnOthers, v -> cfg.hatOnOthers = v);
			}
			case TRAIL -> {
				addToggle(false, "Trail", () -> cfg.trailEnabled, v -> cfg.trailEnabled = v);
				addToggle(false, "Tylko w ruchu", () -> cfg.trailOnlyMoving, v -> cfg.trailOnlyMoving = v);
				addCycle(true, "Rodzaj", TrailType.values(), () -> cfg.trailType, v -> cfg.trailType = v, t -> t.label);
				addCycle(false, "Kolor", ColorPreset.values(), () -> cfg.trailColor, v -> cfg.trailColor = v, c -> c.label);
				addSlider(false, "Gęstość", 1, 6, 1, "%.0f", () -> cfg.trailDensity, v -> cfg.trailDensity = (int) Math.round(v));
				addSlider(false, "Długość", 0.5, 5.0, 0.25, "%.2f s", () -> cfg.trailLength / 20.0, v -> cfg.trailLength = (int) Math.round(v * 20.0));
				addSlider(false, "Szerokość", 0.05, 0.6, 0.01, "%.2f", () -> cfg.trailWidth, v -> cfg.trailWidth = v);
				addSlider(true, "Wysokość nad stopami", 0.1, 1.8, 0.05, "%.2f", () -> cfg.trailHeight, v -> cfg.trailHeight = v);
			}
			case KILL -> {
				addToggle(false, "Kill effect", () -> cfg.killEnabled, v -> cfg.killEnabled = v);
				addToggle(false, "Tylko gracze", () -> cfg.killPlayersOnly, v -> cfg.killPlayersOnly = v);
				addCycle(true, "Rodzaj", KillEffectType.values(), () -> cfg.killType, v -> cfg.killType = v, t -> t.label);
				addCycle(true, "Kolor", ColorPreset.values(), () -> cfg.killColor, v -> cfg.killColor = v, c -> c.label);
				addButton(true, "Test (zamyka menu)", button -> testAhead(true));
			}
			case LANDING -> {
				addToggle(false, "Kółka lądowania", () -> cfg.landingEnabled, v -> cfg.landingEnabled = v);
				addCycle(false, "Kolor", ColorPreset.values(), () -> cfg.landingColor, v -> cfg.landingColor = v, c -> c.label);
				addSlider(false, "Promień", 0.5, 4.0, 0.1, "%.1f", () -> cfg.landingRadius, v -> cfg.landingRadius = v);
				addSlider(false, "Czas", 0.5, 8.0, 0.5, "%.1f s", () -> cfg.landingDuration / 20.0, v -> cfg.landingDuration = (int) Math.round(v * 20.0));
				addSlider(true, "Min. wysokość upadku", 0.0, 5.0, 0.5, "%.1f", () -> cfg.landingMinFall, v -> cfg.landingMinFall = v);
				addButton(true, "Test (zamyka menu)", button -> testAhead(false));
			}
			case GENERAL -> {
				addSlider(true, "Jasność efektów", 0.2, 2.0, 0.05, "%.2f", () -> cfg.glowIntensity, v -> cfg.glowIntensity = v);
				addButton(true, "Przywróć ustawienia domyślne", button -> {
					VisualsConfig.resetToDefaults();
					this.clearAndInit();
				});
			}
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), button -> this.close())
			.dimensions((this.width - gridWidth) / 2, this.height - 28, gridWidth, 20).build());
	}

	/** Odpala efekt 3 bloki przed graczem i zamyka menu, żeby było go widać. */
	private void testAhead(boolean kill) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}
		double yaw = Math.toRadians(player.getYaw());
		double px = player.getX() - Math.sin(yaw) * 3.0;
		double pz = player.getZ() + Math.cos(yaw) * 3.0;
		mc.setScreen(null);
		if (kill) {
			KillEffect.spawn(mc, px, player.getY(), pz, 1.8);
		} else {
			LandingEffect.spawnRing(px, player.getY(), pz);
		}
	}

	/** Zwraca {x, y, szerokość} kolejnego miejsca w siatce. */
	private int[] nextSlot(boolean full) {
		if (full && slot % columns != 0) {
			slot += columns - slot % columns;
		}
		int row = slot / columns;
		int col = slot % columns;
		int x = gridLeft + col * (columnWidth + GAP);
		int y = gridTop + row * ROW;
		int w = full ? gridWidth : columnWidth;
		slot += full ? columns : 1;
		return new int[]{x, y, w};
	}

	private void addButton(boolean full, String label, ButtonWidget.PressAction action) {
		int[] r = nextSlot(full);
		this.addDrawableChild(ButtonWidget.builder(Text.literal(label), action).dimensions(r[0], r[1], r[2], 20).build());
	}

	private void addToggle(boolean full, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		int[] r = nextSlot(full);
		this.addDrawableChild(ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
			boolean value = !getter.getAsBoolean();
			setter.accept(value);
			button.setMessage(toggleText(label, value));
		}).dimensions(r[0], r[1], r[2], 20).build());
	}

	private static Text toggleText(String label, boolean value) {
		return Text.literal(label + ": " + (value ? "WŁ." : "WYŁ."));
	}

	private <E extends Enum<E>> void addCycle(boolean full, String label, E[] values,
											  Supplier<E> getter, Consumer<E> setter, Function<E, String> name) {
		int[] r = nextSlot(full);
		this.addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + name.apply(getter.get())), button -> {
			E next = values[(getter.get().ordinal() + 1) % values.length];
			setter.accept(next);
			button.setMessage(Text.literal(label + ": " + name.apply(next)));
		}).dimensions(r[0], r[1], r[2], 20).build());
	}

	private void addSlider(boolean full, String label, double min, double max, double step,
						   String format, DoubleSupplier getter, DoubleConsumer setter) {
		int[] r = nextSlot(full);
		this.addDrawableChild(new ValueSlider(r[0], r[1], r[2], 20, label, min, max, step, getter.getAsDouble(), format, setter));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Visuals"), this.width / 2, 12, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(tab.hint), this.width / 2, this.height - 42, 0xFFAAAAAA);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		// Ten sam klawisz (domyślnie O) zamyka menu.
		if (VisualsClient.openMenuKey != null && VisualsClient.openMenuKey.matchesKey(input)) {
			this.close();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		VisualsConfig.save();
	}

	/** Suwak z wartością liczbową i zaokrąglaniem do kroku. */
	private static final class ValueSlider extends SliderWidget {
		private final String label;
		private final double min;
		private final double max;
		private final double step;
		private final String format;
		private final DoubleConsumer setter;

		ValueSlider(int x, int y, int width, int height, String label, double min, double max, double step,
					double initial, String format, DoubleConsumer setter) {
			super(x, y, width, height, Text.empty(), Math.max(0.0, Math.min(1.0, (initial - min) / (max - min))));
			this.label = label;
			this.min = min;
			this.max = max;
			this.step = step;
			this.format = format;
			this.setter = setter;
			this.updateMessage();
		}

		private double current() {
			double raw = min + this.value * (max - min);
			double stepped = Math.round(raw / step) * step;
			return Math.max(min, Math.min(max, stepped));
		}

		@Override
		protected void updateMessage() {
			if (label == null) {
				return;
			}
			this.setMessage(Text.literal(label + ": " + String.format(Locale.ROOT, format, current())));
		}

		@Override
		protected void applyValue() {
			setter.accept(current());
		}
	}
}
