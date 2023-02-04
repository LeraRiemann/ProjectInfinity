package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;

public class RandomLake extends RandomisedFeature {
    public RandomLake(RandomFeaturesList parent) {
        super(parent, "lake");
        boolean surface = random.nextBoolean();
        type = (surface ? "lake_surface" : "lake_underground");
        save(1 + random.nextInt(surface ? 50 : 9));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        String block = genBlockOrFluid();
        addBlockProviderCarefully(config, "fluid", block);
        addRandomBlockProvider(config, "barrier", "full_blocks");
        return feature(config);
    }
}
