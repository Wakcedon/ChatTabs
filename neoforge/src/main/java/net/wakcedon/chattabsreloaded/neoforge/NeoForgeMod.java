package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.NeoForgeConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod("chattabs_reloaded")
public class NeoForgeMod {

    private static NeoForgeConfig config;

    public NeoForgeMod() {
        ChatTabs.init();
        config = new NeoForgeConfig(FMLPaths.CONFIGDIR.get().resolve("chattabs.json").toString());
        config.load();
    }

    public static NeoForgeConfig getConfig() {
        return config;
    }
}
