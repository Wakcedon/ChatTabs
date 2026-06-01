package net.grilledham.chattabs.fabric;

import net.fabricmc.api.ModInitializer;
import net.grilledham.chattabs.ChatTabs;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ChatTabs.init();
    }
}
