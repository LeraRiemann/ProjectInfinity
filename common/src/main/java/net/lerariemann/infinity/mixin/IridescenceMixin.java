package net.lerariemann.infinity.mixin;

import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import dev.architectury.fluid.FluidStack;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleArchitecturyFluidAttributes.class)
public class IridescenceMixin {
    @Inject(method = "getColor(Ldev/architectury/fluid/FluidStack;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I",
    at = @At("HEAD"), cancellable = true)
    void inj(@Nullable FluidStack stack, @Nullable BlockRenderView level, @Nullable BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (level != null && pos != null && level.getBlockState(pos).isOf(ModBlocks.IRIDESCENCE.get())) {
            cir.setReturnValue(Iridescence.color(pos));
        }
    }
}
