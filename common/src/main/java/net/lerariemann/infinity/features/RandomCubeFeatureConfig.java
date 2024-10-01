package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public record RandomCubeFeatureConfig(BlockStateProvider blockProvider, List<BlockState> replaceable, FloatProvider radius, boolean useBands) implements FeatureConfig {
    public static final Codec<RandomCubeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(a -> a.blockProvider),
            (Codec.list(BlockState.CODEC).fieldOf("replaceable")).forGetter(a -> a.replaceable),
            (FloatProvider.createValidatedCodec(2.0f, 20.0f).fieldOf("radius")).forGetter(a -> a.radius),
            (Codec.BOOL.fieldOf("use_bands")).orElse(false).forGetter(a -> a.useBands)).apply(
            instance, RandomCubeFeatureConfig::new));
}
