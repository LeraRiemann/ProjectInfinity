package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.block.entity.CosmicAltarEntity;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CosmicAltar extends BlockWithEntity {
    public CosmicAltar(Settings settings) {
        super(settings);
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CosmicAltarEntity(pos, state);
    }
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) return checkType(type, ModBlockEntities.COSMIC_ALTAR.get(), CosmicAltarEntity::serverTick);
        return null;
    }
}
