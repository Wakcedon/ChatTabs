package net.wakcedon.chattabsreloaded.client;

import net.fabricmc.api.ClientModInitializer;
import net.wakcedon.chattabsreloaded.ChatTabs;

public class ChatTabsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        try {
            Class<?> cfg = Class.forName("net.wakcedon.chattabsreloaded.config.ChatTabsConfig");
            cfg.getMethod("load").invoke(null);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { cfg.getMethod("save").invoke(null); } catch (Throwable ignored) {}
            }));
        } catch (Throwable ignored) {}
        ChatTabs.init();
    }
}
