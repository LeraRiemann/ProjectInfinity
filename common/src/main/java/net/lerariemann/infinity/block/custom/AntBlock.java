package net.lerariemann.infinity.block.custom;

import net.minecraft.block.*;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AntBlock extends HorizontalFacingBlock {

    public AntBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    private static boolean inverseExists(Block down) {
        Identifier id = Registries.BLOCK.getId(down);
        String n = id.getNamespace();
        String s = id.getPath();
        if (s.contains("black_")) {
            return Registries.BLOCK.containsId(Identifier.of(n, s.replace("black", "white")));
        }
        if (s.contains("white_")) {
            return Registries.BLOCK.containsId(Identifier.of(n, s.replace("white", "black")));
        }
        return false;
    }
    public static boolean isSafeToRecolor(World world, BlockPos pos) {
        return inverseExists(world.getBlockState(pos).getBlock()) && (world.getBlockEntity(pos) == null);
    }

    public static Block recolor(Block down, boolean toWhite) {
        Identifier id = Registries.BLOCK.getId(down);
        String n = id.getNamespace();
        String s = id.getPath();
        if (s.contains("black_")) {
            return toWhite ? Registries.BLOCK.get(Identifier.of(n, s.replace("black", "white"))) : down;
        }
        if (s.contains("white_")) {
            return toWhite ? down : Registries.BLOCK.get(Identifier.of(n, s.replace("white", "black")));
        }
        return null;
    }

    @Nullable
    public static Clockwiseness getCW(Block down) {
        var s = down.toString();
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
        if (isSafeToRecolor(world, pos.down())) {
            this.safeMove(state, world, pos);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return move(state, world, pos);
    }

    private ActionResult move(BlockState blockState, World world, BlockPos pos) {
        if (isSafeToRecolor(world, pos.down())) return safeMove(blockState, world, pos);
        return ActionResult.FAIL;
    }

    private ActionResult safeMove(BlockState blockState, World world, BlockPos pos) {
        BlockState down = world.getBlockState(pos.down());
        Clockwiseness clockwiseness = getCW(down.getBlock());
        if (clockwiseness == null) return ActionResult.FAIL;
        Direction direction = blockState.get(FACING);
        Direction direction2 = clockwiseness == Clockwiseness.CW ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
        BlockPos blockPos = pos.offset(direction2);
        if (world.canSetBlock(blockPos) && world.getBlockState(blockPos).isReplaceable()) {
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

    public enum Clockwiseness {
        CW,
        CCW
    }
}