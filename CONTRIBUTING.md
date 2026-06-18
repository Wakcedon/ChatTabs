# Contributing to ChatTabs Reloaded

Thank you for your interest in contributing! This document explains the repository layout and how to work with the multi-loader build.

Repository structure

- `src/main/java` — Shared, loader-agnostic code: configuration, models, and non-UI logic.
- `neoforge/` — NeoForge-specific metadata and ModDevGradle configuration. Keeps runtime dependencies minimal.

Building and running

- Use the Gradle wrapper and JDK 21+.
- Build the artifact:

```bash
./gradlew :neoforge:build --no-daemon
```

Contributing workflow

1. Fork the repository and make a feature branch.
2. Run the build locally and ensure it compiles.
3. Open a pull request describing your changes.

Code guidelines

- Keep loader-specific UI and metadata in the `neoforge/` subproject.
- Shared logic belongs in `src/main/java` and must avoid direct runtime dependencies on loader-specific APIs when possible.
- Tests and tooling should be added under `gradle/` or as separate tasks.

If you need help setting up the environment or CI, open an issue and tag @Wakcedon.
