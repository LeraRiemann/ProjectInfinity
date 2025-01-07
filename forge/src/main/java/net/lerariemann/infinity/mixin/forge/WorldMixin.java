package net.lerariemann.infinity.mixin.forge;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    /* for some reason minecraft's own code can softlock the game when beating the dragon without this check */
    @Inject(method = "getBlockEntity", at = @At(value = "HEAD"), cancellable = true)
    private void inj(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (pos == null) {
            cir.setReturnValue(null);
        }
    }
}
