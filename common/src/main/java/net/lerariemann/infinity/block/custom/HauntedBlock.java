package net.lerariemann.infinity.block.custom;

import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class HauntedBlock extends Block {
    BlockState original;

    public HauntedBlock(Block original) {
        super(AbstractBlock.Settings.copy(original));
        this.original = original.getDefaultState();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return original.getCollisionShape(world, pos);
    }
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return original.getOutlineShape(world, pos);
    }
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return original.getRenderType();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        int t = InfinityOptions.access(world).getHauntingTicks(world.random);
        if (t > 0) world.scheduleBlockTick(pos, this, t);
    }
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        world.setBlockState(pos, original);
    }
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, original);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 15;
    }
    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}
