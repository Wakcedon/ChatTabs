package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.wakcedon.chattabsreloaded.config.ProfilesConfig;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.wakcedon.chattabsreloaded.render.ChatContextMenu;
import net.wakcedon.chattabsreloaded.render.ChatHudOverlays;
import net.wakcedon.chattabsreloaded.render.GhostTab;
import net.wakcedon.chattabsreloaded.render.screen.ProfileListScreen;
import net.wakcedon.chattabsreloaded.render.screen.TabEditScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatLine;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.NeoForgeChatLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
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
    private final java.util.ArrayList<GhostTab> chattabs$removingTabs = new java.util.ArrayList<>();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("TAIL"))
    private void chattabs$onRender(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        chattabs$tickAnims();
        chattabs$renderTabs(guiGraphics, mouseX, mouseY);
    }

    @Unique
    private void chattabs$tickAnims() {
        Minecraft client = Minecraft.getInstance();
        boolean chatOpen = client.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();

        if(config.tabAppearAnimation) {
            if(chatOpen) {
                chattabs$animAlpha = Math.min(chattabs$animAlpha + 0.08f, 1.0f);
            } else if(config.tabAnimationFade) {
                chattabs$animAlpha = Math.max(chattabs$animAlpha - 0.08f, 0.0f);
            } else {
                chattabs$animAlpha = 0.0f;
            }
        } else {
            chattabs$animAlpha = chatOpen ? 1.0f : 0.0f;
        }

        java.util.Iterator<GhostTab> it = chattabs$removingTabs.iterator();
        while(it.hasNext()) {
            GhostTab gt = it.next();
            gt.alpha -= 0.06f;
            if(gt.alpha <= 0.0f) {
                it.remove();
            }
        }
    }

    @Unique
    private void chattabs$renderTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled) return;

        if(client.screen instanceof net.minecraft.client.gui.screens.ChatScreen) return;

        if(chattabs$animAlpha <= 0.001f) return;

        int windowHeight = client.getWindow().getGuiScaledHeight();
        float chatScale = client.options.chatScale().get().floatValue();

        ChatHudOverlays.renderChatTabs(
            client, chattabs$tabScroll, guiGraphics, windowHeight, chatScale,
            false, config.chatWidth, mouseX, mouseY, 0, false, 0, null, chattabs$animAlpha
        );

        chattabs$renderGhostTabs(guiGraphics, client, windowHeight, chatScale, config.chatWidth, mouseX, mouseY);
    }

    @Unique
    private void chattabs$renderGhostTabs(GuiGraphics ctx, Minecraft client, int windowHeight, float chatScale, int chatWidth, int mouseX, int mouseY) {
        if(chattabs$removingTabs.isEmpty()) return;
        float alpha = chattabs$animAlpha;
        int x = 4 + 12 + 2;
        int y = net.minecraft.util.Mth.floor(windowHeight / chatScale) - 19;
        int height = 13;
        for(GhostTab gt : chattabs$removingTabs) {
            String ghostStr = Component.translatable("chattabs.ghost.deleted", gt.name).getString();
            int tw = client.font.width(ghostStr);
            int w = tw + 8;
            float a = alpha * gt.alpha;
            if(a > 0.01f) {
                ChatHudOverlays.fillRoundedRect(ctx, x, y, w, height, 0x44FF4444, a);
                ctx.drawString(client.font, ghostStr, x + 3, y + 2, 0xFFFFFF | (Math.round(255 * a) << 24));
            }
            x += w + 4;
        }
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
            } else {
                java.util.List<ChatTab> visible = config.getVisibleChatTabs();
                if(chattabs$hoveredTab < visible.size()) {
                    chattabs$selectTab(chattabs$hoveredTab);
                    if(config.tabDragAndDrop) {
                        int actualIdx = config.getChatTabs().indexOf(visible.get(chattabs$hoveredTab));
                        chatTabs$startDrag(actualIdx, (int)mouseX, (int)mouseY, 0);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Unique
    private void chattabs$selectTab(int visibleIdx) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        ChatTab oldTab = config.getSelectedChatTab();
        if(oldTab != null) oldTab.setFocused(false);
        config.selectedTab = visibleIdx;
        ChatTab newTab = config.getSelectedChatTab();
        if(newTab != null) newTab.setFocused(true);
    }

    @Override
    public boolean chatTabs$mouseReleased(double mouseX, double mouseY, int button) {
        if(chattabs$dragging) {
            chatTabs$endDrag((int)mouseX);
            return true;
        }
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
    public void chatTabs$showRemoveAnim(String tabId, String tabName) {
        chattabs$removingTabs.add(new GhostTab(tabId, tabName));
    }

    @Override
    public java.util.List<GhostTab> chatTabs$getRemovingTabs() {
        return chattabs$removingTabs;
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
        if(tabIndex < 0) return;
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
            java.util.List<ChatTab> allTabs = config.getChatTabs();
            if(chattabs$dragTabIndex < 0 || chattabs$dragTabIndex >= allTabs.size()) {
                chattabs$dragging = false;
                chattabs$dragTabIndex = -1;
                chattabs$dropIndex = -1;
                return;
            }
            java.util.List<ChatTab> visibleTabs = config.getVisibleChatTabs();
            ChatTab tab = allTabs.get(chattabs$dragTabIndex);
            int visibleIdx = visibleTabs.indexOf(tab);
            if(visibleIdx >= 0) chattabs$selectTab(visibleIdx);
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
            context.drawString(textRenderer, Component.translatable("chattabs.dummy.line", i + 1), 4, y, 0xAAAAAA, config.textShadow);
        }
    }

    @Unique
    private void chattabs$showTabContextMenu(Minecraft client, int mouseX, int mouseY) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        int hoveredTabAtCreation = chattabs$hoveredTab;
        chattabs$contextMenu = new ChatContextMenu(mouseX, mouseY, config,
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.newtab"), () -> {
                config.addChatTabFirst(new ChatTab());
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.edit"), () -> {
                if(hoveredTabAtCreation >= 0) {
                    List<ChatTab> visibleTabs = config.getVisibleChatTabs();
                    if(hoveredTabAtCreation < visibleTabs.size()) {
                        Minecraft.getInstance().setScreen(new TabEditScreen(null, visibleTabs.get(hoveredTabAtCreation)));
                    }
                }
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.delete"), () -> {
                if(hoveredTabAtCreation >= 0) {
                    List<ChatTab> visibleTabs = config.getVisibleChatTabs();
                    if(hoveredTabAtCreation < visibleTabs.size()) {
                        ChatTab tab = visibleTabs.get(hoveredTabAtCreation);
                        int allIdx = config.getChatTabs().indexOf(tab);
                        if(allIdx >= 0) {
                            chatTabs$showRemoveAnim(tab.getId(), tab.getDisplayComponent().getString());
                            config.getChatTabs().remove(allIdx);
                            if(config.selectedTab >= allIdx) config.selectedTab--;
                        }
                    }
                    chattabs$contextMenu = null;
                }
            }),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.moveleft"), () -> {
                chattabs$moveTab(hoveredTabAtCreation, -1);
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.contextmenu.tab.moveright"), () -> {
                chattabs$moveTab(hoveredTabAtCreation, 1);
                chattabs$contextMenu = null;
            }),
            new ChatContextMenu.Element(),
            new ChatContextMenu.Element(Component.translatable("chattabsconfig.serverprofiles"), () -> {
                Minecraft.getInstance().setScreen(new ProfileListScreen(null));
                chattabs$contextMenu = null;
            })
        );
    }

    @Unique
    private void chattabs$moveTab(int visibleIdx, int direction) {
        if(visibleIdx < 0) return;
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        List<ChatTab> visible = config.getVisibleChatTabs();
        if(visibleIdx >= visible.size()) return;
        int newIdx = visibleIdx + direction;
        if(newIdx < 0 || newIdx >= visible.size()) return;

        ChatTab tab = visible.get(visibleIdx);
        ChatTab neighbor = visible.get(newIdx);

        if(config instanceof NeoForgeChatTabsConfig ncfg) {
            String serverIp = Minecraft.getInstance().getCurrentServer() != null
                ? Minecraft.getInstance().getCurrentServer().ip : null;
            if(serverIp != null) {
                ProfilesConfig pc = ncfg.getProfilesConfig();
                ServerTabProfile profile = pc.findProfile(serverIp);
                if(profile != null && profile.getTabs() != null && !profile.getTabs().isEmpty()) {
                    List<ChatTab> profileTabs = profile.getTabs();
                    int tabIdx = profileTabs.indexOf(tab);
                    int neighIdx = profileTabs.indexOf(neighbor);
                    if(tabIdx >= 0 && neighIdx >= 0) {
                        profileTabs.set(tabIdx, neighbor);
                        profileTabs.set(neighIdx, tab);
                        pc.save(ncfg.getProfilesPath());
                    }
                    return;
                }
            }
        }

        List<ChatTab> all = config.getChatTabs();
        int tabAllIdx = all.indexOf(tab);
        int neighAllIdx = all.indexOf(neighbor);
        if(tabAllIdx < 0 || neighAllIdx < 0) return;

        all.set(tabAllIdx, neighbor);
        all.set(neighAllIdx, tab);

        if(config.selectedTab == visibleIdx) {
            config.selectedTab = newIdx;
        } else if(config.selectedTab == newIdx) {
            config.selectedTab = visibleIdx;
        }
        config.save();
    }
}
