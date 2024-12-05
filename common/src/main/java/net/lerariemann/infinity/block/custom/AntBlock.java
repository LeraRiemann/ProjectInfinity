package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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

    boolean inverseExists(Block down) {
        var s = Registries.BLOCK.getEntry(down).getIdAsString();
        var state = down.getDefaultState();
        if (PlatformMethods.isInBlack(state)) {
            return Registries.BLOCK.containsId(Identifier.of(s.replace("black", "white")));
        }
        if (PlatformMethods.isInWhite(state)) {
            return Registries.BLOCK.containsId(Identifier.of(s.replace("white", "black")));
        }
        return false;
    }

    Block recolor(Block down, boolean toWhite) {
        String s = Registries.BLOCK.getEntry(down).getIdAsString();
        var state = down.getDefaultState();
        if (PlatformMethods.isInBlack(state)) {
            return toWhite ? Registries.BLOCK.get(Identifier.of(s.replace("black", "white"))) : down;
        }
        if (PlatformMethods.isInWhite(state)) {
            return toWhite ? down : Registries.BLOCK.get(Identifier.of(s.replace("white", "black")));
        }
        return null;
    }

    @Nullable
    Clockwiseness getCW(Block down) {
        String s = Registries.BLOCK.getEntry(down).getIdAsString();
        if (s.contains("black")) {
            return Clockwiseness.CCW;
        }
        if (s.contains("white")) {
            return Clockwiseness.CW;
        }
        return null;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        Block down = world.getBlockState(pos.down()).getBlock();
        if (inverseExists(down)) {
            this.safeMove(state, world, pos);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return move(state, world, pos);
    }

    private ActionResult move(BlockState blockState, World world, BlockPos pos) {
        if (inverseExists(world.getBlockState(pos.down()).getBlock())) return safeMove(blockState, world, pos);
        return ActionResult.FAIL;
    }

    private ActionResult safeMove(BlockState blockState, World world, BlockPos pos) {
        BlockState down = world.getBlockState(pos.down());
        Clockwiseness clockwiseness = getCW(down.getBlock());
        if (clockwiseness == null) return ActionResult.FAIL;
        Direction direction = blockState.get(FACING);
        Direction direction2 = clockwiseness == Clockwiseness.CW ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
        BlockPos blockPos = pos.offset(direction2);
        if (world.canPlace(blockState, blockPos, ShapeContext.absent()) && world.getBlockState(blockPos).isReplaceable()) {
            switch (clockwiseness) {
                case CW:
                    world.setBlockState(pos.down(), recolor(down.getBlock(), false).getStateWithProperties(down), 19);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockState(blockPos, blockState.with(FACING, direction2), 3);
                    break;
                case CCW:
                    world.setBlockState(pos.down(), recolor(down.getBlock(), true).getStateWithProperties(down), 19);
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
