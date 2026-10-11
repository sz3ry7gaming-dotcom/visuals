package dev.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ustawienia moda zapisywane w config/visuals.json. */
public class VisualsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static VisualsConfig instance = new VisualsConfig();

	// Ogólne
	public double glowIntensity = 1.0;

	// Chinese hat
	public boolean hatEnabled = true;
	public boolean hatOnOthers = false;
	public boolean hatGlow = false;
	public ColorPreset hatColor = ColorPreset.RED;
	public double hatSize = 1.0;

	// Trail
	public boolean trailEnabled = true;
	public TrailType trailType = TrailType.RIBBON;
	public ColorPreset trailColor = ColorPreset.RAINBOW;
	public int trailDensity = 2;
	public int trailLength = 30;
	public double trailWidth = 0.22;
	public double trailHeight = 0.9;
	public boolean trailOnlyMoving = true;

	// Kill effect
	public boolean killEnabled = true;
	public KillEffectType killType = KillEffectType.SHOCKWAVE;
	public ColorPreset killColor = ColorPreset.RED;
	public boolean killPlayersOnly = false;

	// Kółka lądowania
	public boolean landingEnabled = true;
	public ColorPreset landingColor = ColorPreset.CYAN;
	public double landingRadius = 1.8;
	public int landingDuration = 50;
	public double landingMinFall = 0.5;

	public static VisualsConfig get() {
		return instance;
	}

	public static void resetToDefaults() {
		instance = new VisualsConfig();
		save();
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("visuals.json");
	}

	public static void load() {
		Path path = path();
		if (Files.exists(path)) {
			try {
				VisualsConfig loaded = GSON.fromJson(Files.readString(path), VisualsConfig.class);
				if (loaded != null) {
					loaded.sanitize();
					instance = loaded;
					return;
				}
			} catch (Exception e) {
				VisualsClient.LOGGER.warn("Nie udało się wczytać visuals.json, używam domyślnych ustawień", e);
			}
		}
		instance = new VisualsConfig();
		save();
	}

	public static void save() {
		try {
			Files.writeString(path(), GSON.toJson(instance));
		} catch (IOException e) {
			VisualsClient.LOGGER.warn("Nie udało się zapisać visuals.json", e);
		}
	}

	/** Naprawia brakujące lub wyjątkowe wartości po ręcznej edycji pliku. */
	private void sanitize() {
		if (hatColor == null) hatColor = ColorPreset.RED;
		if (trailType == null) trailType = TrailType.RIBBON;
		if (trailColor == null) trailColor = ColorPreset.RAINBOW;
		if (killType == null) killType = KillEffectType.SHOCKWAVE;
		if (killColor == null) killColor = ColorPreset.RED;
		if (landingColor == null) landingColor = ColorPreset.CYAN;
		glowIntensity = clamp(glowIntensity, 0.2, 2.0);
		hatSize = clamp(hatSize, 0.5, 1.8);
		trailDensity = (int) clamp(trailDensity, 1, 6);
		trailLength = (int) clamp(trailLength, 10, 100);
		trailWidth = clamp(trailWidth, 0.05, 0.8);
		trailHeight = clamp(trailHeight, 0.05, 2.0);
		landingRadius = clamp(landingRadius, 0.5, 5.0);
		landingDuration = (int) clamp(landingDuration, 10, 200);
		landingMinFall = clamp(landingMinFall, 0.0, 10.0);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}
}
