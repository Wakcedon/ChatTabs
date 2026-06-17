package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.wakcedon.chattabsreloaded.render.ChatContextMenu;
import net.wakcedon.chattabsreloaded.render.ChatHudOverlays;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatHud implements IChatHud {

    @Unique
    private int chattabs$tabScroll = -1;
    @Unique
    private int chattabs$hoveredTab = -1;
    @Unique
    private ChatContextMenu chattabs$contextMenu;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("TAIL"))
    private void chattabs$onRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if(client.screen instanceof EditChatScreen) return;

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        int windowHeight = client.getWindow().getGuiScaledHeight();
        float chatScale = client.options.chatScale().get().floatValue();
        boolean mcTabUnreads = false;

        int[] result = ChatHudOverlays.renderChatTabs(
            client, chattabs$tabScroll, guiGraphics, windowHeight, chatScale,
            focused, config.chatWidth, mouseX, mouseY, 0, mcTabUnreads
        );
        chattabs$hoveredTab = result[0];
        chattabs$tabScroll = result[1];
    }

    @Override
    public boolean chatTabs$mouseClicked(double mouseX, double mouseY, int button) {
        Minecraft client = Minecraft.getInstance();
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return false;

        if(chattabs$contextMenu != null) {
            if(chattabs$contextMenu.click(mouseX, mouseY, button)) {
                chattabs$contextMenu = null;
                return true;
            }
            chattabs$contextMenu = null;
            return true;
        }

        if(chattabs$hoveredTab == -2) {
            chattabs$tabScroll = Math.max(0, chattabs$tabScroll - 1);
            return true;
        }
        if(chattabs$hoveredTab == -3) {
            chattabs$tabScroll++;
            return true;
        }
        if(chattabs$hoveredTab == 0) {
            config.selectedTab = 0;
            return true;
        }
        if(chattabs$hoveredTab > 0) {
            if(button == 1) {
                chattabs$showTabContextMenu(client, (int)mouseX, (int)mouseY);
            } else {
                config.selectedTab = chattabs$hoveredTab;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean chatTabs$mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean chatTabs$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public void chatTabs$renderContextMenu(GuiGraphics context, int windowWidth, int windowHeight, int mouseX, int mouseY, float deltaTicks) {
        if(chattabs$contextMenu != null) {
            chattabs$contextMenu.render(Minecraft.getInstance(), context, windowWidth, windowHeight, mouseX, mouseY);
        }
    }

    @Override
    public void chatTabs$renderDummy(GuiGraphics context, Font textRenderer, int ticks, int mouseX, int mouseY, boolean editFocused) {
        Minecraft client = Minecraft.getInstance();
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        int chatWidth = config.chatWidth;
        int chatHeight = editFocused ? config.chatHeightFocused : config.chatHeightUnfocused;
        int visualHeight = (int)(chatHeight * client.options.chatScale().get());

        context.fill(0, -visualHeight, chatWidth, 0, config.bgColor.getRGB());
        for(int i = 0; i < 5; i++) {
            int y = -12 - (i * 12);
            context.drawString(textRenderer, "Chat line " + (i + 1), 4, y, 0xAAAAAA, config.textShadow);
        }
    }

    @Unique
    private void chattabs$showTabContextMenu(Minecraft client, int mouseX, int mouseY) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        chattabs$contextMenu = new ChatContextMenu(mouseX, mouseY, config,
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.newtab"), () -> {
                config.addChatTabFirst(new ChatTab());
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.delete"), () -> {
                if(chattabs$hoveredTab > 1) {
                    config.getChatTabs().remove(chattabs$hoveredTab - 1);
                    if(config.selectedTab >= chattabs$hoveredTab) config.selectedTab--;
                    chattabs$contextMenu = null;
                }
            }),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.moveleft"), () -> {
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.moveright"), () -> {
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.chat.configure"), () -> {
                Minecraft.getInstance().setScreen(new EditChatScreen(null));
                chattabs$contextMenu = null;
            })
        );
    }
}
