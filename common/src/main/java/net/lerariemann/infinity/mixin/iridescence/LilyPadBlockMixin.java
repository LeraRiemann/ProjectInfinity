package net.lerariemann.infinity.mixin.iridescence;

import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LilyPadBlock.class)
public class LilyPadBlockMixin {
    /* make lilypads plantable on iridescence */
    @Inject(method = "canPlantOnTop", at = @At(value = "RETURN"), cancellable = true)
    void inj(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.getFluidState(pos).isIn(FluidTags.WATER) && world.getFluidState(pos.up()).getFluid() == Fluids.EMPTY) {
            cir.setReturnValue(true);
        }
    }
}
