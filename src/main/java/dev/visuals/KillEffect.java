package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Efekt po zabiciu. Gracz zapamiętuje, w kogo uderzył, a gdy ta istota umiera,
 * w jej miejscu pojawia się wybrany efekt.
 */
public final class KillEffect {
	private static final Map<Integer, Tracked> TRACKED = new HashMap<>();
	private static long tickCounter = 0;
	private static int fakeEntityId = 0;

	private KillEffect() {
	}

	private static final class Tracked {
		double x;
		double y;
		double z;
		long lastHit;

		Tracked(double x, double y, double z, long lastHit) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.lastHit = lastHit;
		}
	}

	public static void reset() {
		TRACKED.clear();
	}

	public static void onAttack(Entity target) {
		if (!(target instanceof LivingEntity)) {
			return;
		}
		Tracked tracked = TRACKED.get(target.getId());
		if (tracked == null) {
			TRACKED.put(target.getId(), new Tracked(target.getX(), target.getY(), target.getZ(), tickCounter));
		} else {
			tracked.lastHit = tickCounter;
		}
	}

	public static void tick(MinecraftClient mc) {
		tickCounter++;
		if (mc.world == null || TRACKED.isEmpty()) {
			return;
		}

		VisualsConfig cfg = VisualsConfig.get();
		Iterator<Map.Entry<Integer, Tracked>> it = TRACKED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Tracked> entry = it.next();
			Tracked tracked = entry.getValue();

			if (tickCounter - tracked.lastHit > 100) {
				it.remove();
				continue;
			}

			Entity entity = mc.world.getEntityById(entry.getKey());
			if (!(entity instanceof LivingEntity living)) {
				continue;
			}

			tracked.x = living.getX();
			tracked.y = living.getY();
			tracked.z = living.getZ();

			if (living.isDead()) {
				it.remove();
				if (cfg.killEnabled && (!cfg.killPlayersOnly || living instanceof PlayerEntity)) {
					spawn(mc, tracked.x, tracked.y, tracked.z, living.getHeight());
				}
			}
		}
	}

	/** Odtwarza wybrany efekt w danym miejscu (x, y, z to pozycja stóp). */
	public static void spawn(MinecraftClient mc, double x, double y, double z, double height) {
		VisualsConfig cfg = VisualsConfig.get();
		ParticleManager pm = mc.particleManager;
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		double cy = y + height * 0.5;

		switch (cfg.killType) {
			case LIGHTNING -> {
				if (mc.world != null) {
					LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(mc.world, SpawnReason.EVENT);
					if (bolt != null) {
						// Wysokie, nietypowe ID, żeby nie kolidować z ID z serwera.
						bolt.setId(0x40000000 + (fakeEntityId++ & 0xFFFFF));
						bolt.setPosition(x, y, z);
						mc.world.addEntity(bolt);
					}
				}
			}
			case TOTEM -> {
				for (int i = 0; i < 40; i++) {
					pm.addParticle(ParticleTypes.TOTEM_OF_UNDYING, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.9, rnd.nextDouble() * 0.8, (rnd.nextDouble() - 0.5) * 0.9);
				}
			}
			case FLAMES -> {
				for (int i = 0; i < 35; i++) {
					pm.addParticle(ParticleTypes.FLAME, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.3, 0.05 + rnd.nextDouble() * 0.25, (rnd.nextDouble() - 0.5) * 0.3);
				}
				for (int i = 0; i < 8; i++) {
					pm.addParticle(ParticleTypes.LARGE_SMOKE, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.1, 0.1, (rnd.nextDouble() - 0.5) * 0.1);
				}
			}
			case SOULS -> {
				for (int i = 0; i < 25; i++) {
					pm.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.2, 0.05 + rnd.nextDouble() * 0.2, (rnd.nextDouble() - 0.5) * 0.2);
				}
				for (int i = 0; i < 8; i++) {
					pm.addParticle(ParticleTypes.SOUL, x, cy, z, 0.0, 0.06 + rnd.nextDouble() * 0.06, 0.0);
				}
			}
			case HEARTS -> {
				for (int i = 0; i < 14; i++) {
					pm.addParticle(ParticleTypes.HEART,
						x + (rnd.nextDouble() - 0.5) * 0.8, cy + rnd.nextDouble() * 0.6, z + (rnd.nextDouble() - 0.5) * 0.8,
						0.0, 0.05, 0.0);
				}
			}
			case BLOOD -> {
				for (int i = 0; i < 45; i++) {
					pm.addParticle(new DustParticleEffect(0xB00000, 1.4f), x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.5, rnd.nextDouble() * 0.3, (rnd.nextDouble() - 0.5) * 0.5);
				}
				for (int i = 0; i < 10; i++) {
					pm.addParticle(ParticleTypes.CRIT, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.6, rnd.nextDouble() * 0.4, (rnd.nextDouble() - 0.5) * 0.6);
				}
			}
			case SPIRAL -> {
				for (int i = 0; i < 70; i++) {
					double angle = i * 0.45;
					double radius = 0.7 - i * 0.005;
					int rgb = cfg.killColor.color(i / 70f);
					pm.addParticle(new DustParticleEffect(rgb, 1.0f),
						x + Math.cos(angle) * radius, y + i * 0.03, z + Math.sin(angle) * radius,
						0.0, 0.01, 0.0);
				}
			}
			case FIREWORK -> {
				for (int i = 0; i < 60; i++) {
					// Losowy kierunek na sferze.
					double theta = rnd.nextDouble() * Math.PI * 2.0;
					double cosPhi = rnd.nextDouble() * 2.0 - 1.0;
					double sinPhi = Math.sqrt(1.0 - cosPhi * cosPhi);
					int rgb = cfg.killColor.color(rnd.nextFloat());
					pm.addParticle(new DustParticleEffect(rgb, 1.2f), x, cy, z,
						Math.cos(theta) * sinPhi * 0.6, cosPhi * 0.6, Math.sin(theta) * sinPhi * 0.6);
				}
				for (int i = 0; i < 15; i++) {
					pm.addParticle(ParticleTypes.FIREWORK, x, cy, z,
						(rnd.nextDouble() - 0.5) * 0.5, (rnd.nextDouble() - 0.5) * 0.5, (rnd.nextDouble() - 0.5) * 0.5);
				}
			}
		}
	}
}
