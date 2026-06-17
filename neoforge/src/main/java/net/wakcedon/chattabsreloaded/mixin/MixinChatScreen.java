package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.render.ChatHudOverlays;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Unique
    private int chattabs$tabScroll = -1;
    @Unique
    private int chattabs$hoveredTab = -1;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void chattabs$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if(client.screen instanceof EditChatScreen) return;

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        int windowHeight = client.getWindow().getGuiScaledHeight();
        float chatScale = client.options.chatScale().get().floatValue();

        int[] result = ChatHudOverlays.renderChatTabs(
            client, chattabs$tabScroll, guiGraphics, windowHeight, chatScale,
            true, config.chatWidth, mouseX, mouseY, 0, false
        );
        chattabs$hoveredTab = result[0];
        chattabs$tabScroll = result[1];
    }
}
