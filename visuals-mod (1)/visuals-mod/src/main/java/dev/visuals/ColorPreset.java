package dev.visuals;

/** Gotowe kolory używane przez kapelusz, trail, kill effect i kółka. */
public enum ColorPreset {
	RED("Czerwony", 0xFF3030),
	ORANGE("Pomarańczowy", 0xFF8A1F),
	YELLOW("Żółty", 0xFFE030),
	GREEN("Zielony", 0x35E04A),
	CYAN("Turkusowy", 0x30E0E0),
	BLUE("Niebieski", 0x3070FF),
	PURPLE("Fioletowy", 0xA040FF),
	PINK("Różowy", 0xFF50B0),
	WHITE("Biały", 0xFFFFFF),
	RAINBOW("Tęcza", -1);

	public final String label;
	private final int rgb;

	ColorPreset(String label, int rgb) {
		this.label = label;
		this.rgb = rgb;
	}

	/**
	 * Zwraca kolor RGB (0xRRGGBB). Dla tęczy zależy od czasu, a {@code phase} (0..1)
	 * przesuwa odcień, żeby kolejne elementy miały różne kolory.
	 */
	public int color(float phase) {
		if (this != RAINBOW) {
			return rgb;
		}
		float hue = ((System.currentTimeMillis() % 4000L) / 4000f + phase) % 1f;
		return hsvToRgb(hue, 0.85f, 1f);
	}

	public ColorPreset next() {
		ColorPreset[] all = values();
		return all[(ordinal() + 1) % all.length];
	}

	public static int hsvToRgb(float h, float s, float v) {
		float hh = (h - (float) Math.floor(h)) * 6f;
		int sector = (int) hh;
		float f = hh - sector;
		float p = v * (1f - s);
		float q = v * (1f - s * f);
		float t = v * (1f - s * (1f - f));
		float r;
		float g;
		float b;
		switch (sector) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return ((int) (r * 255f) << 16) | ((int) (g * 255f) << 8) | (int) (b * 255f);
	}
}
