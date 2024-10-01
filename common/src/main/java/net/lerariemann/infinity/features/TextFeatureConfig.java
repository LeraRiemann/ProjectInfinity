package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public record TextFeatureConfig(BlockStateProvider blockProvider, List<BlockState> replaceable, int orientation, int spacing, String text) implements FeatureConfig {
    public static final Codec<TextFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (BlockStateProvider.TYPE_CODEC.fieldOf("block_provider")).forGetter(a -> a.blockProvider),
            (Codec.list(BlockState.CODEC).fieldOf("replaceable")).forGetter(a -> a.replaceable),
            (Codec.INT.fieldOf("orientation")).orElse(2).forGetter(a -> a.orientation),
            (Codec.INT.fieldOf("spacing")).orElse(1).forGetter(a -> a.spacing),
            (Codec.STRING.fieldOf("text")).forGetter(a -> a.text)).apply(instance, TextFeatureConfig::new));
}
