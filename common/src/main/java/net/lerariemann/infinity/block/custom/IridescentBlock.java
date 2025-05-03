package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.block.entity.ChromaticBlockEntity;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class IridescentBlock extends Block {
    public static int num_models = 24;
    public static final IntProperty COLOR_OFFSET = IntProperty.of("color", 0, num_models - 1);
    public static final MapCodec<IridescentBlock> CODEC = createCodec(IridescentBlock::new);

    public IridescentBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(COLOR_OFFSET, 0));
    }

    @Override
    public MapCodec<? extends IridescentBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(COLOR_OFFSET);
    }

    @Nullable
    public BlockState toStatic(BlockState state) {
        return ModBlocks.CHROMATIC_WOOL.get().getDefaultState();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.STAR_OF_LANG.get())) {
            BlockState state1 = toStatic(state);
            if (state1 != null) {
                world.setBlockState(pos, state1);
                if (world.getBlockEntity(pos) instanceof ChromaticBlockEntity cbe) {
                    cbe.setColor(state.get(COLOR_OFFSET)*(360 / num_models), 255, 255, null);
                }
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hit);
    }

    public static class Carpet extends IridescentBlock {
        protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

        public Carpet(Settings settings) {
            super(settings);
        }

        @Override
        protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return SHAPE;
        }

        @Override
        protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
            return !state.canPlaceAt(world, pos)
                    ? Blocks.AIR.getDefaultState()
                    : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        @Override
        protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
            return !world.isAir(pos.down());
        }

        @Override
        public BlockState toStatic(BlockState state) {
            return ModBlocks.CHROMATIC_CARPET.get().getDefaultState();
        }
    }
}
