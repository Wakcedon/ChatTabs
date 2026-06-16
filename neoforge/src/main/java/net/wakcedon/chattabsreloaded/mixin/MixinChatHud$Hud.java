package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.mixininterface.IChatHudDrawer;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public class MixinChatHud$Hud implements IChatHudDrawer {

    @Override
    public Style chatTabs$getStyle() {
        return Style.EMPTY;
    }
    
    @Override
    public FormattedCharSequence chatTabs$getDrawer() {
        return FormattedCharSequence.EMPTY;
    }
}
