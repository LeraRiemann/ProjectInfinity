package net.lerariemann.infinity.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @ModifyExpressionValue(method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    boolean inj(boolean original, @Local(argsOnly = true) World world) {
        if (InfinityMod.isInfinity(world)) return false;
        return original;
    }

    @ModifyArg(method = "flow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), index = 1)
    BlockState inj(BlockState original, @Local(argsOnly = true) WorldAccess world, @Local(argsOnly = true) BlockPos pos) {
        if (world.getBlockState(pos).isOf(ModBlocks.IRIDESCENCE.get())) {
            return Iridescence.getRandomColorBlock(world, "glazed_terracotta").getDefaultState();
        }
        return original;
    }
}
