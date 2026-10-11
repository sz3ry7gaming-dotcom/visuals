# Visuals (Fabric, Minecraft 1.21.11)

Mod kliencki z wizualnymi dodatkami. Menu otwierasz klawiszem **O** (zmiana w Opcje → Sterowanie → Visuals).

## Funkcje

| Funkcja | Opis |
|---|---|
| **Chinese hat** | Stożkowy kapelusz na głowie, rysowany z kodu (bez tekstur). Kolor (w tym tęcza), rozmiar, opcjonalnie widoczny u innych graczy. Własny widać w trzeciej osobie (F5). |
| **Trail** | Ślad za graczem: kolorowy pył, płomienie, dusze, End Rod, serduszka, nutki, iskry, chmurki, magia. Gęstość i tryb "tylko w ruchu". |
| **Kill effect** | Po zabiciu kogoś, w kogo uderzyłeś: piorun, totem, płomienie, dusze, serduszka, krew, spirala, fajerwerki. Opcja "tylko gracze". |
| **Kółka lądowania** | Rozszerzające się kółko w miejscu lądowania. Kolor, promień, czas i minimalna wysokość upadku. |

W menu każda zakładka ma przycisk testowy (Kill effect i Lądowanie), który zamyka menu i odpala efekt kilka kroków przed Tobą.
Ustawienia zapisują się w `config/visuals.json`.

## Budowanie

Wymagania: **JDK 21** i internet (Gradle pobiera Fabric Loom, mapowania i Minecraft).

```
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

Gotowy plik: `build/libs/visuals-1.0.0.jar`. Wrzuć go do folderu `mods`.

Do uruchomienia potrzebujesz Minecraft **1.21.11**, Fabric Loader **0.19.5+** i **Fabric API** dla 1.21.11.

Testowo w IDE: `./gradlew runClient`.

## Struktura

- `VisualsClient` – punkt wejścia, klawisz O, eventy
- `ConfigScreen` – menu z zakładkami
- `HatFeatureRenderer` – kapelusz (warstwa renderera gracza)
- `TrailEffect`, `LandingEffect`, `KillEffect` – logika efektów
- `VisualsConfig`, `ColorPreset`, `TrailType`, `KillEffectType` – ustawienia
