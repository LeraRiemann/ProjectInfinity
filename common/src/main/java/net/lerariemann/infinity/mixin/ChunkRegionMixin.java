package net.lerariemann.infinity.mixin;

import net.minecraft.world.ChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRegion.class)
public class ChunkRegionMixin {
    @Inject(method = "isValidForSetBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;error(Ljava/lang/String;)V"), cancellable = true)
    private void injected(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
