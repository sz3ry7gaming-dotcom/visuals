package dev.visuals;

import java.util.ArrayList;
import java.util.List;

/** Historia pozycji gracza i budowanie z niej świecącej wstęgi lub helisy. */
final class GlowTrail {
	private static final double MIN_SPACING = 0.08;
	private static final int MAX_SAMPLES = 700;
	private static final List<double[]> SAMPLES = new ArrayList<>();

	private GlowTrail() {
	}

	static void clear() {
		SAMPLES.clear();
	}

	/** Dopisuje punkt, jeśli gracz oddalił się od poprzedniego, i usuwa za stare. */
	static void sample(double x, double y, double z, long now, long lifeMs) {
		while (!SAMPLES.isEmpty() && now - (long) SAMPLES.get(0)[3] > lifeMs) {
			SAMPLES.remove(0);
		}
		if (SAMPLES.isEmpty()) {
			SAMPLES.add(new double[]{x, y, z, now});
			return;
		}
		double[] last = SAMPLES.get(SAMPLES.size() - 1);
		double dx = x - last[0];
		double dy = y - last[1];
		double dz = z - last[2];
		if (dx * dx + dy * dy + dz * dz >= MIN_SPACING * MIN_SPACING) {
			SAMPLES.add(new double[]{x, y, z, now});
			if (SAMPLES.size() > MAX_SAMPLES) {
				SAMPLES.remove(0);
			}
		}
	}

	static void render(GlowBatch b, long now, long lifeMs, double headX, double headY, double headZ,
					   TrailType type, double halfWidth, ColorPreset color,
					   double camX, double camY, double camZ) {
		int n = SAMPLES.size() + 1;
		if (n < 3) {
			return;
		}
		double[] px = new double[n];
		double[] py = new double[n];
		double[] pz = new double[n];
		float[] age = new float[n];
		for (int i = 0; i < n - 1; i++) {
			double[] s = SAMPLES.get(i);
			px[i] = s[0];
			py[i] = s[1];
			pz[i] = s[2];
			age[i] = (float) ((now - (long) s[3]) / (double) lifeMs);
		}
		px[n - 1] = headX;
		py[n - 1] = headY;
		pz[n - 1] = headZ;
		age[n - 1] = 0f;

		if (type == TrailType.HELIX) {
			helix(b, px, py, pz, age, n, now, halfWidth * 0.55, color, camX, camY, camZ);
		} else {
			Shapes.ribbon(b, px, py, pz, age, n, camX, camY, camZ, halfWidth, color, 0.95f, 0f);
		}
	}

	private static void helix(GlowBatch b, double[] px, double[] py, double[] pz, float[] age, int n, long now,
							  double halfWidth, ColorPreset color, double camX, double camY, double camZ) {
		double spin = now * 0.004;
		for (int strand = 0; strand < 2; strand++) {
			double[] hx = new double[n];
			double[] hy = new double[n];
			double[] hz = new double[n];
			for (int i = 0; i < n; i++) {
				int prev = Math.max(0, i - 1);
				int next = Math.min(n - 1, i + 1);
				double tx = px[next] - px[prev];
				double tz = pz[next] - pz[prev];
				// Kierunek poziomo w bok od ruchu.
				double sx = tz;
				double sz = -tx;
				double len = Math.sqrt(sx * sx + sz * sz);
				if (len < 1e-9) {
					sx = 1.0;
					sz = 0.0;
					len = 1.0;
				}
				sx /= len;
				sz /= len;

				double angle = i * 0.55 + spin + strand * Math.PI;
				double radius = 0.34 * Math.pow(1.0 - Math.min(1f, age[i]), 0.5);
				hx[i] = px[i] + sx * Math.cos(angle) * radius;
				hy[i] = py[i] + Math.sin(angle) * radius;
				hz[i] = pz[i] + sz * Math.cos(angle) * radius;
			}
			Shapes.ribbon(b, hx, hy, hz, age, n, camX, camY, camZ, halfWidth, color, 0.95f, strand * 0.5f);
		}
	}
}
