package net.wakcedon.chattabsreloaded;

/**
 * Minimal stub of the common {@code ChatTabs} class used only by the NeoForge entrypoint.
 * The full implementation lives in the main source set, but NeoForge does not need the
 * client‑only code. Providing this lightweight class allows the NeoForge module to compile
 * without pulling in the entire client codebase.
 */
public class ChatTabs {
    public static void init() {
        // No‑op for NeoForge – the real initialization is performed in the common module
        // when the mod is loaded on the client.
    }
}
