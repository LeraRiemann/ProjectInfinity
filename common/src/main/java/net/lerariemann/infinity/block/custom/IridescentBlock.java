package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class IridescentBlock extends Block {
    public static int num_models = 24;
    public static final IntProperty COLOR_OFFSET = IntProperty.of("color", 0, num_models - 1);

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
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) world.scheduleBlockTick(pos, this, 1);
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.IRIDESCENT_STAR.get())) {
            world.setBlockState(pos, state.with(COLOR_OFFSET, (state.get(COLOR_OFFSET) + 1) % num_models));
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1f, 1f);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    public static class Carpet extends IridescentBlock {
        protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

        public Carpet(Settings settings) {
            super(settings);
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return SHAPE;
        }

        @Override
        public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
            return !state.canPlaceAt(world, pos)
                    ? Blocks.AIR.getDefaultState()
                    : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }

        @Override
        public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
            return !world.isAir(pos.down());
        }
    }
}
