package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.HashSet;
import java.util.Set;

public class RandomCeilingBlobFeature extends Feature<RandomCeilingBlobFeatureConfig> {
    public RandomCeilingBlobFeature(Codec<RandomCeilingBlobFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomCeilingBlobFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        if (!structureWorldAccess.isAir(blockPos)) {
            return false;
        }
        BlockState blockState = structureWorldAccess.getBlockState(blockPos.up());
        Set<Block> blocks = new HashSet<>();
        RandomCeilingBlobFeatureConfig config = context.getConfig();
        for (BlockState b : config.target_blocks()) blocks.add(b.getBlock());
        if (!blocks.contains(blockState.getBlock())) {
            return false;
        }
        BlockState state = context.getConfig().block().get(random, blockPos);
        structureWorldAccess.setBlockState(blockPos, state, Block.NOTIFY_LISTENERS);
        for (int i = 0; i < 1500; ++i) {
            BlockPos blockPos2 = blockPos.add(random.nextInt(config.size_xz()) - random.nextInt(config.size_xz()),
                    -random.nextInt(config.size_y()), random.nextInt(config.size_xz()) - random.nextInt(config.size_xz()));
            if (!structureWorldAccess.getBlockState(blockPos2).isAir()) continue;
            int j = 0;
            for (Direction direction : Direction.values()) {
                if (structureWorldAccess.getBlockState(blockPos2.offset(direction)).isOf(state.getBlock())) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j == 0) continue;
            structureWorldAccess.setBlockState(blockPos2, state, Block.NOTIFY_LISTENERS);
        }
        return true;
    }
}