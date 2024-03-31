package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.client.InfinityOptions;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DimensionType.class)
public class DimensionTypeMixin implements InfinityOptionsAccess {
    @Unique
    public InfinityOptions infinityoptions;

    @ModifyArg(method = "getSkyAngle", at = @At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;fractionalPart(D)D"), index = 0)
    private double injected(double value) {
        try {
            double timescale = getInfinityOptions().getTimeScale();
            return timescale*(value + 0.25) - 0.25;
        } catch (Exception e) {
            return value;
        }
    }
    public InfinityOptions getInfinityOptions() {
        return infinityoptions;
    }
    @Override
    public void setInfinityOptions(InfinityOptions options) {
        infinityoptions = options;
    }
}
