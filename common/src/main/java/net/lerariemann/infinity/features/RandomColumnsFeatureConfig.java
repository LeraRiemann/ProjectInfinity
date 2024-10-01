package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record RandomColumnsFeatureConfig(IntProvider reach, IntProvider height,
                                         BlockStateProvider block) implements FeatureConfig {
    public static final Codec<RandomColumnsFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (IntProvider.createValidatingCodec(0, 3).fieldOf("reach")).forGetter(a -> a.reach),
            (IntProvider.createValidatingCodec(1, 15).fieldOf("height")).forGetter(a -> a.height),
            (BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(a -> a.block)).apply(
            instance, RandomColumnsFeatureConfig::new));
}

