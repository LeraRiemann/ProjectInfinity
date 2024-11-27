package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RandomCeilingBlobFeature extends Feature<RandomCeilingBlobFeature.Config> {
    public RandomCeilingBlobFeature(Codec<RandomCeilingBlobFeature.Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<RandomCeilingBlobFeature.Config> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        if (!structureWorldAccess.isAir(blockPos)) {
            return false;
        }
        BlockState blockState = structureWorldAccess.getBlockState(blockPos.up());
        Set<Block> blocks = new HashSet<>();
        Config config = context.getConfig();
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

    public record Config(BlockStateProvider block, List<BlockState> target_blocks, int size_xz, int size_y) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (BlockStateProvider.TYPE_CODEC.fieldOf("block")).forGetter(a -> a.block),
                (Codec.list(BlockState.CODEC).fieldOf("targets")).forGetter(a -> a.target_blocks),
                (Codec.INT.fieldOf("size_xz")).forGetter(a -> a.size_xz),
                (Codec.INT.fieldOf("size_y")).forGetter(a -> a.size_y)).apply(
                instance, Config::new));
    }
}