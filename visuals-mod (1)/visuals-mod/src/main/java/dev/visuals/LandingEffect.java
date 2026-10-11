package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Wykrywa lądowanie gracza i tworzy w tym miejscu świecące kółka (rysuje je {@link GlowFx}). */
public final class LandingEffect {
	private static boolean wasOnGround = true;
	private static double peakY = 0.0;

	private LandingEffect() {
	}

	public static void reset() {
		wasOnGround = true;
	}

	/** Tworzy kółka w podanym miejscu (używane też przez przycisk testowy w menu). */
	public static void spawnRing(double x, double y, double z) {
		VisualsConfig cfg = VisualsConfig.get();
		GlowFx.landing(x, y, z, cfg.landingColor, cfg.landingRadius, cfg.landingDuration * 50L);
	}

	public static void tick(MinecraftClient mc) {
		VisualsConfig cfg = VisualsConfig.get();
		ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}

		boolean onGround = player.isOnGround();

		if (player.isTouchingWater() || player.hasVehicle()) {
			// Woda i pojazdy resetują wysokość upadku, żeby nie robić kółek przy wychodzeniu z wody.
			peakY = player.getY();
		} else if (!onGround) {
			peakY = wasOnGround ? player.getY() : Math.max(peakY, player.getY());
		} else if (!wasOnGround && cfg.landingEnabled) {
			double fall = peakY - player.getY();
			if (fall >= cfg.landingMinFall) {
				spawnRing(player.getX(), player.getY(), player.getZ());
			}
		}

		wasOnGround = onGround;
	}
}
