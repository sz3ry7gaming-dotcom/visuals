package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Część śladu zależna od ticków: "pył gwiezdny" (własne błyski) oraz klasyczne cząsteczki Minecrafta.
 * Wstęga i helisa są rysowane co klatkę w {@link VisualsRenderer}.
 */
public final class TrailEffect {
	private TrailEffect() {
	}

	public static void tick(MinecraftClient mc) {
		VisualsConfig cfg = VisualsConfig.get();
		ClientPlayerEntity player = mc.player;
		if (!cfg.trailEnabled || player == null || player.isSpectator()) {
			return;
		}
		TrailType type = cfg.trailType;
		if (type == TrailType.RIBBON || type == TrailType.HELIX) {
			return;
		}

		Vec3d v = player.getVelocity();
		boolean movingHorizontally = v.x * v.x + v.z * v.z > 0.0006;
		boolean fallingOrFlying = !player.isOnGround() && Math.abs(v.y) > 0.2;
		if (cfg.trailOnlyMoving && !movingHorizontally && !fallingOrFlying) {
			return;
		}

		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		for (int i = 0; i < cfg.trailDensity; i++) {
			double x = player.getX() + (rnd.nextDouble() - 0.5) * 0.5;
			double y = player.getY() + 0.1 + rnd.nextDouble() * cfg.trailHeight;
			double z = player.getZ() + (rnd.nextDouble() - 0.5) * 0.5;

			if (type == TrailType.STARDUST) {
				int rgb = cfg.trailColor.color(rnd.nextFloat());
				GlowParticles.sparkle(x, y, z,
					(rnd.nextDouble() - 0.5) * 0.5, 0.15 + rnd.nextDouble() * 0.3, (rnd.nextDouble() - 0.5) * 0.5,
					0.2, 0.13f + rnd.nextFloat() * 0.14f, 0.7f + rnd.nextFloat() * 0.6f, rgb);
			} else {
				int rgb = cfg.trailColor.color(i * 0.07f);
				ParticleEffect effect = type.create(rgb);
				mc.particleManager.addParticle(effect, x, y, z, 0.0, 0.01, 0.0);
			}
		}
	}
}
