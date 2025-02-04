package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.block.entity.HauntedBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SuperHauntedBlock extends BlockWithEntity {
    public static final MapCodec<SuperHauntedBlock> CODEC = createCodec(SuperHauntedBlock::new);
    public SuperHauntedBlock(Settings settings) {
        super(settings);
    }

    public static BlockState getOriginal(BlockView w, BlockPos p) {
        if (w.getBlockEntity(p) instanceof HauntedBlockEntity e) return e.original;
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (world.getBlockEntity(pos) instanceof HauntedBlockEntity e) {
            e.updateFrom(oldState);
        }
        int t = HauntedBlockEntity.getExpiryTicks(world);
        if (t > 0) world.scheduleBlockTick(pos, this, t);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        super.scheduledTick(state, world, pos, random);
        world.setBlockState(pos, getOriginal(world, pos));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, getOriginal(world, pos));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOriginal(world, pos).getCollisionShape(world, pos);
    }
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOriginal(world, pos).getOutlineShape(world, pos);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 15;
    }
    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 15;
    }
    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HauntedBlockEntity(pos, state);
    }
}
