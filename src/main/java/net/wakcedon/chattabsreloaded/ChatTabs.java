package net.wakcedon.chattabsreloaded;

import java.util.logging.Logger;

public class ChatTabs {

    public static final Logger LOGGER = Logger.getLogger("ChatTabs");

    /**
     * Loader-agnostic initialization entry for shared setup.
     * Fabric and NeoForge entrypoints should call this.
     */
    public static void init() {
        LOGGER.info("ChatTabs Reloaded common init");
    }
}
