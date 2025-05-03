package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {
    @ModifyArg(method = "receiveNeighborFluids", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0), index = 1)
    BlockState inj(BlockState original, @Local Direction dir,
                   @Local(argsOnly = true) World world, @Local(argsOnly = true) BlockPos pos) {
        FluidState st = world.getFluidState(pos.offset(dir.getOpposite()));
        if (Iridescence.isIridescence(st) && !world.getFluidState(pos).isStill()) {
            return Iridescence.getRandomColorBlock(world, "glazed_terracotta").getDefaultState();
        }
        return original;
    }
}
