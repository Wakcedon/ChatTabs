package net.grilledham.chattabs.neoforge;

import net.grilledham.chattabs.ChatTabs;
import net.minecraftforge.fml.common.Mod;

@Mod("chattabs-reloaded")
public class NeoForgeMod {

    public NeoForgeMod() {
        // call into common init
        ChatTabs.init();
    }
}
