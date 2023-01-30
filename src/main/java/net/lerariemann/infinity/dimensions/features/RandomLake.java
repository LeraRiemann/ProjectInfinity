package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

public class RandomLake extends RandomisedFeature {
    public RandomLake(int i, RandomProvider provider, String path) {
        super(i, provider);
        name = "lake_" + i;
        boolean surface = random.nextBoolean();
        id = "lake";
        type = (surface ? "lake_surface" : "lake_underground");
        save(path,1 + random.nextInt(surface ? 50 : 9));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        String block = genBlockOrFluid();
        addBlockProviderCarefully(config, "fluid", block);
        addRandomBlockProvider(config, "barrier");
        return feature(config);
    }
}
