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
 * Chiński kapelusz (stożkowy) na głowie gracza. Geometria jest rysowana z kodu,
 * więc mod nie potrzebuje żadnych własnych modeli ani tekstur.
 */
public class HatFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	private static final Identifier WHITE = Identifier.ofVanilla("textures/misc/white.png");

	/** Profil kapelusza: {promień, wysokość}. W przestrzeni modelu oś Y rośnie w dół, głowa ma górę na y = -0.5. */
	private static final float[][] PROFILE = {
		{0.00f, -0.86f},
		{0.12f, -0.78f},
		{0.27f, -0.66f},
		{0.45f, -0.56f},
		{0.66f, -0.47f}
	};
	private static final int SEGMENTS = 20;

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

		matrices.push();
		// Dzięki temu kapelusz podąża za ruchem głowy (obrót, kucanie).
		this.getContextModel().head.applyTransform(matrices);
		queue.getBatchingQueue(0).submitCustom(matrices, RenderLayers.entityTranslucent(WHITE),
			(entry, consumer) -> buildHat(entry, consumer, rgb, size, light));
		matrices.pop();
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

				// Naprzemienne paski i lekkie przyciemnienie ku brzegowi dają wrażenie plecionki.
				float shade = (s % 2 == 0 ? 1.0f : 0.8f) * (1.0f - 0.12f * i / (rings - 1));
				int red = (int) (((rgb >> 16) & 0xFF) * shade);
				int green = (int) (((rgb >> 8) & 0xFF) * shade);
				int blue = (int) ((rgb & 0xFF) * shade);

				// Czworokąt rysowany w obie strony, żeby kapelusz był widoczny z dołu i z góry.
				quad(consumer, entry, red, green, blue, light, nx, normalY, nz,
					r0 * c0, y0, r0 * s0,
					r0 * c1, y0, r0 * s1,
					r1 * c1, y1, r1 * s1,
					r1 * c0, y1, r1 * s0);
				quad(consumer, entry, red, green, blue, light, -nx, -normalY, -nz,
					r1 * c0, y1, r1 * s0,
					r1 * c1, y1, r1 * s1,
					r0 * c1, y0, r0 * s1,
					r0 * c0, y0, r0 * s0);
			}
		}
	}

	private static void quad(VertexConsumer consumer, MatrixStack.Entry entry, int r, int g, int b, int light,
							 float nx, float ny, float nz,
							 float x1, float y1, float z1, float x2, float y2, float z2,
							 float x3, float y3, float z3, float x4, float y4, float z4) {
		vertex(consumer, entry, r, g, b, light, nx, ny, nz, x1, y1, z1);
		vertex(consumer, entry, r, g, b, light, nx, ny, nz, x2, y2, z2);
		vertex(consumer, entry, r, g, b, light, nx, ny, nz, x3, y3, z3);
		vertex(consumer, entry, r, g, b, light, nx, ny, nz, x4, y4, z4);
	}

	private static void vertex(VertexConsumer consumer, MatrixStack.Entry entry, int r, int g, int b, int light,
							   float nx, float ny, float nz, float x, float y, float z) {
		consumer.vertex(entry, x, y, z)
			.color(r, g, b, 255)
			.texture(0.5f, 0.5f)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(light)
			.normal(entry, nx, ny, nz);
	}
}
