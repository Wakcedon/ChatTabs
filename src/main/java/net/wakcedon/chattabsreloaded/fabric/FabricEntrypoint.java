package net.wakcedon.chattabsreloaded.fabric;

import net.fabricmc.api.ModInitializer;
import net.wakcedon.chattabsreloaded.ChatTabs;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ChatTabs.init();
    }
}
