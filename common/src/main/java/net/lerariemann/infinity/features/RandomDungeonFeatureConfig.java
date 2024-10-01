package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;

public record RandomDungeonFeatureConfig(BlockState mainProvider, BlockState decorationProvider, String mob,
                                         int size) implements FeatureConfig {
    public static final Codec<RandomDungeonFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (BlockState.CODEC.fieldOf("main_state")).forGetter(a -> a.mainProvider),
            (BlockState.CODEC.fieldOf("decor_state")).forGetter(a -> a.decorationProvider),
            (Codec.STRING.fieldOf("mob")).orElse("minecraft:pig").forGetter(a -> a.mob),
            (Codec.INT.fieldOf("size")).orElse(2).forGetter(a -> a.size)).apply(
            instance, RandomDungeonFeatureConfig::new));

}