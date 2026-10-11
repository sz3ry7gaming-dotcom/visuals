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

	// Chinese hat
	public boolean hatEnabled = true;
	public boolean hatOnOthers = false;
	public ColorPreset hatColor = ColorPreset.RED;
	public double hatSize = 1.0;

	// Trail
	public boolean trailEnabled = true;
	public TrailType trailType = TrailType.DUST;
	public ColorPreset trailColor = ColorPreset.RAINBOW;
	public int trailDensity = 2;
	public boolean trailOnlyMoving = true;

	// Kill effect
	public boolean killEnabled = true;
	public KillEffectType killType = KillEffectType.LIGHTNING;
	public ColorPreset killColor = ColorPreset.RED;
	public boolean killPlayersOnly = false;

	// Kółka lądowania
	public boolean landingEnabled = true;
	public ColorPreset landingColor = ColorPreset.CYAN;
	public double landingRadius = 1.6;
	public int landingDuration = 40;
	public double landingMinFall = 0.5;

	public static VisualsConfig get() {
		return instance;
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
		if (trailType == null) trailType = TrailType.DUST;
		if (trailColor == null) trailColor = ColorPreset.RAINBOW;
		if (killType == null) killType = KillEffectType.LIGHTNING;
		if (killColor == null) killColor = ColorPreset.RED;
		if (landingColor == null) landingColor = ColorPreset.CYAN;
		hatSize = Math.max(0.5, Math.min(1.8, hatSize));
		trailDensity = Math.max(1, Math.min(6, trailDensity));
		landingRadius = Math.max(0.5, Math.min(5.0, landingRadius));
		landingDuration = Math.max(10, Math.min(200, landingDuration));
		landingMinFall = Math.max(0.0, Math.min(10.0, landingMinFall));
	}
}
