package net.wakcedon.chattabsreloaded.render;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class ChatHudOverlays {

    private static int applyAlpha(int color, float alpha) {
        if(alpha >= 0.999f) return color;
        int a = Math.round(((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static void fillRoundedRect(GuiGraphics ctx, int x, int y, int w, int h, int color) {
        fillRoundedRect(ctx, x, y, w, h, color, 1.0f);
    }

    public static void fillRoundedRect(GuiGraphics ctx, int x, int y, int w, int h, int color, float alpha) {
        int c = applyAlpha(color, alpha);
        if (w < 4 || h < 4) { ctx.fill(x, y, x + w, y + h, c); return; }
        ctx.fill(x + 2, y, x + w - 2, y + h, c);
        ctx.fill(x, y + 2, x + 2, y + h - 2, c);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, c);
    }
    
    public static int[] renderChatTabs(Minecraft client, int tabScroll, GuiGraphics context, int windowHeight, float chatScale, boolean expanded, int chatWidth, int mouseX, int mouseY, int messages, boolean mcTabUnreads, int baseYOffset, List<Integer> tabMidpointsOut) {
        return renderChatTabs(client, tabScroll, context, windowHeight, chatScale, expanded, chatWidth, mouseX, mouseY, messages, mcTabUnreads, baseYOffset, tabMidpointsOut, 1.0f);
    }

    public static int[] renderChatTabs(Minecraft client, int tabScroll, GuiGraphics context, int windowHeight, float chatScale, boolean expanded, int chatWidth, int mouseX, int mouseY, int messages, boolean mcTabUnreads, int baseYOffset, List<Integer> tabMidpointsOut, float alpha) {
        int hoveredTab = -1;
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled || config.getVisibleChatTabs().isEmpty() || chatScale == 0) return new int[]{hoveredTab, tabScroll};
        
        int height = 13;
        int x = 4;
        int y = Mth.floor((windowHeight - baseYOffset) / chatScale);
        y -= ((messages * (int)(9 * (client.options.chatLineSpacing().get() + 1))) + height + 6);
        int scrollerWidth = client.font.width("<") + 6;
        int tabNum = 0;
        int width;
        boolean hovered;
        if(tabScroll > -1) {
            hovered = (mouseX >= x && mouseX < x + scrollerWidth && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
            fillRoundedRect(context, x, y, scrollerWidth, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB(), alpha);
            context.drawString(client.font, "<", x + 2, y + 2, applyAlpha(0xFFFFFFFF, alpha), config.textShadow);
            if(hovered) {
                hoveredTab = -2;
            }
            x += scrollerWidth + 2;
        }
        boolean shouldScrollTabs = false;
        for(ChatTab tab : config.getVisibleChatTabs()) {
            if(tabScroll > -1 && tabNum < tabScroll) {
                tabNum++;
                shouldScrollTabs = true;
                continue;
            }
            Component tabName = tab.getDisplayComponent();
            String tabNameStr = tabName.getString();
            int textWidth = client.font.width(tabNameStr);
            int badgeW = 0;
            String badgeText = null;
            if(config.showUnreadCounter && config.selectedTab != tabNum && tab.hasUnreads()) {
                int count = tab.getUnreadCount();
                badgeText = count > 99 ? "99+" : String.valueOf(count);
                badgeW = Math.max(8, client.font.width(badgeText) + 4);
            }
            width = textWidth + 8 + (badgeText != null ? 4 + badgeW : 0);
            if(x + width > chatWidth - scrollerWidth) {
                shouldScrollTabs = true;
                hovered = (mouseX >= chatWidth - scrollerWidth && mouseX < chatWidth && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
                fillRoundedRect(context, chatWidth - scrollerWidth, y, scrollerWidth, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB(), alpha);
                context.fill(x, y, chatWidth - scrollerWidth, y + height, applyAlpha(config.bgColor.getRGB(), alpha));
                context.drawString(client.font, ">", chatWidth - scrollerWidth + 2, y + 2, applyAlpha(0xFFFFFFFF, alpha), config.textShadow);
                if(hovered) {
                    hoveredTab = -3;
                }
                break;
            }
            hovered = (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
            if(config.selectedTab == tabNum) {
                fillRoundedRect(context, x - 1, y - 1, width + 2, height + 2, config.selectedTabColor.getRGB(), alpha);
            } else if(tab.hasUnreads()) {
                int base = config.unreadColor.getRGB();
                if(tab.shouldBlink()) {
                    float pulse = (float)((Math.sin(System.currentTimeMillis() / 300.0 * Math.PI * 2) + 1.0) / 2.0);
                    int pa = Math.round(((base >> 24) & 0xFF) * (0.2f + 0.8f * pulse));
                    int pc = (pa << 24) | (base & 0x00FFFFFF);
                    fillRoundedRect(context, x - 1, y - 1, width + 2, height + 2, pc, alpha);
                } else {
                    int sa = ((base >> 24) & 0xFF) / 3;
                    int sc = (sa << 24) | (base & 0x00FFFFFF);
                    fillRoundedRect(context, x - 1, y - 1, width + 2, height + 2, sc, alpha);
                }
            }
            fillRoundedRect(context, x, y, width, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB(), alpha);
            int textColor = applyAlpha(0xFFFFFFFF, alpha);
            context.drawString(client.font, tabNameStr, x + 3, y + 2, textColor, config.textShadow);
            if(badgeText != null) {
                int badgeX = x + 3 + textWidth + 4;
                int badgeY = y + (height - 10) / 2;
                fillRoundedRect(context, badgeX, badgeY, badgeW, 10, applyAlpha(0xFFFF4444, alpha), alpha);
                context.drawString(client.font, badgeText, badgeX + (badgeW - client.font.width(badgeText)) / 2, badgeY + 1, textColor, config.textShadow);
            }
            if(tabMidpointsOut != null) {
                tabMidpointsOut.add(x + width / 2);
            }
            if(hovered) {
                hoveredTab = tabNum;
            }
            tabNum++;
            x += width + 5;
        }
        
        if(shouldScrollTabs && tabScroll < 0) {
            tabScroll = 0;
        } else if(!shouldScrollTabs) {
            tabScroll = -1;
        }
        
        return new int[]{hoveredTab, tabScroll};
    }

    public static void renderDropIndicator(GuiGraphics ctx, int x, int y, int height) {
        ctx.fill(x, y, x + 2, y + height, 0xFFFFFFFF);
    }

    public static void renderFloatingTab(GuiGraphics ctx, Minecraft client, String text, int mouseX, int mouseY) {
        int textWidth = client.font.width(text);
        int w = textWidth + 8;
        int h = 13;
        int x = mouseX - w / 2;
        int y = mouseY - h / 2;
        fillRoundedRect(ctx, x - 1, y - 1, w + 2, h + 2, 0xAAFFFFFF);
        fillRoundedRect(ctx, x, y, w, h, 0xAA000000);
        ctx.drawString(client.font, text, x + 3, y + 2, -1, true);
    }
}