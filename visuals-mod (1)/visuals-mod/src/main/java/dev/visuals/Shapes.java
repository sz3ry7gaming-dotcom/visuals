package dev.visuals;

/** Gotowe kształty świecące: pierścienie, dyski, kolumny, błyski, kule i wstęgi. */
final class Shapes {
	private Shapes() {
	}

	static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}

	static int argb(int rgb, float alpha) {
		int a = (int) (clamp01(alpha) * 255f + 0.5f);
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	/** Szybki start, łagodne hamowanie. */
	static float easeOut(float t) {
		float u = 1f - clamp01(t);
		return 1f - u * u * u;
	}

	/** 0 przed e0, 1 po e1, gładko pomiędzy. */
	static float smooth(float t, float e0, float e1) {
		float x = clamp01((t - e0) / (e1 - e0));
		return x * x * (3f - 2f * x);
	}

	/** Miękki pierścień leżący poziomo: środek pasa świeci, krawędzie zanikają. */
	static void softRing(GlowBatch b, double cx, double cy, double cz, double radius, double halfWidth,
						 int segments, double a0, double a1, ColorPreset color, float alpha, float phase) {
		double ri = Math.max(0.0, radius - halfWidth);
		double ro = radius + halfWidth;
		for (int s = 0; s < segments; s++) {
			double g0 = a0 + (a1 - a0) * s / segments;
			double g1 = a0 + (a1 - a0) * (s + 1) / segments;
			double c0 = Math.cos(g0);
			double s0 = Math.sin(g0);
			double c1 = Math.cos(g1);
			double s1 = Math.sin(g1);
			int rgb = color.color(phase + (float) s / segments);
			int mid = argb(rgb, alpha);
			int edge = argb(rgb, 0f);
			b.quad(cx + c0 * radius, cy, cz + s0 * radius, mid,
				cx + c0 * ro, cy, cz + s0 * ro, edge,
				cx + c1 * ro, cy, cz + s1 * ro, edge,
				cx + c1 * radius, cy, cz + s1 * radius, mid);
			b.quad(cx + c0 * ri, cy, cz + s0 * ri, edge,
				cx + c0 * radius, cy, cz + s0 * radius, mid,
				cx + c1 * radius, cy, cz + s1 * radius, mid,
				cx + c1 * ri, cy, cz + s1 * ri, edge);
		}
	}

	/** Poziomy dysk świecący od środka (segments musi być parzyste). */
	static void disc(GlowBatch b, double cx, double cy, double cz, double radius, int segments,
					 ColorPreset color, float alphaCenter, float phase) {
		for (int k = 0; k < segments / 2; k++) {
			int i0 = 2 * k;
			int i1 = 2 * k + 1;
			int i2 = 2 * k + 2;
			double g0 = Math.PI * 2.0 * i0 / segments;
			double g1 = Math.PI * 2.0 * i1 / segments;
			double g2 = Math.PI * 2.0 * i2 / segments;
			int rgb = color.color(phase + (float) i1 / segments);
			int center = argb(rgb, alphaCenter);
			int edge = argb(rgb, 0f);
			b.quad(cx, cy, cz, center,
				cx + Math.cos(g0) * radius, cy, cz + Math.sin(g0) * radius, edge,
				cx + Math.cos(g1) * radius, cy, cz + Math.sin(g1) * radius, edge,
				cx + Math.cos(g2) * radius, cy, cz + Math.sin(g2) * radius, edge);
		}
	}

	/** Pionowa kolumna światła: u dołu alfa bottom, u góry alfa top. */
	static void column(GlowBatch b, double cx, double y0, double cz, double radius, double height, int sides,
					   ColorPreset color, float alphaBottom, float alphaTop, float phase) {
		for (int s = 0; s < sides; s++) {
			double g0 = Math.PI * 2.0 * s / sides;
			double g1 = Math.PI * 2.0 * (s + 1) / sides;
			int rgb = color.color(phase + (float) s / sides);
			int bottom = argb(rgb, alphaBottom);
			int top = argb(rgb, alphaTop);
			double x0 = cx + Math.cos(g0) * radius;
			double z0 = cz + Math.sin(g0) * radius;
			double x1 = cx + Math.cos(g1) * radius;
			double z1 = cz + Math.sin(g1) * radius;
			b.quad(x0, y0, z0, bottom, x1, y0, z1, bottom, x1, y0 + height, z1, top, x0, y0 + height, z0, top);
		}
	}

	/** Okrągły, miękki blask zwrócony do kamery. */
	static void orb(GlowBatch b, double x, double y, double z,
					double rx, double ry, double rz, double ux, double uy, double uz,
					double radius, int rgb, float alpha) {
		int center = argb(rgb, alpha);
		int edge = argb(rgb, 0f);
		double[] px = new double[8];
		double[] py = new double[8];
		double[] pz = new double[8];
		for (int i = 0; i < 8; i++) {
			double g = Math.PI * 0.25 * i;
			double c = Math.cos(g) * radius;
			double s = Math.sin(g) * radius;
			px[i] = x + rx * c + ux * s;
			py[i] = y + ry * c + uy * s;
			pz[i] = z + rz * c + uz * s;
		}
		for (int k = 0; k < 4; k++) {
			int i0 = 2 * k;
			int i1 = 2 * k + 1;
			int i2 = (2 * k + 2) % 8;
			b.quad(x, y, z, center, px[i0], py[i0], pz[i0], edge, px[i1], py[i1], pz[i1], edge, px[i2], py[i2], pz[i2], edge);
		}
	}

	/** Czteroramienny błysk (gwiazdka) z miękkim środkiem, zwrócony do kamery. */
	static void sparkle(GlowBatch b, double x, double y, double z,
						double rx, double ry, double rz, double ux, double uy, double uz,
						double size, int rgb, float alpha) {
		int center = argb(rgb, alpha);
		int edge = argb(rgb, 0f);
		double w = size * 0.14;
		spike(b, x, y, z, rx, ry, rz, ux, uy, uz, size, w, center, edge);
		spike(b, x, y, z, ux, uy, uz, rx, ry, rz, size, w, center, edge);
		orb(b, x, y, z, rx, ry, rz, ux, uy, uz, size * 0.38, rgb, alpha * 0.85f);
	}

	private static void spike(GlowBatch b, double x, double y, double z,
							  double ax, double ay, double az, double bx, double by, double bz,
							  double length, double width, int center, int edge) {
		double lx = x - ax * length;
		double ly = y - ay * length;
		double lz = z - az * length;
		double rx = x + ax * length;
		double ry = y + ay * length;
		double rz = z + az * length;
		double tx = x + bx * width;
		double ty = y + by * width;
		double tz = z + bz * width;
		double dx = x - bx * width;
		double dy = y - by * width;
		double dz = z - bz * width;
		b.tri(x, y, z, center, lx, ly, lz, edge, tx, ty, tz, edge);
		b.tri(x, y, z, center, tx, ty, tz, edge, rx, ry, rz, edge);
		b.tri(x, y, z, center, rx, ry, rz, edge, dx, dy, dz, edge);
		b.tri(x, y, z, center, dx, dy, dz, edge, lx, ly, lz, edge);
	}

	/**
	 * Wstęga zwrócona do kamery wzdłuż podanej ścieżki.
	 * age01[i] to wiek punktu (0 = najnowszy, 1 = najstarszy), od niego zależy szerokość i przezroczystość.
	 */
	static void ribbon(GlowBatch b, double[] px, double[] py, double[] pz, float[] age01, int n,
					   double camX, double camY, double camZ, double halfWidth,
					   ColorPreset color, float alphaScale, float phaseOffset) {
		if (n < 2) {
			return;
		}
		double[] lx = new double[n];
		double[] ly = new double[n];
		double[] lz = new double[n];
		double[] rx = new double[n];
		double[] ry = new double[n];
		double[] rz = new double[n];
		int[] mid = new int[n];
		int[] edge = new int[n];

		for (int i = 0; i < n; i++) {
			int prev = Math.max(0, i - 1);
			int next = Math.min(n - 1, i + 1);
			double tx = px[next] - px[prev];
			double ty = py[next] - py[prev];
			double tz = pz[next] - pz[prev];
			double vx = px[i] - camX;
			double vy = py[i] - camY;
			double vz = pz[i] - camZ;
			// Kierunek boczny: prostopadły do ruchu i do kierunku patrzenia, więc wstęga zawsze jest płasko do kamery.
			double sx = ty * vz - tz * vy;
			double sy = tz * vx - tx * vz;
			double sz = tx * vy - ty * vx;
			double len = Math.sqrt(sx * sx + sy * sy + sz * sz);
			if (len < 1e-9) {
				sx = 0.0;
				sy = 1.0;
				sz = 0.0;
				len = 1.0;
			}
			sx /= len;
			sy /= len;
			sz /= len;

			float age = clamp01(age01[i]);
			double life = Math.pow(1.0 - age, 0.75);
			double w = halfWidth * life;
			lx[i] = px[i] - sx * w;
			ly[i] = py[i] - sy * w;
			lz[i] = pz[i] - sz * w;
			rx[i] = px[i] + sx * w;
			ry[i] = py[i] + sy * w;
			rz[i] = pz[i] + sz * w;

			int rgb = color.color(phaseOffset + age * 0.7f);
			float alpha = alphaScale * (float) Math.pow(1.0 - age, 1.5);
			mid[i] = argb(rgb, alpha);
			edge[i] = argb(rgb, 0f);
		}

		for (int i = 0; i < n - 1; i++) {
			int j = i + 1;
			b.quad(lx[i], ly[i], lz[i], edge[i],
				px[i], py[i], pz[i], mid[i],
				px[j], py[j], pz[j], mid[j],
				lx[j], ly[j], lz[j], edge[j]);
			b.quad(px[i], py[i], pz[i], mid[i],
				rx[i], ry[i], rz[i], edge[i],
				rx[j], ry[j], rz[j], edge[j],
				px[j], py[j], pz[j], mid[j]);
		}
	}
}
