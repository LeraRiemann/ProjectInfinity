package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

public record RandomPortalSetupperConfig(boolean axis_x, int width, int height, int offset_l, int offset_t,
                                         int sign_offset_l, int sign_offset_y, int y) implements FeatureConfig {
    public static final Codec<RandomPortalSetupperConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            (Codec.BOOL.fieldOf("axis_x")).orElse(Boolean.TRUE).forGetter(a -> a.axis_x),
            (Codec.INT.fieldOf("width")).orElse(2).forGetter(a -> a.width),
            (Codec.INT.fieldOf("height")).orElse(3).forGetter(a -> a.height),
            (Codec.INT.fieldOf("offset_l")).orElse(8).forGetter(a -> a.offset_l),
            (Codec.INT.fieldOf("offset_t")).orElse(8).forGetter(a -> a.offset_t),
            (Codec.INT.fieldOf("sign_offset_l")).orElse(0).forGetter(a -> a.sign_offset_l),
            (Codec.INT.fieldOf("sign_offset_y")).orElse(0).forGetter(a -> a.sign_offset_y),
            (Codec.INT.fieldOf("y")).orElse(16).forGetter(a -> a.y)).apply(
            instance, RandomPortalSetupperConfig::new));
}
