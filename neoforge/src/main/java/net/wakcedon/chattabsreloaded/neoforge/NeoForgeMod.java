package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.fabricmc.fabric.api.mod.container.v1.ModContainer;

@Mod("chattabs-reloaded")
public class NeoForgeMod {

    public NeoForgeMod() {
        // call into common init
        ChatTabs.init();
    }
}
