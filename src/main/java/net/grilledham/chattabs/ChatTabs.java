package net.grilledham.chattabs;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class ChatTabs {

    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Loader-agnostic initialization entry for shared setup.
     * Fabric and NeoForge entrypoints should call this.
     */
    public static void init() {
        LOGGER.info("ChatTabs common init");
    }
}
