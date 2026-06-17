package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.SubscribeEvent;

@Mod("chattabs_reloaded")
public class NeoForgeMod {

    private static NeoForgeChatTabsConfig config;

    public NeoForgeMod(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ChatTabsModConfig.CLIENT_SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(container, parent));

        ChatTabs.init();
        config = new NeoForgeChatTabsConfig(FMLPaths.CONFIGDIR.get().resolve("chattabs.json"));
        config.load();
        config.syncFromSpec();
    }

    @Mod.EventBusSubscriber(modid = "chattabs_reloaded", bus = Mod.EventBusSubscriber.Bus.MOD)
    static class ConfigEventHandler {
        @SubscribeEvent
        static void onLoad(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() == ChatTabsModConfig.CLIENT_SPEC && config != null) {
                config.syncFromSpec();
            }
        }

        @SubscribeEvent
        static void onReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() == ChatTabsModConfig.CLIENT_SPEC && config != null) {
                config.syncFromSpec();
            }
        }
    }

    public static NeoForgeChatTabsConfig getConfig() {
        return config;
    }
}
