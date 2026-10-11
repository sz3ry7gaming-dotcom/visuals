# Visuals (Fabric, Minecraft 1.21.11)

Mod kliencki z wizualnymi dodatkami. Menu otwierasz klawiszem **O** (zmiana w Opcje → Sterowanie → Visuals).

Główne efekty nie używają cząsteczek Minecrafta. Są rysowane własną geometrią (świecące, półprzezroczyste kształty),
dlatego wyglądają inaczej niż zwykłe particle z gry.

## Funkcje

| Funkcja | Opis |
|---|---|
| **Chinese hat** | Stożkowy kapelusz: plecionka z pasków, wklęsły profil, grubsza krawędź, ciemna podszewka, złota końcówka i troczki. Kolor (też tęcza), rozmiar, opcjonalne świecenie i widoczność u innych graczy. Własny widać w trzeciej osobie (F5). |
| **Trail** | **Wstęga** (miękka, świecąca, zwęża się i gaśnie), **Helisa** (dwie skręcone wstęgi), **Pył gwiezdny** (świecące błyski). Dodatkowo klasyczne cząsteczki: pył, płomienie, dusze, End Rod, serduszka, nutki, iskry, chmurki, magia. Regulacja długości, szerokości i wysokości. |
| **Kill effect** | **Fala uderzeniowa**, **Słup światła**, **Odłamki**, **Duchy** (własny blask), a także piorun, totem, płomienie, dusze, serduszka, krew, spirala i fajerwerki. |
| **Kółka lądowania** | Magiczny okrąg w miejscu lądowania: rozchodzące się kółka, obracające się kreski i miękki blask na środku. Kolor, promień, czas i minimalna wysokość upadku. |
| **Ogólne** | Jasność wszystkich efektów blaskiem oraz reset ustawień. |

Kill effect i Lądowanie mają w menu przycisk testowy, który zamyka menu i odpala efekt kilka kroków przed Tobą.
Ustawienia zapisują się w `config/visuals.json`.

## Budowanie

Wymagania: **JDK 21** i internet (Gradle pobiera Fabric Loom, mapowania i Minecraft).

```
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

Gotowy plik: `build/libs/visuals-1.0.0.jar`. Wrzuć go do folderu `mods`.
Alternatywnie wgraj projekt na GitHub: workflow w `.github/workflows/build.yml` zbuduje jar automatycznie.

Do uruchomienia potrzebujesz Minecraft **1.21.11**, Fabric Loader **0.19.5+** i **Fabric API** dla 1.21.11.

## Struktura

- `VisualsClient` – punkt wejścia, klawisz O, eventy
- `VisualsRenderer` – rysowanie efektów w świecie co klatkę (jedna świecąca warstwa)
- `GlowBatch`, `Shapes` – bufor wierzchołków i kształty (pierścień, dysk, kolumna, błysk, kula, wstęga)
- `GlowTrail`, `GlowFx`, `GlowParticles` – wstęga/helisa, efekty czasowe, własne cząsteczki
- `HatFeatureRenderer` – kapelusz (warstwa renderera gracza)
- `TrailEffect`, `LandingEffect`, `KillEffect` – logika efektów
- `ConfigScreen` – menu z zakładkami
- `VisualsConfig`, `ColorPreset`, `TrailType`, `KillEffectType` – ustawienia
