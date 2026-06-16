package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {
    
    @Shadow
    private CommandSuggestions commandSuggestions;
    
    @Shadow
    protected abstract boolean insertionClickMode();
    
    protected MixinChatScreen(Component title) {
        super(title);
    }
    
    @Inject(method = "render", at = @At("TAIL"))
    private void renderChatContextMenu(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        ChatComponent chatHud = this.minecraft.gui.getChat();
        ((IChatHud)chatHud).chatTabs$renderContextMenu(context, width, height, mouseX, mouseY, deltaTicks);
    }
    
    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"))
    private void modifyChatMessage(ClientPacketListener instance, String content) {
        if(ChatTabsConfigBase.getInstance().enabled && ChatTabsConfigBase.getInstance().selectedTab > 0) {
            content = ChatTabsConfigBase.getInstance().getSelectedChatTab().modifySend(content);
            if (content.startsWith("/")) {
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
        ChatComponent chatHud = this.minecraft.gui.getChat();
        if(((IChatHud)chatHud).chatTabs$mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ChatComponent chatHud = this.minecraft.gui.getChat();
        if(((IChatHud)chatHud).chatTabs$mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        ChatComponent chatHud = this.minecraft.gui.getChat();
        if(((IChatHud)chatHud).chatTabs$mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}