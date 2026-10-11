package dev.visuals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Efekty czasowe rysowane własną geometrią: kółka lądowania, fala uderzeniowa, słup światła, błysk. */
final class GlowFx {
	private static final List<Fx> FX = new ArrayList<>();

	private GlowFx() {
	}

	private enum Kind {
		LANDING, SHOCKWAVE, PILLAR, FLASH
	}

	private static final class Fx {
		final Kind kind;
		final double x;
		final double y;
		final double z;
		final ColorPreset color;
		final long born;
		final long life;
		final double radius;

		Fx(Kind kind, double x, double y, double z, ColorPreset color, long life, double radius) {
			this.kind = kind;
			this.x = x;
			this.y = y;
			this.z = z;
			this.color = color;
			this.born = System.currentTimeMillis();
			this.life = life;
			this.radius = radius;
		}
	}

	static void clear() {
		FX.clear();
	}

	static void landing(double x, double y, double z, ColorPreset color, double radius, long lifeMs) {
		FX.add(new Fx(Kind.LANDING, x, y, z, color, lifeMs, radius));
	}

	static void shockwave(double x, double y, double z, ColorPreset color) {
		FX.add(new Fx(Kind.SHOCKWAVE, x, y, z, color, 1000L, 3.6));
	}

	static void pillar(double x, double y, double z, ColorPreset color) {
		FX.add(new Fx(Kind.PILLAR, x, y, z, color, 1400L, 0.5));
	}

	static void flash(double x, double y, double z, ColorPreset color) {
		FX.add(new Fx(Kind.FLASH, x, y, z, color, 320L, 1.3));
	}

	static void build(GlowBatch b, long now, double rx, double ry, double rz, double ux, double uy, double uz) {
		Iterator<Fx> it = FX.iterator();
		while (it.hasNext()) {
			Fx fx = it.next();
			float t = (now - fx.born) / (float) fx.life;
			if (t >= 1f) {
				it.remove();
				continue;
			}
			if (t < 0f) {
				continue;
			}
			switch (fx.kind) {
				case LANDING -> landingShape(b, fx, t, now);
				case SHOCKWAVE -> shockwaveShape(b, fx, t);
				case PILLAR -> pillarShape(b, fx, t);
				case FLASH -> {
					int rgb = fx.color.color(0f);
					float a = (float) Math.pow(1.0 - t, 1.5);
					float size = (float) fx.radius * (0.4f + 1.1f * Shapes.easeOut(t));
					Shapes.orb(b, fx.x, fx.y, fx.z, rx, ry, rz, ux, uy, uz, size, rgb, a);
					Shapes.sparkle(b, fx.x, fx.y, fx.z, rx, ry, rz, ux, uy, uz, size * 1.7, 0xFFFFFF, a * 0.8f);
				}
			}
		}
	}

	private static int segmentsFor(double radius) {
		return Math.max(32, Math.min(96, 24 + (int) (radius * 22)));
	}

	/** Magiczny okrąg: rozchodzące się kółko, obracający się pierścień z kresek i miękki blask na środku. */
	private static void landingShape(GlowBatch b, Fx fx, float t, long now) {
		double y = fx.y + 0.03;
		double full = fx.radius;
		float fade = 1f - Shapes.smooth(t, 0.55f, 1f);

		// Rozchodzące się kółko, które zostaje na pełnym promieniu.
		double r1 = full * Shapes.easeOut(t / 0.4f);
		double hw = (0.07 + 0.05 * full) * (1.0 - 0.45 * t);
		Shapes.softRing(b, fx.x, y, fx.z, r1, hw, segmentsFor(r1), 0.0, Math.PI * 2.0, fx.color, 0.95f * fade, 0f);

		// Druga, cieńsza fala wychodząca chwilę później.
		float t2 = Shapes.clamp01((t - 0.12f) / 0.88f);
		double r2 = full * 0.7 * Shapes.easeOut(t2 / 0.5f);
		Shapes.softRing(b, fx.x, y, fx.z, r2, hw * 0.6, segmentsFor(r2), 0.0, Math.PI * 2.0, fx.color,
			0.7f * (1f - Shapes.smooth(t2, 0.5f, 1f)) * Shapes.smooth(t, 0.12f, 0.3f), 0.25f);

		// Obracające się kreski w środku.
		float dashIn = Shapes.smooth(t, 0.25f, 0.5f) * fade;
		if (dashIn > 0.01f) {
			double spin = now * 0.0012;
			int dashes = 12;
			double dashR = full * 0.82;
			for (int i = 0; i < dashes; i++) {
				double a0 = spin + Math.PI * 2.0 * i / dashes;
				double a1 = a0 + Math.PI * 2.0 / dashes * 0.55;
				Shapes.softRing(b, fx.x, y, fx.z, dashR, 0.05, 6, a0, a1, fx.color, 0.85f * dashIn, 0.5f + (float) i / dashes);
			}
		}

		// Miękki blask na środku, gaśnie szybciej niż reszta.
		float glow = (float) Math.pow(1.0 - t, 2.0);
		Shapes.disc(b, fx.x, y, fx.z, full * 0.6, 24, fx.color, 0.45f * glow, 0.1f);
	}

	private static void shockwaveShape(GlowBatch b, Fx fx, float t) {
		double y = fx.y + 0.06;
		double full = fx.radius;
		float fade = (float) Math.pow(1.0 - t, 1.2);

		double r1 = full * Shapes.easeOut(t);
		Shapes.softRing(b, fx.x, y, fx.z, r1, 0.16 + 0.3 * t, segmentsFor(r1), 0.0, Math.PI * 2.0, fx.color, 0.95f * fade, 0f);

		float t2 = Shapes.clamp01((t - 0.14f) / 0.86f);
		double r2 = full * 0.66 * Shapes.easeOut(t2);
		Shapes.softRing(b, fx.x, y + 0.02, fx.z, r2, 0.12 + 0.2 * t2, segmentsFor(r2), 0.0, Math.PI * 2.0, fx.color,
			0.8f * (float) Math.pow(1.0 - t2, 1.2) * Shapes.smooth(t, 0.14f, 0.25f), 0.4f);

		float flashT = Shapes.clamp01(t / 0.45f);
		Shapes.disc(b, fx.x, y, fx.z, 1.3 + full * 0.25 * flashT, 24, fx.color, 0.7f * (1f - flashT) * (1f - flashT), 0f);
	}

	private static void pillarShape(GlowBatch b, Fx fx, float t) {
		float rise = Shapes.easeOut(t / 0.22f);
		double height = 7.0 * rise;
		float fade = (float) Math.pow(1.0 - t, 1.1);
		double radius = fx.radius * (float) Math.pow(1.0 - t, 0.6) + 0.05;

		Shapes.column(b, fx.x, fx.y, fx.z, radius, height, 16, fx.color, 0.85f * fade, 0f, 0f);
		// Jasny, wąski rdzeń.
		Shapes.column(b, fx.x, fx.y, fx.z, radius * 0.45, height * 0.9, 12, ColorPreset.WHITE, 0.9f * fade, 0f, 0f);
		// Pierścień u podstawy.
		double ringR = 0.9 + 1.8 * Shapes.easeOut(t);
		Shapes.softRing(b, fx.x, fx.y + 0.04, fx.z, ringR, 0.16, 40, 0.0, Math.PI * 2.0, fx.color, 0.9f * fade, 0f);
		Shapes.disc(b, fx.x, fx.y + 0.04, fx.z, 1.1, 20, fx.color, 0.5f * fade, 0f);
	}
}
