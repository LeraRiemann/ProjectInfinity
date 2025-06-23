package net.lerariemann.infinity.mixin.fixes;

import net.lerariemann.infinity.entity.custom.AntEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class ColllisionShapeMixin {
    @Shadow private FluidState fluidState;

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
            at = @At("RETURN"), cancellable = true)
    private void inj(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        try { //1.21.3 - this feels ugly
            FluidState fluidState = world.getFluidState(pos);
            if (!fluidState.isIn(FluidTags.WATER)) return;
            int level = fluidState.getLevel();
            if (level == 0) return;
            int trueLevel = 2 * (level - 1) + 1;
            if (context.isAbove(AntEntity.getWaterCollisionShape(trueLevel - 1), pos, true)
                    && context.canWalkOnFluid(world.getFluidState(pos.up()), fluidState)) {
                cir.setReturnValue(VoxelShapes.union(cir.getReturnValue(), AntEntity.getWaterCollisionShape(trueLevel)));
            }
        } catch (IllegalStateException ignored) { }
    }
}
