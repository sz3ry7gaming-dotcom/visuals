package dev.visuals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Własne świecące cząsteczki (błyski i kule), niezależne od systemu cząsteczek Minecrafta. */
final class GlowParticles {
	private static final int MAX = 600;
	private static final List<Particle> PARTICLES = new ArrayList<>();

	private GlowParticles() {
	}

	private enum Shape {
		SPARKLE, ORB
	}

	private static final class Particle {
		double x;
		double y;
		double z;
		double vx;
		double vy;
		double vz;
		double gravity;
		double drag;
		double swirl;
		float size;
		float sizeEnd;
		int rgb;
		float age;
		float life;
		float seed;
		Shape shape;
	}

	static void clear() {
		PARTICLES.clear();
	}

	private static void add(Particle p) {
		if (PARTICLES.size() >= MAX) {
			PARTICLES.remove(0);
		}
		PARTICLES.add(p);
	}

	/** Pojedynczy błysk, np. do śladu "pył gwiezdny". */
	static void sparkle(double x, double y, double z, double vx, double vy, double vz,
						double gravity, float size, float life, int rgb) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		Particle p = new Particle();
		p.x = x;
		p.y = y;
		p.z = z;
		p.vx = vx;
		p.vy = vy;
		p.vz = vz;
		p.gravity = gravity;
		p.drag = 1.2;
		p.size = size;
		p.sizeEnd = size * 0.2f;
		p.rgb = rgb;
		p.life = life;
		p.seed = r.nextFloat() * 6.28f;
		p.shape = Shape.SPARKLE;
		add(p);
	}

	/** Rozbryzg błysków we wszystkie strony. */
	static void burst(double x, double y, double z, ColorPreset color, int count, double speed, double gravity) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < count; i++) {
			double theta = r.nextDouble() * Math.PI * 2.0;
			double cosPhi = r.nextDouble() * 2.0 - 1.0;
			double sinPhi = Math.sqrt(1.0 - cosPhi * cosPhi);
			double s = speed * (0.35 + r.nextDouble() * 0.65);
			Particle p = new Particle();
			p.x = x;
			p.y = y;
			p.z = z;
			p.vx = Math.cos(theta) * sinPhi * s;
			p.vy = cosPhi * s * 0.8 + speed * 0.15;
			p.vz = Math.sin(theta) * sinPhi * s;
			p.gravity = gravity;
			p.drag = 1.6;
			p.size = 0.16f + r.nextFloat() * 0.2f;
			p.sizeEnd = 0.03f;
			p.rgb = color.color(r.nextFloat());
			p.life = 0.7f + r.nextFloat() * 0.7f;
			p.seed = r.nextFloat() * 6.28f;
			p.shape = r.nextInt(4) == 0 ? Shape.ORB : Shape.SPARKLE;
			add(p);
		}
	}

	/** Kule unoszące się spiralnie ku górze. */
	static void spirits(double x, double y, double z, ColorPreset color, int count) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < count; i++) {
			double theta = r.nextDouble() * Math.PI * 2.0;
			double out = 0.5 + r.nextDouble() * 0.9;
			Particle p = new Particle();
			p.x = x;
			p.y = y + r.nextDouble() * 0.4;
			p.z = z;
			p.vx = Math.cos(theta) * out;
			p.vy = 1.4 + r.nextDouble() * 1.8;
			p.vz = Math.sin(theta) * out;
			p.gravity = -0.4;
			p.drag = 1.1;
			p.swirl = 3.0 + r.nextDouble() * 2.5;
			p.size = 0.22f + r.nextFloat() * 0.18f;
			p.sizeEnd = 0.02f;
			p.rgb = color.color(r.nextFloat());
			p.life = 1.1f + r.nextFloat() * 0.9f;
			p.seed = r.nextFloat() * 6.28f;
			p.shape = Shape.ORB;
			add(p);
		}
	}

	static void update(float dt) {
		if (dt <= 0f) {
			return;
		}
		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.age += dt;
			if (p.age >= p.life) {
				it.remove();
				continue;
			}
			if (p.swirl != 0.0) {
				double a = p.swirl * dt;
				double c = Math.cos(a);
				double s = Math.sin(a);
				double nvx = p.vx * c - p.vz * s;
				double nvz = p.vx * s + p.vz * c;
				p.vx = nvx;
				p.vz = nvz;
			}
			p.vy -= p.gravity * dt;
			double damp = Math.max(0.0, 1.0 - p.drag * dt);
			p.vx *= damp;
			p.vy *= damp;
			p.vz *= damp;
			p.x += p.vx * dt;
			p.y += p.vy * dt;
			p.z += p.vz * dt;
		}
	}

	static void build(GlowBatch b, double rx, double ry, double rz, double ux, double uy, double uz) {
		for (Particle p : PARTICLES) {
			float t = p.age / p.life;
			float fadeIn = Math.min(1f, p.age / 0.08f);
			float alpha = fadeIn * (float) Math.pow(1.0 - t, 1.3);
			float size = p.size + (p.sizeEnd - p.size) * t;
			if (p.shape == Shape.SPARKLE) {
				size *= 0.8f + 0.2f * (float) Math.sin(p.age * 28f + p.seed);
				Shapes.sparkle(b, p.x, p.y, p.z, rx, ry, rz, ux, uy, uz, size, p.rgb, alpha);
			} else {
				Shapes.orb(b, p.x, p.y, p.z, rx, ry, rz, ux, uy, uz, size, p.rgb, alpha * 0.9f);
			}
		}
	}
}
