package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.wakcedon.chattabsreloaded.render.ChatContextMenu;
import net.wakcedon.chattabsreloaded.render.ChatHudOverlays;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatLine;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.NeoForgeChatLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public abstract class MixinChatHud implements IChatHud {

    @Shadow
    private List<?> trimmedMessages;

    @Unique
    private int chattabs$tabScroll = -1;
    @Unique
    private int chattabs$hoveredTab = -1;
    @Unique
    private boolean chattabs$dragging;
    @Unique
    private int chattabs$dragTabIndex = -1;
    @Unique
    private int chattabs$dropIndex = -1;
    @Unique
    private int chattabs$dragMouseX;
    @Unique
    private int chattabs$dragMouseY;
    @Unique
    private int chattabs$dragTabX; // tab's x position when drag started
    @Unique
    private ChatContextMenu chattabs$contextMenu;
    @Unique
    private List<?> chattabs$savedTrimmedMessages;

    @Unique
    private float chattabs$animAlpha = 0.0f;
    @Unique
    private float chattabs$animTarget = 0.0f;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("TAIL"))
    private void chattabs$onRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        chattabs$renderTabs(guiGraphics, mouseX, mouseY);
    }

    @Unique
    private void chattabs$renderTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        if(config.tabAnimationFade) {
            boolean chatOpen = client.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
            float target = chatOpen ? 1.0f : 0.0f;
            chattabs$animTarget = target;
            if(chattabs$animAlpha < target) {
                chattabs$animAlpha = Math.min(chattabs$animAlpha + 0.12f, target);
            } else if(chattabs$animAlpha > target) {
                chattabs$animAlpha = Math.max(chattabs$animAlpha - 0.12f, target);
            }
        } else {
            chattabs$animAlpha = 1.0f;
        }

        if(client.screen instanceof net.minecraft.client.gui.screens.ChatScreen) return;

        if(chattabs$animAlpha <= 0.001f) return;

        int windowHeight = client.getWindow().getGuiScaledHeight();
        float chatScale = client.options.chatScale().get().floatValue();

        ChatHudOverlays.renderChatTabs(
            client, chattabs$tabScroll, guiGraphics, windowHeight, chatScale,
            false, config.chatWidth, mouseX, mouseY, 0, false, 0, null, chattabs$animAlpha
        );
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("HEAD"))
    private void chattabs$filterBeforeRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        ChatTab selectedTab = config.getSelectedChatTab();
        if(selectedTab == null) return;

        chattabs$savedTrimmedMessages = new ArrayList<>(this.trimmedMessages);
        this.trimmedMessages.removeIf(line -> {
            NeoForgeChatLine wrapped = new NeoForgeChatLine(line);
            return !selectedTab.getFilter().test(wrapped);
        });
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private void chattabs$restoreAfterRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if(chattabs$savedTrimmedMessages != null) {
            this.trimmedMessages.clear();
            this.trimmedMessages.addAll((List) chattabs$savedTrimmedMessages);
            chattabs$savedTrimmedMessages = null;
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void chattabs$onAddMessage(Component message, CallbackInfo ci) {
        chattabs$processMessage(message);
    }

    @Unique
    private void chattabs$processMessage(Component message) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        ChatLine line = new NeoForgeChatLine(message, true);
        for(ChatTab tab : config.getVisibleChatTabs()) {
            if(tab.getFilter().test(line)) {
                tab.addChatLine(line);
            }
        }
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
        if(chattabs$hoveredTab >= 0) {
            if(button == 1) {
                chattabs$showTabContextMenu(client, (int)mouseX, (int)mouseY);
            } else if(config.tabDragAndDrop) {
                java.util.List<ChatTab> visible = config.getVisibleChatTabs();
                if(chattabs$hoveredTab < visible.size()) {
                    int actualIdx = config.getChatTabs().indexOf(visible.get(chattabs$hoveredTab));
                    chatTabs$startDrag(actualIdx, (int)mouseX, (int)mouseY, 0);
                }
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
    public float chatTabs$getAnimAlpha() {
        return chattabs$animAlpha;
    }

    @Override
    public void chatTabs$setAnimTarget(boolean visible) {
        chattabs$animTarget = visible ? 1.0f : 0.0f;
    }

    @Override
    public void chatTabs$setHoverState(int hoveredTab, int tabScroll) {
        chattabs$hoveredTab = hoveredTab;
        chattabs$tabScroll = tabScroll;
    }

    @Override
    public boolean chatTabs$isDragging() {
        return chattabs$dragging;
    }

    @Override
    public int chatTabs$getDragTabIndex() {
        return chattabs$dragTabIndex;
    }

    @Override
    public int chatTabs$getDropIndex() {
        return chattabs$dropIndex;
    }

    @Override
    public void chatTabs$startDrag(int tabIndex, int mouseX, int mouseY, int tabX) {
        chattabs$dragging = true;
        chattabs$dragTabIndex = tabIndex;
        chattabs$dragMouseX = mouseX;
        chattabs$dragMouseY = mouseY;
        chattabs$dragTabX = tabX;
        chattabs$dropIndex = tabIndex;
    }

    @Override
    public void chatTabs$endDrag(int mouseX) {
        if(!chattabs$dragging) return;

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();

        if(Math.abs(mouseX - chattabs$dragMouseX) < 5) {
            java.util.List<ChatTab> visibleTabs = config.getVisibleChatTabs();
            ChatTab tab = config.getChatTabs().get(chattabs$dragTabIndex);
            int visibleIdx = visibleTabs.indexOf(tab);
            if(visibleIdx >= 0) config.selectedTab = visibleIdx;
            chattabs$dragging = false;
            chattabs$dragTabIndex = -1;
            chattabs$dropIndex = -1;
            return;
        }

        java.util.List<ChatTab> allTabs = config.getChatTabs();
        java.util.List<ChatTab> visibleTabs = config.getVisibleChatTabs();
        int from = chattabs$dragTabIndex;
        int toVisible = chattabs$dropIndex;

        if(from >= 0 && from < allTabs.size() && toVisible >= 0) {
            int to = toVisible < visibleTabs.size() ? allTabs.indexOf(visibleTabs.get(toVisible)) : allTabs.size();
            if(to < 0) to = allTabs.size();

            if(from != to) {
                ChatTab tab = allTabs.remove(from);
                int insertAt = to > from ? to - 1 : to;
                allTabs.add(insertAt, tab);

                if(config.selectedTab == from) {
                    config.selectedTab = insertAt;
                } else if(from < config.selectedTab && insertAt >= config.selectedTab) {
                    config.selectedTab--;
                } else if(from > config.selectedTab && insertAt <= config.selectedTab) {
                    config.selectedTab++;
                }
            }
        }

        chattabs$dragging = false;
        chattabs$dragTabIndex = -1;
        chattabs$dropIndex = -1;
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
                if(chattabs$hoveredTab > 0) {
                    config.getChatTabs().remove(chattabs$hoveredTab);
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
