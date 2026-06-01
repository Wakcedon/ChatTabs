package net.wakcedon.chattabsreloaded.mixin;

import net.wakcedon.chattabsreloaded.config.FabricConfig;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatComponent.AlphaCalculator.class)
public interface FabricMixinAlphaCalculator {

    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 200.0))
    private static double modifyMaxTimeTicks(double constant) {
        if(FabricConfig.getConfig().getConfig().enabled) {
            return FabricConfig.getConfig().getConfig().previewTime * 20;
        }
        return constant;
    }
    
    @ModifyConstant(method = "lambda$timeBased$0", constant = @Constant(doubleValue = 10.0))
    private static double modifyMaxTimeSeconds(double constant) {
        if(FabricConfig.getConfig().getConfig().enabled) {
            return FabricConfig.getConfig().getConfig().previewTime;
        }
        return constant;
    }
}