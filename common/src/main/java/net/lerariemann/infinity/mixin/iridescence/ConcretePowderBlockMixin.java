package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {
    /* Iridescence should not convert concrete powder */
    @Inject(method= "hardensIn(Lnet/minecraft/block/BlockState;)Z",
    at = @At("RETURN"), cancellable = true)
    private static void inj(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (Iridescence.isIridescence(state.getFluidState())) cir.setReturnValue(false);
    }
}
