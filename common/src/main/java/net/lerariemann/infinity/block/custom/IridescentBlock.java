package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class IridescentBlock extends Block {
    public static int num_models = 12;
    public static final IntProperty COLOR_OFFSET = IntProperty.of("color", 0, num_models - 1);
    public static final MapCodec<IridescentBlock> CODEC = createCodec(IridescentBlock::new);

    public IridescentBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(COLOR_OFFSET, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COLOR_OFFSET);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getPosBased(ctx.getBlockPos());
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.equals(getPosBased(pos))) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        world.setBlockState(pos, getPosBased(pos));
    }

    public BlockState getPosBased(BlockPos pos) {
        return getDefaultState().with(COLOR_OFFSET, getPosBasedOffset(pos));
    }

    public static int getPosBasedOffset(BlockPos pos) {
        return WarpLogic.properMod((int)(num_models*(Math.cos(pos.getX() / 16.0) + Math.cos(pos.getY() / 16.0) + Math.cos(pos.getZ() / 16.0))), num_models);
    }
}
