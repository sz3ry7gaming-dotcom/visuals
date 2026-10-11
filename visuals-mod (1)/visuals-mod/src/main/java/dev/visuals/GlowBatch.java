package dev.visuals;

import java.util.Arrays;

/**
 * Bufor wierzchołków na jedną klatkę. Wszystkie efekty (wstęga, kółka, błyski, cząsteczki)
 * dopisują tu czworokąty, a potem całość trafia do jednej warstwy renderowania.
 * Współrzędne są zapisywane względem kamery, tak jak wymaga tego renderer świata.
 */
final class GlowBatch {
	private float[] pos = new float[3 * 4096];
	private int[] col = new int[4096];
	private int count;
	private double ox;
	private double oy;
	private double oz;
	private float intensity = 1f;

	void begin(double cameraX, double cameraY, double cameraZ, float intensity) {
		this.count = 0;
		this.ox = cameraX;
		this.oy = cameraY;
		this.oz = cameraZ;
		this.intensity = intensity;
	}

	int count() {
		return count;
	}

	float[] positions() {
		return Arrays.copyOf(pos, count * 3);
	}

	int[] colors() {
		return Arrays.copyOf(col, count);
	}

	void vertex(double x, double y, double z, int argb) {
		if (count == col.length) {
			col = Arrays.copyOf(col, col.length * 2);
			pos = Arrays.copyOf(pos, pos.length * 2);
		}
		int i = count * 3;
		pos[i] = (float) (x - ox);
		pos[i + 1] = (float) (y - oy);
		pos[i + 2] = (float) (z - oz);
		int alpha = Math.min(255, Math.round((argb >>> 24) * intensity));
		col[count++] = (alpha << 24) | (argb & 0xFFFFFF);
	}

	/** Czworokąt rysowany z obu stron, więc widać go pod każdym kątem niezależnie od ustawienia culling. */
	void quad(double x1, double y1, double z1, int c1,
			  double x2, double y2, double z2, int c2,
			  double x3, double y3, double z3, int c3,
			  double x4, double y4, double z4, int c4) {
		vertex(x1, y1, z1, c1);
		vertex(x2, y2, z2, c2);
		vertex(x3, y3, z3, c3);
		vertex(x4, y4, z4, c4);
		vertex(x4, y4, z4, c4);
		vertex(x3, y3, z3, c3);
		vertex(x2, y2, z2, c2);
		vertex(x1, y1, z1, c1);
	}

	/** Trójkąt zapisany jako zdegenerowany czworokąt. */
	void tri(double x1, double y1, double z1, int c1,
			 double x2, double y2, double z2, int c2,
			 double x3, double y3, double z3, int c3) {
		quad(x1, y1, z1, c1, x2, y2, z2, c2, x3, y3, z3, c3, x3, y3, z3, c3);
	}
}
