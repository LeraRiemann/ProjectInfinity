package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SkyRendering.class)
public class SkyRenderingMixin {
    @ModifyConstant(method = "tessellateStars", constant = @Constant(intValue = 1500))
    private int injected(int constant) {
        return infinity$options().getNumStars();
    }
    @ModifyConstant(method = "tessellateStars", constant = @Constant(floatValue = 0.15f))
    private float injected2(float constant) {
        return infinity$options().getStarSizeBase();
    }
    @ModifyConstant(method = "tessellateStars", constant = @Constant(floatValue = 0.1f))
    private float injected3(float constant) {
        return infinity$options().getStarSizeModifier();
    }

    @Unique
    private InfinityOptions infinity$options() {
        return InfinityOptions.ofClient(MinecraftClient.getInstance());
    }
}
