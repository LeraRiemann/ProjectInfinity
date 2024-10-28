package net.lerariemann.infinity.mixin;

import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AquiferSampler.Impl.class)
public class AqifierSamplerImplMixin {
    @Mutable
    @Final
    @Shadow
    private long[] blockPositions;

    @Inject(method = "index(III)I", at = @At("RETURN"), cancellable = true)
    void inj2(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        int i = cir.getReturnValue();
        if (i >= blockPositions.length) cir.setReturnValue(blockPositions.length - 1);
    }
}
