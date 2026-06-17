package net.wakcedon.chattabsreloaded.render;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.render.screen.EditChatScreen;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChatHudOverlays {

    private static void fillRoundedRect(GuiGraphics ctx, int x, int y, int w, int h, int color) {
        if (w < 4 || h < 4) { ctx.fill(x, y, x + w, y + h, color); return; }
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + 2, y + h - 2, color);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, color);
    }
    
    public static int[] renderChatTabs(Minecraft client, int tabScroll, GuiGraphics context, int windowHeight, float chatScale, boolean expanded, int chatWidth, int mouseX, int mouseY, int messages, boolean mcTabUnreads, int baseYOffset) {
        int hoveredTab = -1;
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(!config.enabled || config.getVisibleChatTabs().isEmpty() || chatScale == 0) return new int[]{hoveredTab, tabScroll};
        
        int height = 13;
        int x = 4;
        int y = Mth.floor((windowHeight - baseYOffset) / chatScale);
        y -= ((messages * (int)(9 * (client.options.chatLineSpacing().get() + 1))) + height + 4);
        int scrollerWidth = client.font.width("<") + 6;
        int tabNum = 0;
        int width;
        boolean hovered;
        if(tabScroll > -1) {
            hovered = (mouseX >= x && mouseX < x + scrollerWidth && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
            fillRoundedRect(context, x, y, scrollerWidth, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB());
            context.drawString(client.font, "<", x + 2, y + 2, -1, config.textShadow);
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
            width = client.font.width(tabName.getString()) + 8;
            if(x + width > chatWidth - scrollerWidth) {
                shouldScrollTabs = true;
                hovered = (mouseX >= chatWidth - scrollerWidth && mouseX < chatWidth && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
                fillRoundedRect(context, chatWidth - scrollerWidth, y, scrollerWidth, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB());
                context.fill(x, y, chatWidth - scrollerWidth, y + height, config.bgColor.getRGB());
                context.drawString(client.font, ">", chatWidth - scrollerWidth + 2, y + 2, -1, config.textShadow);
                if(hovered) {
                    hoveredTab = -3;
                }
                break;
            }
            hovered = (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) && !(client.screen instanceof EditChatScreen);
            fillRoundedRect(context, x, y, width, height, hovered ? config.bgColorHovered.getRGB() : config.bgColor.getRGB());
            context.drawString(client.font, tabName.getString(), x + 3, y + 2, -1, config.textShadow);
            if(config.selectedTab == tabNum) {
                int c = config.selectedTabColor.getRGB();
                context.fill(x + 2, y - 1, x + width - 2, y, c);
                context.fill(x + 2, y + height, x + width - 2, y + height + 1, c);
                context.fill(x - 1, y + 2, x, y + height - 2, c);
                context.fill(x + width, y + 2, x + width + 1, y + height - 2, c);
            } else if(tab.hasUnreads()) {
                int c = config.unreadColor.getRGB();
                context.fill(x + 2, y - 1, x + width - 2, y, c);
                context.fill(x + 2, y + height, x + width - 2, y + height + 1, c);
                context.fill(x - 1, y + 2, x, y + height - 2, c);
                context.fill(x + width, y + 2, x + width + 1, y + height - 2, c);
            }
            if(hovered) {
                hoveredTab = tabNum;
            }
            tabNum++;
            x += width + 4;
        }
        
        if(shouldScrollTabs && tabScroll < 0) {
            tabScroll = 0;
        } else if(!shouldScrollTabs) {
            tabScroll = -1;
        }
        
        return new int[]{hoveredTab, tabScroll};
    }
}