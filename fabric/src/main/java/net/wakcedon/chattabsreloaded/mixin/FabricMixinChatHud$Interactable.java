package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.mixininterface.IChatHudDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatHud.class)
public class FabricMixinChatHud$Interactable implements IChatHudDrawer {

    @Shadow
    private @Nullable Style hoveredStyle;
    
    @Override
    public Style chatTabs$getStyle() {
        return hoveredStyle;
    }
    
    @Override
    public FormattedCharSequence chatTabs$getDrawer() {
        return FormattedCharSequence.EMPTY;
    }
}
