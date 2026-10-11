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
		TRAIL("Trail", "Kolor działa tylko dla trybu \"Kolorowy pył\"."),
		KILL("Kill effect", "Efekt pojawia się, gdy zabijesz kogoś, w kogo uderzyłeś."),
		LANDING("Lądowanie", "Kółko pojawia się w miejscu, gdzie wylądowałeś.");

		final String label;
		final String hint;

		Tab(String label, String hint) {
			this.label = label;
			this.hint = hint;
		}
	}

	private static Tab lastTab = Tab.HAT;
	private Tab tab = lastTab;

	public ConfigScreen() {
		super(Text.literal("Visuals"));
	}

	@Override
	protected void init() {
		VisualsConfig cfg = VisualsConfig.get();

		// Zakładki
		Tab[] tabs = Tab.values();
		int gap = 4;
		int tabWidth = Math.min(100, (this.width - 20 - gap * (tabs.length - 1)) / tabs.length);
		int tabsTotal = tabWidth * tabs.length + gap * (tabs.length - 1);
		int tabsX = (this.width - tabsTotal) / 2;
		for (int i = 0; i < tabs.length; i++) {
			Tab t = tabs[i];
			String label = (t == tab ? "» " : "") + t.label;
			this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
				tab = t;
				lastTab = t;
				this.clearAndInit();
			}).dimensions(tabsX + i * (tabWidth + gap), 28, tabWidth, 20).build());
		}

		// Zawartość zakładki
		int w = Math.min(240, this.width - 20);
		int x = (this.width - w) / 2;
		int top = 58;
		int step = 24;

		switch (tab) {
			case HAT -> {
				addToggle(x, top, w, "Chinese hat", () -> cfg.hatEnabled, v -> cfg.hatEnabled = v);
				addCycle(x, top + step, w, "Kolor", ColorPreset.values(), () -> cfg.hatColor, v -> cfg.hatColor = v, c -> c.label);
				addSlider(x, top + step * 2, w, "Rozmiar", 0.5, 1.8, 0.05, "%.2fx", () -> cfg.hatSize, v -> cfg.hatSize = v);
				addToggle(x, top + step * 3, w, "Widoczny u innych graczy", () -> cfg.hatOnOthers, v -> cfg.hatOnOthers = v);
			}
			case TRAIL -> {
				addToggle(x, top, w, "Trail", () -> cfg.trailEnabled, v -> cfg.trailEnabled = v);
				addCycle(x, top + step, w, "Rodzaj", TrailType.values(), () -> cfg.trailType, v -> cfg.trailType = v, t -> t.label);
				addCycle(x, top + step * 2, w, "Kolor", ColorPreset.values(), () -> cfg.trailColor, v -> cfg.trailColor = v, c -> c.label);
				addSlider(x, top + step * 3, w, "Gęstość", 1, 6, 1, "%.0f", () -> cfg.trailDensity, v -> cfg.trailDensity = (int) Math.round(v));
				addToggle(x, top + step * 4, w, "Tylko podczas ruchu", () -> cfg.trailOnlyMoving, v -> cfg.trailOnlyMoving = v);
			}
			case KILL -> {
				addToggle(x, top, w, "Kill effect", () -> cfg.killEnabled, v -> cfg.killEnabled = v);
				addCycle(x, top + step, w, "Rodzaj", KillEffectType.values(), () -> cfg.killType, v -> cfg.killType = v, t -> t.label);
				addCycle(x, top + step * 2, w, "Kolor", ColorPreset.values(), () -> cfg.killColor, v -> cfg.killColor = v, c -> c.label);
				addToggle(x, top + step * 3, w, "Tylko po zabiciu graczy", () -> cfg.killPlayersOnly, v -> cfg.killPlayersOnly = v);
				this.addDrawableChild(ButtonWidget.builder(Text.literal("Test (zamyka menu)"), button -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					ClientPlayerEntity player = mc.player;
					if (player != null) {
						double yaw = Math.toRadians(player.getYaw());
						double px = player.getX() - Math.sin(yaw) * 3.0;
						double pz = player.getZ() + Math.cos(yaw) * 3.0;
						mc.setScreen(null);
						KillEffect.spawn(mc, px, player.getY(), pz, 1.8);
					}
				}).dimensions(x, top + step * 4, w, 20).build());
			}
			case LANDING -> {
				addToggle(x, top, w, "Kółka lądowania", () -> cfg.landingEnabled, v -> cfg.landingEnabled = v);
				addCycle(x, top + step, w, "Kolor", ColorPreset.values(), () -> cfg.landingColor, v -> cfg.landingColor = v, c -> c.label);
				addSlider(x, top + step * 2, w, "Promień", 0.5, 4.0, 0.1, "%.1f", () -> cfg.landingRadius, v -> cfg.landingRadius = v);
				addSlider(x, top + step * 3, w, "Czas", 0.5, 8.0, 0.5, "%.1f s", () -> cfg.landingDuration / 20.0, v -> cfg.landingDuration = (int) Math.round(v * 20.0));
				addSlider(x, top + step * 4, w, "Min. wysokość upadku", 0.0, 5.0, 0.5, "%.1f", () -> cfg.landingMinFall, v -> cfg.landingMinFall = v);
				this.addDrawableChild(ButtonWidget.builder(Text.literal("Test (zamyka menu)"), button -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					ClientPlayerEntity player = mc.player;
					if (player != null) {
						double yaw = Math.toRadians(player.getYaw());
						double px = player.getX() - Math.sin(yaw) * 3.0;
						double pz = player.getZ() + Math.cos(yaw) * 3.0;
						mc.setScreen(null);
						LandingEffect.spawnRing(px, player.getY(), pz);
					}
				}).dimensions(x, top + step * 5, w, 20).build());
			}
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), button -> this.close())
			.dimensions(x, this.height - 28, w, 20).build());
	}

	private void addToggle(int x, int y, int w, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		this.addDrawableChild(ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), button -> {
			boolean value = !getter.getAsBoolean();
			setter.accept(value);
			button.setMessage(toggleText(label, value));
		}).dimensions(x, y, w, 20).build());
	}

	private static Text toggleText(String label, boolean value) {
		return Text.literal(label + ": " + (value ? "WŁ." : "WYŁ."));
	}

	private <E extends Enum<E>> void addCycle(int x, int y, int w, String label, E[] values,
											  Supplier<E> getter, Consumer<E> setter, Function<E, String> name) {
		this.addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + name.apply(getter.get())), button -> {
			E next = values[(getter.get().ordinal() + 1) % values.length];
			setter.accept(next);
			button.setMessage(Text.literal(label + ": " + name.apply(next)));
		}).dimensions(x, y, w, 20).build());
	}

	private void addSlider(int x, int y, int w, String label, double min, double max, double step,
						   String format, DoubleSupplier getter, DoubleConsumer setter) {
		this.addDrawableChild(new ValueSlider(x, y, w, 20, label, min, max, step, getter.getAsDouble(), format, setter));
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
