package net.wakcedon.chattabsreloaded.config;

// Fabric UI uses Cloth Config and ModMenu, but we avoid hard dependencies by
// building this file against a reflection-based fallback. The fabric subproject
// can add cloth-config at development time if available.

public final class FabricConfigScreens {

    private FabricConfigScreens() {}

    public static Object create() {
        // Caller may reflectively interact with Cloth Config if present.
        return null;
    }
}
