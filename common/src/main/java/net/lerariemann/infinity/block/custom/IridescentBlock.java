package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class IridescentBlock extends Block {
    public static int num_models = 24;
    public static final IntProperty COLOR_OFFSET = IntProperty.of("color", 0, num_models - 1);
    public static final MapCodec<IridescentBlock> CODEC = createCodec(IridescentBlock::new);

    public IridescentBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(COLOR_OFFSET, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(COLOR_OFFSET);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getPosBased(ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 1);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        BlockState s = getPosBased(world, pos);
        if(!state.equals(s)) world.setBlockState(pos, s);
    }

    public BlockState getPosBased(World world, BlockPos pos) {
        return getDefaultState().with(COLOR_OFFSET, InfinityOptions.access(world).iridMap.getColor(pos));
    }
}
