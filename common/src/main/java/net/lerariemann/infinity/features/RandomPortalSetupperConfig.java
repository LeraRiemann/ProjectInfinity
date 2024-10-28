package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

public record RandomPortalSetupperConfig(boolean axis_x, int offset_l, int offset_t, int y) implements FeatureConfig {
    public static final Codec<RandomPortalSetupperConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (Codec.BOOL.fieldOf("axis_x")).orElse(Boolean.TRUE).forGetter(a -> a.axis_x),
            (Codec.INT.fieldOf("offset_l")).orElse(8).forGetter(a -> a.offset_l),
            (Codec.INT.fieldOf("offset_t")).orElse(8).forGetter(a -> a.offset_t),
            (Codec.INT.fieldOf("y")).orElse(48).forGetter(a -> a.y)).apply(
            instance, RandomPortalSetupperConfig::new));
}
