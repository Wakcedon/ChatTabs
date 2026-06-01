package net.wakcedon.chattabsreloaded.fabric;

import net.fabricmc.api.ModInitializer;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.FabricConfig;

public class FabricEntrypoint implements ModInitializer {
    private static FabricConfig config;
    
    @Override
    public void onInitialize() {
        ChatTabs.init();
        config = new FabricConfig();
        config.load();
        // Config is now loaded and accessible through getConfig()
    }
    
    public static FabricConfig getConfig() {
        return config;
    }
}