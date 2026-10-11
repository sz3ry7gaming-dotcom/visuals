package dev.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Rysuje własne efekty w świecie (wstęga, kółka, błyski) co klatkę.
 * Cała geometria trafia do jednej świecącej warstwy "lightning" jako kolorowe czworokąty.
 */
public final class VisualsRenderer {
	private static final GlowBatch BATCH = new GlowBatch();
	private static long lastFrame = 0L;

	private VisualsRenderer() {
	}

	public static void init() {
		WorldRenderEvents.BEFORE_ENTITIES.register(VisualsRenderer::render);
	}

	/** Czyści wszystkie efekty (np. po wyjściu ze świata). */
	public static void reset() {
		GlowTrail.clear();
		GlowFx.clear();
		GlowParticles.clear();
		lastFrame = 0L;
	}

	private static void render(WorldRenderContext context) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity player = mc.player;
		if (player == null || mc.world == null) {
			return;
		}

		VisualsConfig cfg = VisualsConfig.get();
		long now = System.currentTimeMillis();
		float dt = lastFrame == 0L ? 0f : Math.min(0.1f, (now - lastFrame) / 1000f);
		lastFrame = now;

		CameraRenderState camera = context.worldState().cameraRenderState;
		Vec3d cam = camera.pos;
		Vector3f right = new Vector3f(1f, 0f, 0f).rotate(camera.orientation);
		Vector3f up = new Vector3f(0f, 1f, 0f).rotate(camera.orientation);

		BATCH.begin(cam.x, cam.y, cam.z, (float) cfg.glowIntensity);

		// Wstęga / helisa za graczem.
		if (cfg.trailEnabled && cfg.trailType != null && (cfg.trailType == TrailType.RIBBON || cfg.trailType == TrailType.HELIX)
			&& !player.isSpectator()) {
			float tickProgress = mc.getRenderTickCounter().getTickProgress(false);
			Vec3d p = player.getLerpedPos(tickProgress);
			double hy = p.y + cfg.trailHeight;
			long life = cfg.trailLength * 50L;
			GlowTrail.sample(p.x, hy, p.z, now, life);
			GlowTrail.render(BATCH, now, life, p.x, hy, p.z, cfg.trailType, cfg.trailWidth, cfg.trailColor,
				cam.x, cam.y, cam.z);
		} else {
			GlowTrail.clear();
		}

		GlowFx.build(BATCH, now, right.x, right.y, right.z, up.x, up.y, up.z);
		GlowParticles.update(dt);
		GlowParticles.build(BATCH, right.x, right.y, right.z, up.x, up.y, up.z);

		int count = BATCH.count();
		if (count == 0) {
			return;
		}
		float[] pos = BATCH.positions();
		int[] col = BATCH.colors();
		context.commandQueue().getBatchingQueue(0).submitCustom(context.matrices(), RenderLayers.lightning(),
			(entry, consumer) -> {
				for (int i = 0; i < count; i++) {
					consumer.vertex(entry, pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2]).color(col[i]);
				}
			});
	}
}
