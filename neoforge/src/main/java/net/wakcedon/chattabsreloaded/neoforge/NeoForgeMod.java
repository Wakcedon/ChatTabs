package net.wakcedon.chattabsreloaded.neoforge;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.neoforged.fml.common.Mod;

@Mod("chattabs-reloaded")
public class NeoForgeMod {

    public NeoForgeMod() {
        // call into common init
        ChatTabs.init();
    }
}
