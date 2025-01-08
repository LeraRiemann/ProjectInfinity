package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
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
import org.jetbrains.annotations.Nullable;

public class ChromaticBlock extends BlockWithEntity {
    public static final MapCodec<ChromaticBlock> CODEC = createCodec(ChromaticBlock::new);
    public ChromaticBlock(Settings settings) {
        super(settings);
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        ItemStack res = super.getPickStack(world, pos, state);
        if (!res.isEmpty() && world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe)
            res.applyComponentsFrom(cbe.asMap());
        return res;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
            ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
            if (cbe.onUse(world, pos, stack)) {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hit);
    }

    public static class Carpet extends ChromaticBlock {
        protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

        public Carpet(Settings settings) {
            super(settings);
        }

        @Override
        protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return SHAPE;
        }

        @Override
        protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
            return !state.canPlaceAt(world, pos)
                    ? Blocks.AIR.getDefaultState()
                    : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }

        @Override
        protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
            return !world.isAir(pos.down());
        }
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromaticBlockEntity(pos, state);
    }
}
