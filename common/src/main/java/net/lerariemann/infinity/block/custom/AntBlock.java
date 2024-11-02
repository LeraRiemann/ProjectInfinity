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
    public static final MapCodec<AntBlock> CODEC = createCodec(AntBlock::new);

    public AntBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        Block down = world.getBlockState(pos.down()).getBlock();
        if ((down == Blocks.WHITE_CONCRETE) || (down == Blocks.BLACK_CONCRETE)) {
            this.move(state, world, pos);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return move(state, world, pos);
    }

    private ActionResult move(BlockState blockState, World world, BlockPos pos) {
        Clockwiseness clockwiseness;
        Block down = world.getBlockState(pos.down()).getBlock();
        if (down == Blocks.WHITE_CONCRETE) {
            clockwiseness = Clockwiseness.CW;
        } else if (down == Blocks.BLACK_CONCRETE) {
            clockwiseness = Clockwiseness.CCW;
        }
        else return ActionResult.FAIL;
        Direction direction = blockState.get(FACING);
        Direction direction2 = clockwiseness == Clockwiseness.CW ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
        BlockPos blockPos = pos.offset(direction2);
        if (world.canPlace(blockState, blockPos, ShapeContext.absent())) {
            switch (clockwiseness) {
                case CW:
                    world.setBlockState(pos.down(), Blocks.BLACK_CONCRETE.getDefaultState(), 19);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(blockPos, blockState.with(FACING, direction2), 3);
                    break;
                case CCW:
                    world.setBlockState(pos.down(), Blocks.WHITE_CONCRETE.getDefaultState(), 19);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(blockPos, blockState.with(FACING, direction2), 3);
            }
        }
        return ActionResult.SUCCESS;
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
        CCW
    }
}
