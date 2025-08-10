package net.lerariemann.infinity.block.custom;

import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import net.lerariemann.infinity.compat.CreateCompat;
import net.lerariemann.infinity.registry.core.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RailHelper extends BlockWithEntity {
    public RailHelper(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (Platform.isModLoaded("create")) {
            CreateCompat.reattachRails(world, pos, getBlockEntity(world, pos));
        }
        else world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    public static RHBEntity getBlockEntity(ServerWorld world, BlockPos pos) {
        return ((RHBEntity)world.getBlockEntity(pos));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RHBEntity(pos, state);
    }

    public static class RHBEntity extends BlockEntity {
        public Identifier trackBlock;
        public String shape;

        public RHBEntity(BlockPos pos, BlockState state) {
            super(ModBlockEntities.RAIL_HELPER.get(), pos, state);
            trackBlock = null;
            shape = "xo";
        }
    }
}