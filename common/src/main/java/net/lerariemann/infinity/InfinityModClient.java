package net.lerariemann.infinity;

import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.var.ModPayloads;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

public class InfinityModClient {
    public final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);



    public static void init() {
        ModPayloads.registerPayloadsClient();
        ModEntities.registerEntityRenderers();
    }
}
