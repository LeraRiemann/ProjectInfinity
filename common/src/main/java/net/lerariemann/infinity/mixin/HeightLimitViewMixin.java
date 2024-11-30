package net.lerariemann.infinity.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HeightLimitView.class)
public interface HeightLimitViewMixin {

    /* for some reason minecraft's own code can softlock the game when beating the dragon without this check */
    @Inject(method = "isOutOfHeightLimit(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"), cancellable = true)
    private void inj(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (pos == null) {
            cir.setReturnValue(true);
        }
    }
}
