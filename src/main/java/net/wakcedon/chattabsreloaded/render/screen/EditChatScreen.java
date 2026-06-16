package net.wakcedon.chattabsreloaded.render.screen;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditChatScreen extends Screen {
    
    private final Screen parent;
    
    private boolean editFocused = true;
    
    private float topEdgeTicks = 0;
    private float rightEdgeTicks = 0;
    
    private double dragStartX;
    private double dragStartY;
    
    private int dragStartWidth;
    private int dragStartHeight;
    
    // -1 - not dragging, 0 - drag height, 1 - drag width
    private int dragging = -1;
    
    private final ChatTabsConfigBase config;
    
    public EditChatScreen(Screen parent) {
        super(Component.translatable("chattabs.editchatscreen"));
        this.parent = parent;
        this.config = ChatTabsConfigBase.getInstance();
        addRenderableWidget(Button.builder(Component.translatable("chattabs.editchatscreen.editfocused"), button -> editFocused = !editFocused)
                .bounds(4, height - 30, font.width(Component.translatable("chattabs.editchatscreen.editfocused")) + 28, 20)
                .build());
    }
    
    @Override
    protected void init() {
        int lineHeight = (int)(9 * (minecraft.options.chatLineSpacing().get() + 1));
        config.chatHeightFocused = (config.chatHeightFocused / lineHeight) * lineHeight;
        config.chatHeightUnfocused = (config.chatHeightUnfocused / lineHeight) * lineHeight;
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        ((IChatHud)minecraft.gui.getChat()).chatTabs$renderDummy(context, this.font, this.minecraft.gui.getGuiTicks(), mouseX, mouseY, editFocused);
        super.render(context, mouseX, mouseY, deltaTicks);
        
        context.pose().pushPose();
        
        int chatWidth = config.chatWidth + (int)(12 * minecraft.options.chatScale().get());
        int chatY = height - 41;
        int chatHeight = (editFocused ? config.chatHeightFocused : config.chatHeightUnfocused);
        int chatVisualHeight = (int)(chatHeight * minecraft.options.chatScale().get());
        // top edge
        context.hLine(0, chatWidth, chatY - chatVisualHeight, fade(-1, topEdgeTicks));
        if((mouseX >= 0 && mouseX < chatWidth && mouseY >= chatY - chatVisualHeight - 3 && mouseY < chatY - chatVisualHeight + 3) || dragging == 0) {
            topEdgeTicks += deltaTicks / 2f;
            if(topEdgeTicks > 1) {
                topEdgeTicks = 1;
            }
        } else {
            topEdgeTicks -= deltaTicks / 2f;
            if(topEdgeTicks < 0) {
                topEdgeTicks = 0;
            }
        }
        // right edge
        context.vLine(chatWidth, chatY - chatVisualHeight, chatY, fade(-1, rightEdgeTicks));
        if((mouseX >= chatWidth - 3 && mouseX < chatWidth + 3 && mouseY >= chatY - chatVisualHeight && mouseY < chatY) || dragging == 1) {
            rightEdgeTicks += deltaTicks / 2f;
            if(rightEdgeTicks > 1) {
                rightEdgeTicks = 1;
            }
        } else {
            rightEdgeTicks -= deltaTicks / 2f;
            if(rightEdgeTicks < 0) {
                rightEdgeTicks = 0;
            }
        }
        
        context.pose().popPose();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(minecraft.options.chatScale().get() == 0) return super.mouseClicked(mouseX, mouseY, button);
        int chatWidth = config.chatWidth + 12;
        int chatY = height - 41;
        int chatHeight = (editFocused ? config.chatHeightFocused : config.chatHeightUnfocused);
        int chatVisualHeight = (int)(chatHeight * minecraft.options.chatScale().get());
        if(mouseX >= 0 && mouseX < chatWidth && mouseY >= chatY - chatVisualHeight - 3 && mouseY < chatY - chatVisualHeight + 3) {
            dragging = 0;
            dragStartX = mouseX;
            dragStartY = mouseY / minecraft.options.chatScale().get();
            dragStartHeight = chatHeight;
            return true;
        } else if(mouseX >= chatWidth - 3 && mouseX < chatWidth + 3 && mouseY >= chatY - chatVisualHeight && mouseY < chatY) {
            dragging = 1;
            dragStartX = mouseX;
            dragStartY = mouseY / minecraft.options.chatScale().get();
            dragStartWidth = chatWidth - 12;
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(minecraft.options.chatScale().get() == 0) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if(dragging == 0) {
            int lineHeight = (int)(9 * (minecraft.options.chatLineSpacing().get() + 1));
            if(editFocused) {
                config.chatHeightFocused = (Math.clamp(dragStartHeight + (int)(dragStartY - (mouseY / minecraft.options.chatScale().get())), 20, 900) / lineHeight) * lineHeight;
            } else {
                config.chatHeightUnfocused = (Math.clamp(dragStartHeight + (int)(dragStartY - (mouseY / minecraft.options.chatScale().get())), 20, 900) / lineHeight) * lineHeight;
            }
            return true;
        } else if(dragging == 1) {
            config.chatWidth = Math.max(dragStartWidth - (int)(dragStartX - mouseX), 40);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(dragging != -1) {
            dragging = -1;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
    
    private int fade(int color, float amount) {
        int alpha = (color >> 24) & 0xFF;
        alpha = (int)((float)alpha * amount);
        return (color & 0x00FFFFFF) + (alpha << 24);
    }
}