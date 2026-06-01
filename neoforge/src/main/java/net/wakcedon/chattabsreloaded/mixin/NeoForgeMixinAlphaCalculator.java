package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.NeoForgeConfig;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatComponent.AlphaCalculator.class)
public interface NeoForgeMixinAlphaCalculator {

    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 200.0))
    private static double modifyMaxTimeTicks(double constant) {
        NeoForgeConfig config = NeoForgeMod.getConfig();
        if(config.getConfig().enabled) {
            return config.getConfig().previewTime * 20;
        }
        return constant;
    }
    
    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 10.0))
    private static double modifyMaxTimeSeconds(double constant) {
        NeoForgeConfig config = NeoForgeMod.getConfig();
        if(config.getConfig().enabled) {
            return config.getConfig().previewTime;
        }
        return constant;
    }
}