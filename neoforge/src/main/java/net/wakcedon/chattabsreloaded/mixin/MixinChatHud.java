package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatComponent.class)
public abstract class MixinChatHud implements IChatHud {

    @Shadow
    private Style hoveredStyle;

    @Override
    public boolean chatTabs$mouseClicked(double mouseX, double mouseY, int button) {
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
    }

    @Override
    public void chatTabs$renderDummy(GuiGraphics context, Font textRenderer, int ticks, int mouseX, int mouseY, boolean checked) {
    }
}
