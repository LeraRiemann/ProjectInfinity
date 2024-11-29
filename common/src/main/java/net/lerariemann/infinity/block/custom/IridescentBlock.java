package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.IridescentBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class IridescentBlock extends BlockWithEntity {

    public IridescentBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IridescentBlockEntity(pos, state);
    }
}
