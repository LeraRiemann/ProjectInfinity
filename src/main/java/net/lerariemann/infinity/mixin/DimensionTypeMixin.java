package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.MinecraftClientAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    @ModifyArg(method = "getSkyAngle", at = @At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;fractionalPart(D)D"), index = 0)
    private double injected(double value) {
        try {
            double timescale = ((MinecraftClientAccess)MinecraftClient.getInstance()).getInfinityOptions().getTimeScale();
            return timescale*(value + 0.25) - 0.25;
        } catch (Exception e) {
            return value;
        }
    }
}
