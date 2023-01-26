package net.lerariemann.infinity.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class NeitherPortalBlockEntity extends BlockEntity {
    private int dimension;

    public NeitherPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_PORTAL, pos, state);
    }

    public NeitherPortalBlockEntity(BlockPos pos, BlockState state, int i) {
        this(pos, state);
        this.dimension = i;
    }

    public int getDimension() {
        return this.dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
