package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public record RandomCeilingBlobFeatureConfig(BlockStateProvider block, List<BlockState> target_blocks, int size_xz, int size_y) implements FeatureConfig {
    public static final Codec<RandomCeilingBlobFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (BlockStateProvider.TYPE_CODEC.fieldOf("block")).forGetter(a -> a.block),
            (Codec.list(BlockState.CODEC).fieldOf("targets")).forGetter(a -> a.target_blocks),
            (Codec.INT.fieldOf("size_xz")).forGetter(a -> a.size_xz),
            (Codec.INT.fieldOf("size_y")).forGetter(a -> a.size_y)).apply(
            instance, RandomCeilingBlobFeatureConfig::new));
}
