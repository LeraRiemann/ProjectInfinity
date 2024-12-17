package net.lerariemann.infinity.mixin.fixes;

import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AquiferSampler.Impl.class)
public class AquifierSamplerImplMixin {
    @Mutable
    @Final
    @Shadow
    private long[] blockPositions;

    /* for some reason minecraft's own code can fail with an array out of bounds exception if i don't clamp this */
    @Inject(method = "index(III)I", at = @At("RETURN"), cancellable = true)
    void inj2(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        int i = cir.getReturnValue();
        if (i >= blockPositions.length) cir.setReturnValue(blockPositions.length - 1);
    }
}