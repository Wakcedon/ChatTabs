package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.wakcedon.chattabsreloaded.render.ChatHudOverlays;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.commands.ChatTabsCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    @Shadow
    private CommandSuggestions commandSuggestions;

    @Shadow
    private net.minecraft.client.gui.components.EditBox input;

    @Unique
    private int chattabs$tabScroll = -1;
    @Unique
    private int chattabs$hoveredTab = -1;

    @Unique
    private int chattabs$savedInputY;

    protected MixinChatScreen(Component title) {
        super(title);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"))
    private void chattabs$onRenderHead(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        IChatHud chatHud = (IChatHud)this.minecraft.gui.getChat();
        float slideAnim = chatHud.chatTabs$getChatSlideAnim();
        if(slideAnim < 1.0f) {
            chattabs$savedInputY = input.getY();
            int slideOffset = (int)((1.0f - slideAnim) * 30);
            input.setY(input.getY() + slideOffset);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void renderChatContextMenu(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        ((IChatHud)this.minecraft.gui.getChat()).chatTabs$renderContextMenu(context, width, height, mouseX, mouseY, deltaTicks);
        chattabs$renderTabs(context, mouseX, mouseY);
        if(((IChatHud)this.minecraft.gui.getChat()).chatTabs$getChatSlideAnim() < 1.0f) {
            input.setY(chattabs$savedInputY);
        }
    }

    @Unique
    private void chattabs$renderTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        if(client.screen instanceof EditChatScreen) return;

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        IChatHud chatHud = (IChatHud)client.gui.getChat();
        float alpha = config.tabAnimationFade ? chatHud.chatTabs$getAnimAlpha() : 1.0f;

        int windowHeight = client.getWindow().getGuiScaledHeight();
        float chatScale = client.options.chatScale().get().floatValue();
        int baseYOffset = height - input.getY();

        List<Integer> tabMidpoints = new ArrayList<>();
        int[] result = ChatHudOverlays.renderChatTabs(
            client, chattabs$tabScroll, guiGraphics, windowHeight, chatScale,
            true, config.chatWidth, mouseX, mouseY, 0, false, baseYOffset, tabMidpoints, alpha
        );
        chattabs$hoveredTab = result[0];
        chattabs$tabScroll = result[1];
        if(config.tabDragAndDrop && chatHud.chatTabs$isDragging()) {
            if(!client.mouseHandler.isLeftPressed()) {
                chatHud.chatTabs$endDrag(mouseX);
                if(config.showUnreadCounter) {
                    // re-read config after potential reorder (selectedTab may have changed)
                }
                chatHud.chatTabs$setHoverState(chattabs$hoveredTab, chattabs$tabScroll);
                return;
            }

            int dropIdx = tabMidpoints.size();
            for(int i = 0; i < tabMidpoints.size(); i++) {
                if(mouseX < tabMidpoints.get(i)) {
                    dropIdx = i;
                    break;
                }
            }

            int tabY = Mth.floor((windowHeight - baseYOffset) / chatScale) - 17;

            int indicatorX = dropIdx < tabMidpoints.size()
                ? tabMidpoints.get(dropIdx)
                : (tabMidpoints.isEmpty() ? 4 : tabMidpoints.get(tabMidpoints.size() - 1) + 20);

            ChatHudOverlays.renderDropIndicator(guiGraphics, indicatorX - 1, tabY, 13);

            List<ChatTab> allTabs = config.getChatTabs();
            int dragIdx = chatHud.chatTabs$getDragTabIndex();
            if(dragIdx >= 0 && dragIdx < allTabs.size()) {
                ChatHudOverlays.renderFloatingTab(guiGraphics, client, allTabs.get(dragIdx).getDisplayComponent().getString(), mouseX, mouseY);
            }

            chatHud.chatTabs$setHoverState(chattabs$hoveredTab, chattabs$tabScroll);
            return;
        }

        chatHud.chatTabs$setHoverState(chattabs$hoveredTab, chattabs$tabScroll);

        float a = config.tabAnimationFade ? chatHud.chatTabs$getAnimAlpha() : 1.0f;
        for(net.wakcedon.chattabsreloaded.render.GhostTab gt : chatHud.chatTabs$getRemovingTabs()) {
            String name = "x " + gt.name;
            int tw = client.font.width(name);
            int w = tw + 8;
            float ga = a * gt.alpha;
            if(ga > 0.01f) {
                int gx = 4;
                int gy = Mth.floor((windowHeight - baseYOffset) / chatScale) - 17;
                ChatHudOverlays.fillRoundedRect(guiGraphics, gx, gy, w, 13, 0x44FF4444, ga);
                guiGraphics.drawString(client.font, name, gx + 3, gy + 2, 0xFFFFFF | (Math.round(255 * ga) << 24));
            }
        }
    }

    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"))
    private void modifyChatMessage(ClientPacketListener instance, String content) {
        if(ChatTabsCommands.handleCommand(content)) return;

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(config.enabled) {
            ChatTab selectedTab = config.getSelectedChatTab();
            if(selectedTab != null) {
                content = selectedTab.modifySend(content);
            }
            if(content.startsWith("/")) {
                instance.sendCommand(content.substring(1));
            } else {
                instance.sendChat(content);
            }
        } else {
            instance.sendChat(content);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(((IChatHud)this.minecraft.gui.getChat()).chatTabs$mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }


}
