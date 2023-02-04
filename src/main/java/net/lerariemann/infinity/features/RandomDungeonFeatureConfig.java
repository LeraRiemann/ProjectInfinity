package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;

public class RandomDungeonFeatureConfig implements FeatureConfig {
    public static final Codec<RandomDungeonFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec) BlockState.CODEC.fieldOf("main_state")).forGetter(a -> ((RandomDungeonFeatureConfig) a).mainProvider),
            ((MapCodec) BlockState.CODEC.fieldOf("decor_state")).forGetter(a -> ((RandomDungeonFeatureConfig) a).decorationProvider),
            ((MapCodec) Codec.STRING.fieldOf("mob")).orElse("minecraft:pig").forGetter(a -> ((RandomDungeonFeatureConfig) a).mob),
            ((MapCodec) Codec.INT.fieldOf("size")).orElse(2).forGetter(a -> ((RandomDungeonFeatureConfig) a).size)).apply(instance, (mainProvider1, decorationProvider1, mob1, size1) -> new RandomDungeonFeatureConfig((BlockState)mainProvider1, (BlockState)decorationProvider1, (String)mob1, (int)size1)));
    public final BlockState mainProvider;
    public final BlockState decorationProvider;
    public final String mob;
    public final int size;

    public RandomDungeonFeatureConfig(BlockState mainProvider, BlockState decorationProvider, String mob, int size) {
        this.mainProvider = mainProvider;
        this.decorationProvider = decorationProvider;
        this.mob = mob;
        this.size = size;
    }
}