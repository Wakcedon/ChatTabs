package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod("chattabs_reloaded")
public class NeoForgeMod {

    private static NeoForgeChatTabsConfig config;

    public NeoForgeMod(IEventBus modEventBus) {
        ModContainer container = ModList.get().getModContainerById("chattabs_reloaded").orElseThrow();

        container.registerConfig(ModConfig.Type.CLIENT, ChatTabsModConfig.CLIENT_SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new ConfigurationScreen(container, parent));

        modEventBus.addListener((ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == ChatTabsModConfig.CLIENT_SPEC && config != null) {
                config.syncFromSpec();
            }
        });
        modEventBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == ChatTabsModConfig.CLIENT_SPEC && config != null) {
                config.syncFromSpec();
            }
        });

        ChatTabs.init();
        config = new NeoForgeChatTabsConfig(FMLPaths.CONFIGDIR.get().resolve("chattabs.json"));
        config.load();
        config.syncFromSpec();
    }

    public static NeoForgeChatTabsConfig getConfig() {
        return config;
    }
}
