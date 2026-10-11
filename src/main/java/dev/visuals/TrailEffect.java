package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

/** Ślad cząsteczek za lokalnym graczem. */
public final class TrailEffect {
	private TrailEffect() {
	}

	public static void tick(MinecraftClient mc) {
		VisualsConfig cfg = VisualsConfig.get();
		ClientPlayerEntity player = mc.player;
		if (!cfg.trailEnabled || player == null || player.isSpectator()) {
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
			int rgb = cfg.trailColor.color(i * 0.07f);
			ParticleEffect effect = cfg.trailType.create(rgb);
			double x = player.getX() + (rnd.nextDouble() - 0.5) * 0.4;
			double y = player.getY() + 0.1 + rnd.nextDouble() * 0.25;
			double z = player.getZ() + (rnd.nextDouble() - 0.5) * 0.4;
			mc.particleManager.addParticle(effect, x, y, z, 0.0, 0.01, 0.0);
		}
	}
}
