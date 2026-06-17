package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.mixininterface.IChatHud;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.gui.GuiGraphics;
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
        ((IChatHud)this.minecraft.gui.getChat()).chatTabs$renderContextMenu(context, width, height, mouseX, mouseY, deltaTicks);
    }

    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"))
    private void modifyChatMessage(ClientPacketListener instance, String content) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if(config.enabled && config.selectedTab > 0) {
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

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(((IChatHud)this.minecraft.gui.getChat()).chatTabs$mouseReleased(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if(((IChatHud)this.minecraft.gui.getChat()).chatTabs$mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }
}
