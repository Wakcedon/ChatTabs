# ChatTabs

ChatTabs adds customizable tabs to the Minecraft chat UI.

Features
- Per-tab filters and send modifiers
- Server-profile-based tab sets
- Config GUI (Cloth/Cloth config integration)
- Works on Fabric and NeoForge (separate artifacts)

Building

Requirements: JDK 17+ (project targets Java 25 via toolchain), Gradle wrapper

To build both artifacts locally:

```bash
./gradlew :fabric:build :neoforge:build
```

Output
- `fabric/build/libs/` — Fabric remapped mod JAR
- `neoforge/build/libs/` — NeoForge mod JAR

Notes
- The project is organized as a root common library (shared code) and two
  subprojects `fabric` and `neoforge` that provide loader-specific metadata and
  entrypoints. The NeoForge build uses Forgified Fabric API to bridge Fabric
  API usage where possible.
