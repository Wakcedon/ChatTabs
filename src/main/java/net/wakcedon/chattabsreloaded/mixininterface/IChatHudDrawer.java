package net.wakcedon.chattabsreloaded.mixininterface;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public interface IChatHudDrawer {
    
    Style chatTabs$getStyle();
    FormattedCharSequence chatTabs$getDrawer();
}
