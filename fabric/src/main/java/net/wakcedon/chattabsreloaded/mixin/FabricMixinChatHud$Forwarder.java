package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.mixininterface.IChatHudDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHud.class)
public class FabricMixinChatHud$Forwarder implements IChatHudDrawer {

    @Override
    public Style chatTabs$getStyle() {
        return Style.EMPTY;
    }
    
    @Override
    public FormattedCharSequence chatTabs$getDrawer() {
        return FormattedCharSequence.EMPTY;
    }
}
