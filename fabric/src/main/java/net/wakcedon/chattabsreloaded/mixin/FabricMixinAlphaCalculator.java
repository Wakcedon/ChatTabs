package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatHud.AlphaCalculator.class)
public interface FabricMixinAlphaCalculator {

    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 200.0))
    private static double modifyMaxTimeTicks(double constant) {
        if(ChatTabsConfigBase.getInstance().enabled) {
            return ChatTabsConfigBase.getInstance().previewTime * 20;
        }
        return constant;
    }
    
    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 10.0))
    private static double modifyMaxTimeSeconds(double constant) {
        if(ChatTabsConfigBase.getInstance().enabled) {
            return ChatTabsConfigBase.getInstance().previewTime;
        }
        return constant;
    }
}
