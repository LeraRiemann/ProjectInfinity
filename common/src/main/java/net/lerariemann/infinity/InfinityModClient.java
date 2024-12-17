package net.lerariemann.infinity;

import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

public class InfinityModClient {
    public final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);

    public static void initializeClient() {
        ModPayloads.registerPayloadsClient();
        ModEntities.registerEntityRenderers();
    }
}
