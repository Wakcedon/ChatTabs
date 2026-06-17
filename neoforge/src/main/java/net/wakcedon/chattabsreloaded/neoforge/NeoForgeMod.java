package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = "chattabs_reloaded", mixinConfig = "chattabs.mixins.json")
public class NeoForgeMod {

    private static NeoForgeChatTabsConfig config;

    public NeoForgeMod() {
        ChatTabs.init();
        config = new NeoForgeChatTabsConfig(FMLPaths.CONFIGDIR.get().resolve("chattabs.json"));
        config.load();
    }

    public static NeoForgeChatTabsConfig getConfig() {
        return config;
    }
}
