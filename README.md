# ChatTabs Reloaded

ChatTabs Reloaded is a fork and modern continuation of the original ChatTabs mod, maintained by Wakcedon with contributions from Gri11edHam.

What it does
- Adds configurable chat tabs to the Minecraft chat UI.
- Per-tab message filters, color rules, and send modifiers (prefix/suffix).
- Server profile support: different tab sets per server.
- Config GUI (Fabric only, via Cloth Config) — NeoForge build does not require Cloth at runtime.

Supported loaders and versions
- Fabric: 1.21.x (Fabric Loader 0.18.x)
- NeoForge: 1.21.x (NeoForge 21.x)

Supported languages
- English (default)
- Russian (included)

Quick links
- Russian README: [README.ru.md](README.ru.md)
- Contributing guide: [CONTRIBUTING.md](CONTRIBUTING.md)

How the project is organized
- `src/main/java` — shared, loader-agnostic code and data models.
- `fabric/` — Fabric entrypoints, Cloth-config UI, mod metadata and Loom build.
- `neoforge/` — NeoForge wrapper metadata and ModDevGradle build.

Building locally
Requirements: JDK 21 (recommended), Gradle wrapper

To build both artifacts locally:

```bash
./gradlew -Pci=true :fabric:build :neoforge:build --no-daemon --parallel
```

Outputs
- `fabric/build/libs/` — Fabric remapped mod JAR
- `neoforge/build/libs/` — NeoForge mod JAR

Translations
- Russian locale is included at `fabric/src/main/resources/assets/chattabs-reloaded/lang/ru_ru.json`.

If you'd like to help translate or contribute, see CONTRIBUTING.md.
