package dev.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Chiński kapelusz (stożkowy) na głowie gracza: plecionka z pasków, wklęsły profil, grubsza krawędź,
 * wnętrze z ciemniejszą podszewką, złota końcówka na czubku i troczki pod brodą.
 * Geometria jest rysowana z kodu, więc mod nie potrzebuje własnych modeli ani tekstur.
 */
public class HatFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	private static final Identifier WHITE = Identifier.ofVanilla("textures/misc/white.png");
	private static final int FULL_BRIGHT = 15728880;

	/** Profil kapelusza: {promień, wysokość}. W przestrzeni modelu oś Y rośnie w dół, głowa ma górę na y = -0.5. */
	private static final float[][] PROFILE = {
		{0.00f, -0.90f},
		{0.07f, -0.84f},
		{0.17f, -0.75f},
		{0.30f, -0.66f},
		{0.45f, -0.58f},
		{0.62f, -0.51f},
		{0.80f, -0.46f}
	};
	private static final int SEGMENTS = 24;
	private static final int GOLD = 0xFFC83A;
	private static final int STRAP = 0x7A1414;

	public HatFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
					   PlayerEntityRenderState state, float limbAngle, float limbDistance) {
		VisualsConfig cfg = VisualsConfig.get();
		if (!cfg.hatEnabled || state.invisible) {
			return;
		}

		MinecraftClient mc = MinecraftClient.getInstance();
		boolean isLocalPlayer = mc.player != null && state.id == mc.player.getId();
		if (!isLocalPlayer && !cfg.hatOnOthers) {
			return;
		}

		int rgb = cfg.hatColor.color(0f);
		float size = (float) cfg.hatSize;
		int lightValue = cfg.hatGlow ? FULL_BRIGHT : light;

		matrices.push();
		// Dzięki temu kapelusz podąża za ruchem głowy (obrót, kucanie).
		this.getContextModel().head.applyTransform(matrices);
		queue.getBatchingQueue(0).submitCustom(matrices, RenderLayers.entityTranslucent(WHITE),
			(entry, consumer) -> buildHat(entry, consumer, rgb, size, lightValue));
		matrices.pop();
	}

	private static int shade(int rgb, float factor) {
		int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * factor));
		int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * factor));
		int b = Math.min(255, (int) ((rgb & 0xFF) * factor));
		return (r << 16) | (g << 8) | b;
	}

	private static void buildHat(MatrixStack.Entry entry, VertexConsumer consumer, int rgb, float size, int light) {
		int rings = PROFILE.length;
		float baseY = PROFILE[rings - 1][1];

		for (int i = 0; i < rings - 1; i++) {
			// Skalujemy promień oraz wysokość względem krawędzi, żeby kapelusz zawsze siedział na głowie.
			float r0 = PROFILE[i][0] * size;
			float r1 = PROFILE[i + 1][0] * size;
			float y0 = baseY + (PROFILE[i][1] - baseY) * size;
			float y1 = baseY + (PROFILE[i + 1][1] - baseY) * size;

			// Normalna powierzchni (do góry i na zewnątrz) w przekroju promień/wysokość.
			float dr = r1 - r0;
			float dy = y1 - y0;
			float len = (float) Math.sqrt(dr * dr + dy * dy);
			float normalRadial = dy / len;
			float normalY = -dr / len;

			// Poziome pasy plecionki: co drugi pas jaśniejszy.
			float band = (i % 2 == 0) ? 1.0f : 0.86f;

			for (int s = 0; s < SEGMENTS; s++) {
				double a0 = Math.PI * 2.0 * s / SEGMENTS;
				double a1 = Math.PI * 2.0 * (s + 1) / SEGMENTS;
				double am = (a0 + a1) * 0.5;
				float c0 = (float) Math.cos(a0);
				float s0 = (float) Math.sin(a0);
				float c1 = (float) Math.cos(a1);
				float s1 = (float) Math.sin(a1);
				float nx = normalRadial * (float) Math.cos(am);
				float nz = normalRadial * (float) Math.sin(am);

				// Kratka: naprzemienne pionowe paski nakładają się na poziome pasy.
				float weave = (s % 2 == 0 ? 1.0f : 0.84f) * band;
				int top = shade(rgb, weave);
				int lining = shade(rgb, weave * 0.5f);

				// Zewnętrzna strona.
				quad(consumer, entry, top, light, nx, normalY, nz,
					r0 * c0, y0, r0 * s0,
					r0 * c1, y0, r0 * s1,
					r1 * c1, y1, r1 * s1,
					r1 * c0, y1, r1 * s0);
				// Wnętrze (ciemniejsza podszewka).
				quad(consumer, entry, lining, light, -nx, -normalY, -nz,
					r1 * c0, y1, r1 * s0,
					r1 * c1, y1, r1 * s1,
					r0 * c1, y0, r0 * s1,
					r0 * c0, y0, r0 * s0);
			}
		}

		// Grubsza krawędź na obrzeżu.
		float rb = PROFILE[rings - 1][0] * size;
		float edge = 0.035f * size;
		int edgeColor = shade(rgb, 0.7f);
		for (int s = 0; s < SEGMENTS; s++) {
			double a0 = Math.PI * 2.0 * s / SEGMENTS;
			double a1 = Math.PI * 2.0 * (s + 1) / SEGMENTS;
			float c0 = (float) Math.cos(a0);
			float s0 = (float) Math.sin(a0);
			float c1 = (float) Math.cos(a1);
			float s1 = (float) Math.sin(a1);
			float nx = (float) Math.cos((a0 + a1) * 0.5);
			float nz = (float) Math.sin((a0 + a1) * 0.5);
			quad(consumer, entry, edgeColor, light, nx, 0f, nz,
				rb * c0, baseY, rb * s0,
				rb * c1, baseY, rb * s1,
				rb * c1, baseY + edge, rb * s1,
				rb * c0, baseY + edge, rb * s0);
			quad(consumer, entry, edgeColor, light, -nx, 0f, -nz,
				rb * c0, baseY + edge, rb * s0,
				rb * c1, baseY + edge, rb * s1,
				rb * c1, baseY, rb * s1,
				rb * c0, baseY, rb * s0);
		}

		// Złota końcówka na czubku: mały ośmiościan.
		float tipY = PROFILE[0][1];
		float tipH = 0.075f * size;
		float tipR = 0.04f * size;
		for (int s = 0; s < 4; s++) {
			double a0 = Math.PI * 0.5 * s;
			double a1 = Math.PI * 0.5 * (s + 1);
			float c0 = (float) Math.cos(a0) * tipR;
			float s0 = (float) Math.sin(a0) * tipR;
			float c1 = (float) Math.cos(a1) * tipR;
			float s1 = (float) Math.sin(a1) * tipR;
			float nx = (float) Math.cos((a0 + a1) * 0.5);
			float nz = (float) Math.sin((a0 + a1) * 0.5);
			int gold = shade(GOLD, s % 2 == 0 ? 1.0f : 0.88f);
			// Górna piramidka i dolna piramidka.
			quad(consumer, entry, gold, light, nx, -0.6f, nz,
				c0, tipY, s0, c1, tipY, s1, 0f, tipY - tipH, 0f, 0f, tipY - tipH, 0f);
			quad(consumer, entry, gold, light, nx, 0.6f, nz,
				c1, tipY, s1, c0, tipY, s0, 0f, tipY + tipH * 0.5f, 0f, 0f, tipY + tipH * 0.5f, 0f);
		}

		// Troczki pod brodą: dwa paski po bokach głowy.
		strap(consumer, entry, light, -1f, size);
		strap(consumer, entry, light, 1f, size);
	}

	/** Pasek ze skraju kapelusza w dół po policzku, na zewnątrz głowy (głowa ma szerokość ±0.25). */
	private static void strap(VertexConsumer consumer, MatrixStack.Entry entry, int light, float side, float size) {
		float x = side * 0.27f;
		float zFront = 0.10f;
		float zBack = 0.04f;
		float top = PROFILE[PROFILE.length - 1][1] + 0.03f;
		float bottom = 0.0f;
		quad(consumer, entry, STRAP, light, side, 0f, 0f,
			x, top, zBack, x, top, zFront, x, bottom, zFront, x, bottom, zBack);
		quad(consumer, entry, STRAP, light, -side, 0f, 0f,
			x, bottom, zBack, x, bottom, zFront, x, top, zFront, x, top, zBack);
	}

	private static void quad(VertexConsumer consumer, MatrixStack.Entry entry, int rgb, int light,
							 float nx, float ny, float nz,
							 float x1, float y1, float z1, float x2, float y2, float z2,
							 float x3, float y3, float z3, float x4, float y4, float z4) {
		vertex(consumer, entry, rgb, light, nx, ny, nz, x1, y1, z1);
		vertex(consumer, entry, rgb, light, nx, ny, nz, x2, y2, z2);
		vertex(consumer, entry, rgb, light, nx, ny, nz, x3, y3, z3);
		vertex(consumer, entry, rgb, light, nx, ny, nz, x4, y4, z4);
	}

	private static void vertex(VertexConsumer consumer, MatrixStack.Entry entry, int rgb, int light,
							   float nx, float ny, float nz, float x, float y, float z) {
		consumer.vertex(entry, x, y, z)
			.color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255)
			.texture(0.5f, 0.5f)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(light)
			.normal(entry, nx, ny, nz);
	}
}
