package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.KelpBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KelpBlock.class)
public abstract class KelpBlockMixin extends AbstractPlantStemBlock {
    protected KelpBlockMixin(Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater, double growthChance) {
        super(settings, growthDirection, outlineShape, tickWater, growthChance);
    }

    @Inject(method="getPlacementState", at= @At(value = "HEAD"), cancellable = true)
    void inj(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (Iridescence.isIridescence(ctx.getWorld(), ctx.getBlockPos())) {
            BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(this.growthDirection));
            if (blockState.isOf(ModBlocks.IRIDESCENT_KELP_PLANT.get()) || blockState.isOf(ModBlocks.IRIDESCENT_KELP.get()))
                cir.setReturnValue(ModBlocks.IRIDESCENT_KELP_PLANT.get().getDefaultState());
            cir.setReturnValue(ModBlocks.IRIDESCENT_KELP.get().getRandomGrowthState(ctx.getWorld()));
        }
    }
}
