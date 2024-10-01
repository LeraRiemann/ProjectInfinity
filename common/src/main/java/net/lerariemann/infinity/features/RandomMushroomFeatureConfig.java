package net.lerariemann.infinity.features;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record RandomMushroomFeatureConfig(BlockStateProvider capProvider, BlockStateProvider stemProvider,
                                          BlockState validBaseBlock, int foliageRadius,
                                          int height) implements FeatureConfig {
    public static final Codec<RandomMushroomFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (BlockStateProvider.TYPE_CODEC.fieldOf("cap_provider")).forGetter(a -> a.capProvider),
            (BlockStateProvider.TYPE_CODEC.fieldOf("stem_provider")).forGetter(a -> a.stemProvider),
            (BlockState.CODEC.fieldOf("valid_base_block")).forGetter(a -> a.validBaseBlock),
            (Codec.INT.fieldOf("foliage_radius")).orElse(2).forGetter(a -> a.foliageRadius),
            (Codec.INT.fieldOf("height")).orElse(5).forGetter(a -> a.height)).apply(
            instance, RandomMushroomFeatureConfig::new));
}
