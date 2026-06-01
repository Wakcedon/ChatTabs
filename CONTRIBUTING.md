# Contributing to ChatTabs Reloaded

Thank you for your interest in contributing! This document explains the repository layout and how to work with the multi-loader build.

Repository structure

- `src/main/java` — Shared, loader-agnostic code: configuration, models, and non-UI logic.
- `fabric/` — Fabric-specific entrypoints, Cloth-config UI and `fabric.mod.json`. Built with Fabric Loom.
- `neoforge/` — NeoForge-specific metadata and ModDevGradle configuration. Keeps runtime dependencies minimal.

Building and running

- Use the Gradle wrapper and JDK 21+.
- Build both artifacts:

```bash
./gradlew -Pci=true :fabric:build :neoforge:build --no-daemon --parallel
```

Contributing workflow

1. Fork the repository and make a feature branch.
2. Run the build locally and ensure both artifacts compile.
3. Open a pull request describing your changes.

Code guidelines

- Keep loader-specific UI and metadata in the `fabric/` and `neoforge/` subprojects.
- Shared logic belongs in `src/main/java` and must avoid direct runtime dependencies on Fabric-only APIs when possible.
- Tests and tooling should be added under `gradle/` or as separate tasks.

If you need help setting up the environment or CI, open an issue and tag @Wakcedon.
