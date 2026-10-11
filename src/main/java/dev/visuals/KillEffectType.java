package dev.visuals;

/** Rodzaje efektu po zabiciu. */
public enum KillEffectType {
	LIGHTNING("Piorun"),
	TOTEM("Totem"),
	FLAMES("Płomienie"),
	SOULS("Dusze"),
	HEARTS("Serduszka"),
	BLOOD("Krew"),
	SPIRAL("Spirala"),
	FIREWORK("Fajerwerki");

	public final String label;

	KillEffectType(String label) {
		this.label = label;
	}

	public KillEffectType next() {
		KillEffectType[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
