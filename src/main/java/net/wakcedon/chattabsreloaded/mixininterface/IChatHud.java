package net.wakcedon.chattabsreloaded.mixininterface;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface IChatHud {
    
    boolean chatTabs$mouseClicked(double mouseX, double mouseY, int button);
    boolean chatTabs$mouseReleased(double mouseX, double mouseY, int button);
    boolean chatTabs$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    
    void chatTabs$renderContextMenu(GuiGraphics context, int windowWidth, int windowHeight, int mouseX, int mouseY, float deltaTicks);
    
    void chatTabs$renderDummy(GuiGraphics context, Font textRenderer, int ticks, int mouseX, int mouseY, boolean checked);
}
