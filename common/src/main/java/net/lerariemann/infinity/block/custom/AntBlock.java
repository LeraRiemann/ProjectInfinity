package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
public class AntBlock extends HorizontalFacingBlock {

    public AntBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));

    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
//        super.scheduledTick(state, world, pos, random);
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.getBlock() == Blocks.WHITE_CONCRETE) {
            this.move(state, world, pos, Clockwiseness.CW);
        } else if (blockState.getBlock() == Blocks.BLACK_CONCRETE) {
            this.move(state, world, pos, Clockwiseness.CCW);
        }

    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        move(state, world, pos, Clockwiseness.CW);
        return ActionResult.SUCCESS;
    };



        private void move(BlockState state, World world, BlockPos pos, Clockwiseness clockwiseness) {
        Direction direction = state.get(FACING);
        Direction direction2 = clockwiseness == Clockwiseness.CW ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
        BlockPos blockPos = pos.offset(direction2);
        if (world.canSetBlock(blockPos)) {
            switch (clockwiseness) {
                case CW:
                    world.setBlockState(pos.down(), Blocks.BLACK_CONCRETE.getDefaultState(), 19);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(blockPos, state.with(FACING, direction2), 3);
                    break;
                case CCW:
                    world.setBlockState(pos.down(), Blocks.WHITE_CONCRETE.getDefaultState(), 19);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(blockPos, state.with(FACING, direction2), 3);
            }
        }
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 1);

    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    enum Clockwiseness {
        CW,
        CCW;

    }
}
