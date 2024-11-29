package net.lerariemann.infinity.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class IridescentBlockEntity extends BlockEntity {
    public IridescentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRIDESCENT.get(), pos, state);
    }

    public boolean shouldDrawSide(Direction direction) {
        if (world!=null) return Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction, this.getPos().offset(direction));
        return false;
    }
}
