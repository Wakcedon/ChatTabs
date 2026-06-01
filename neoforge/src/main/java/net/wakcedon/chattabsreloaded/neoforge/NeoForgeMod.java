package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.NeoForgeConfig;
import net.neoforged.fml.common.Mod;

@Mod("chattabs-reloaded")
public class NeoForgeMod {

    private static NeoForgeConfig config;

    public NeoForgeMod() {
        // call into common init
        ChatTabs.init();
        config = new NeoForgeConfig();
        config.load();
        // Set the max lines from config
        ChatTabs.setMaxLines(config.getConfig().maxLines);
    }
    
    public static NeoForgeConfig getConfig() {
        return config;
    }
}