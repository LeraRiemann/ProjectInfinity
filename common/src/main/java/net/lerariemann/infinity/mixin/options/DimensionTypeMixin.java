package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements InfinityOptionsAccess {
    @Unique
    public InfinityOptions infinity$options;

    @ModifyArg(method = "getSkyAngle", at = @At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;fractionalPart(D)D"), index = 0)
    private double injected(double value) {
        try {
            double timescale = infinity$getOptions().getTimeScale();
            return timescale*(value + 0.25) - 0.25;
        } catch (Exception e) {
            return value;
        }
    }
    public InfinityOptions infinity$getOptions() {
        return InfinityOptions.nullSafe(infinity$options);
    }
    @Override
    public void infinity$setOptions(InfinityOptions options) {
        infinity$options = options;
    }
}
