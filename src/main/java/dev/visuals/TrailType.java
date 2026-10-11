package dev.visuals;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/** Rodzaje śladu za graczem. */
public enum TrailType {
	DUST("Kolorowy pył"),
	FLAME("Płomienie"),
	SOUL("Dusze"),
	END_ROD("End Rod"),
	HEARTS("Serduszka"),
	NOTES("Nutki"),
	SPARKS("Iskry"),
	CLOUD("Chmurki"),
	MAGIC("Magia");

	public final String label;

	TrailType(String label) {
		this.label = label;
	}

	/** Tylko {@link #DUST} używa wybranego koloru, pozostałe mają własne kolory. */
	public ParticleEffect create(int rgb) {
		return switch (this) {
			case DUST -> new DustParticleEffect(rgb, 1.0f);
			case FLAME -> ParticleTypes.FLAME;
			case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
			case END_ROD -> ParticleTypes.END_ROD;
			case HEARTS -> ParticleTypes.HEART;
			case NOTES -> ParticleTypes.NOTE;
			case SPARKS -> ParticleTypes.ELECTRIC_SPARK;
			case CLOUD -> ParticleTypes.CLOUD;
			case MAGIC -> ParticleTypes.WITCH;
		};
	}

	public TrailType next() {
		TrailType[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
