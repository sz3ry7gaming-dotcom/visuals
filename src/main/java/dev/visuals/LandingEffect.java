package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Kółka pokazujące, gdzie gracz wylądował. */
public final class LandingEffect {
	private static final List<Ring> RINGS = new ArrayList<>();
	private static boolean wasOnGround = true;
	private static double peakY = 0.0;

	private LandingEffect() {
	}

	private static final class Ring {
		final double x;
		final double y;
		final double z;
		int age = 0;

		Ring(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static void reset() {
		RINGS.clear();
		wasOnGround = true;
	}

	/** Tworzy kółko w podanym miejscu (używane też przez przycisk testowy w menu). */
	public static void spawnRing(double x, double y, double z) {
		RINGS.add(new Ring(x, y, z));
	}

	public static void tick(MinecraftClient mc) {
		VisualsConfig cfg = VisualsConfig.get();
		ClientPlayerEntity player = mc.player;
		if (player == null) {
			return;
		}

		detectLanding(player, cfg);
		renderRings(mc, cfg);
	}

	private static void detectLanding(ClientPlayerEntity player, VisualsConfig cfg) {
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

	private static void renderRings(MinecraftClient mc, VisualsConfig cfg) {
		if (RINGS.isEmpty()) {
			return;
		}

		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		int duration = cfg.landingDuration;
		Iterator<Ring> it = RINGS.iterator();
		while (it.hasNext()) {
			Ring ring = it.next();
			ring.age++;
			if (ring.age > duration) {
				it.remove();
				continue;
			}

			// Kółko szybko rośnie do pełnego promienia, potem utrzymuje się do końca czasu.
			float grow = Math.min(1f, ring.age / (duration * 0.3f));
			float eased = 1f - (1f - grow) * (1f - grow);
			double radius = cfg.landingRadius * eased;

			int points = 10 + (int) (radius * 12);
			double offset = rnd.nextDouble() * Math.PI * 2.0;
			for (int i = 0; i < points; i++) {
				double angle = offset + (Math.PI * 2.0 * i) / points;
				int rgb = cfg.landingColor.color(i / (float) points);
				double px = ring.x + Math.cos(angle) * radius;
				double pz = ring.z + Math.sin(angle) * radius;
				mc.particleManager.addParticle(new DustParticleEffect(rgb, 0.8f), px, ring.y + 0.08, pz, 0.0, 0.0, 0.0);
			}

			if (ring.age == 1) {
				// Mały podmuch w miejscu lądowania.
				for (int i = 0; i < 6; i++) {
					double vx = (rnd.nextDouble() - 0.5) * 0.2;
					double vz = (rnd.nextDouble() - 0.5) * 0.2;
					mc.particleManager.addParticle(ParticleTypes.CLOUD, ring.x, ring.y + 0.1, ring.z, vx, 0.02, vz);
				}
			}
		}
	}
}
